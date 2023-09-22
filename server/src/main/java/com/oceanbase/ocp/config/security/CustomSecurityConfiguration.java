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

import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.config.security.handler.CustomAuthenticationFailureHandler;
import com.oceanbase.ocp.config.security.handler.CustomAuthenticationSuccessHandler;
import com.oceanbase.ocp.config.security.handler.CustomLogoutSuccessHandler;
import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.i18n.I18nService;
import com.oceanbase.ocp.core.response.ErrorResponse;
import com.oceanbase.ocp.core.response.error.ApiError;
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

    @Autowired
    private CommonSecurityProperties commonSecurityProperties;

    @Autowired
    private LoginKeyService loginKeyService;

    @Autowired
    public UserDetailsService userDetailsService;

    @Autowired
    public CsrfRequestMatcher csrfRequestMatcher;

    @Autowired
    private I18nService i18nService;

    @Autowired
    private UserLoginAttemptManager userLoginAttemptManager;

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

    @Bean
    public BCryptPasswordEncoder customPasswordEncoder() {
        return new CustomBCryptPasswordEncoder(loginKeyService.getLoginPrivateKey());
    }


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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/assets/**", "/index.html", "/**/*.js", "/**/*.css", "/webjars/**",
                "/**/*.svg");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider())
                .authorizeRequests(
                        authz -> {
                            authz.regexMatchers("/login*").permitAll()
                                    .regexMatchers(commonSecurityProperties.getAuthWhitelist()).permitAll()
                                    .anyRequest().authenticated();
                        })
                .formLogin(formLogin -> formLogin
                        .loginPage(OcpConstants.LOGIN_PAGE).permitAll()
                        .loginProcessingUrl("/api/v1/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler()))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/logout")
                        .deleteCookies("JSESSIONID", "callback")
                        .invalidateHttpSession(true)
                        .permitAll()
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler("/login")))
                .sessionManagement(sessionMgr -> sessionMgr.sessionCreationPolicy(IF_REQUIRED)
                        .sessionFixation()
                        .migrateSession())
                .exceptionHandling(eh -> eh.authenticationEntryPoint(authenticationEntryPoint(i18nService)));
        if (commonSecurityProperties.isCsrfEnabled()) {
            http.csrf().requireCsrfProtectionMatcher(csrfRequestMatcher)
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        } else {
            http.csrf().disable();
        }
        if (commonSecurityProperties.isBasicAuthEnabled()) {
            http.httpBasic().authenticationEntryPoint(this::handleAuthFailed);
        }
        return http.build();
    }

    @SneakyThrows
    public void handleAuthFailed(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        log.info("Auth failed, client={}, uri={}, authStr={}, errMsg={}",
                WebRequestUtils.getClientAddress(request), request.getRequestURI(), request.getHeader("Authorization"),
                e.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        // Build ErrorResponse with an ApiError instance
        ApiError apiError = new ApiError(e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.error(HttpStatus.UNAUTHORIZED, apiError, TraceUtils.getTraceId(),
                TraceUtils.getDuration());
        response.getWriter().write(JsonUtils.toJsonString(errorResponse));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

}
