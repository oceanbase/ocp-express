/*
 * Copyright (c) 2023 OceanBase
 * OCP Express is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.oceanbase.ocp.config.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.config.security.handler.CustomAuthenticationFailureHandler;
import com.oceanbase.ocp.config.security.handler.CustomAuthenticationSuccessHandler;
import com.oceanbase.ocp.config.security.handler.CustomLogoutSuccessHandler;
import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.i18n.I18nService;
import com.oceanbase.ocp.core.security.util.CustomBCryptPasswordEncoder;
import com.oceanbase.ocp.core.util.WebRequestUtils;
import com.oceanbase.ocp.security.iam.JdbcUserDetailsService;
import com.oceanbase.ocp.security.iam.LoginKeyService;
import com.oceanbase.ocp.security.iam.UserLoginAttemptManager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CustomSecurityConfiguration {

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(I18nService i18nService) {
        return new CustomAuthenticationEntryPoint(i18nService);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new JdbcUserDetailsService();
    }

    /**
     * Local JDBC Authentication support (will be disabled, if BUC SSO is enabled):
     *
     * <pre>
     * http body "username=xxx&password=xxx"
     * </pre>
     */
    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE - 100)
    @EnableWebSecurity
    static class JdbcAuthenticationConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private CommonSecurityProperties commonSecurityProperties;

        @Autowired
        private LoginKeyService loginKeyService;

        @Autowired
        public UserDetailsService userDetailsService;

        @Bean
        public BCryptPasswordEncoder customPasswordEncoder() {
            return new CustomBCryptPasswordEncoder(loginKeyService.getLoginPrivateKey());
        }

        @Autowired
        public CsrfRequestMatcher csrfRequestMatcher;

        @Autowired
        public AuthenticationEntryPoint authenticationEntryPoint;

        @Autowired
        private I18nService i18nService;

        @Autowired
        private UserLoginAttemptManager userLoginAttemptManager;

        @Bean
        public AuthenticationFailureHandler authenticationFailureHandler() {
            return new CustomAuthenticationFailureHandler(i18nService, userLoginAttemptManager);
        }

        @Bean
        public AuthenticationSuccessHandler authenticationSuccessHandler() {
            return new CustomAuthenticationSuccessHandler(userLoginAttemptManager);
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setHideUserNotFoundExceptions(false);
            provider.setUserDetailsService(userDetailsService);
            provider.setPasswordEncoder(customPasswordEncoder());
            return provider;
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(authenticationProvider());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http.authorizeRequests()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).denyAll()
                        .regexMatchers(commonSecurityProperties.getAuthWhitelist()).permitAll()
                        .regexMatchers("login?callback=.*").permitAll()
                        .antMatchers("/assets/**", "/index.html", "/**/*.js", "/**/*.css", "/webjars/**", "/**/*.svg").permitAll()
                    .and()
                        .authorizeRequests()
                        .anyRequest().authenticated()
                    .and()
                        .formLogin()
                        .loginPage(OcpConstants.LOGIN_PAGE).permitAll()
                        .loginProcessingUrl("/api/v1/login")
                        .defaultSuccessUrl("/index")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                    .and()
                        .logout()
                        .logoutUrl("/api/v1/logout")
                        .deleteCookies("JSESSIONID", "callback")
                        .invalidateHttpSession(true)
                        .permitAll()
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler("/login"))
                    .and()
                        .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation()
                        .migrateSession()
                    .and()
                        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
            // @formatter:on

            // We should only disable csrf in development environments
            if (commonSecurityProperties.isCsrfEnabled()) {
                http.csrf().requireCsrfProtectionMatcher(csrfRequestMatcher)
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
            } else {
                http.csrf().disable();
            }
            if (commonSecurityProperties.isBasicAuthEnabled()) {
                http.httpBasic().authenticationEntryPoint(this::handleAuthFailed);
            }

            log.info("Local JDBC authentication enabled: whiteList {}, basicAuth {}, CSRF {}",
                    commonSecurityProperties.getAuthWhitelist(), commonSecurityProperties.isBasicAuthEnabled(),
                    commonSecurityProperties.isCsrfEnabled());
        }

        @SneakyThrows
        private void handleAuthFailed(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException e) {
            log.info("Basic auth failed, client={}, uri={}, authStr={}, errMsg={}",
                    WebRequestUtils.getClientAddress(request), request.getRequestURI(),
                    request.getHeader("Authorization"),
                    e.getMessage());
            ObjectNode node = JsonUtils.OBJECT_MAPPER.createObjectNode();
            node.put("status", HttpStatus.UNAUTHORIZED.value());
            node.put("successful", false);
            node.put("timestamp", System.currentTimeMillis());
            node.put("errMsg", e.getMessage());
            response.getWriter().write(JsonUtils.OBJECT_MAPPER.writeValueAsString(node));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

    }

}
