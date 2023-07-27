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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.i18n.I18nService;
import com.oceanbase.ocp.core.response.ErrorResponse;
import com.oceanbase.ocp.core.response.error.ApiError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String CALLBACK_COOKIE_NAME = "callback";

    private static final String ERROR_PAGE = "/error";

    private final I18nService i18nService;

    /**
     * Define API request patterns on the following urls.
     */
    private final AntPathRequestMatcher[] apiRequestMatchers = {new AntPathRequestMatcher("/api/**")};

    public CustomAuthenticationEntryPoint(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException authException) throws IOException {
        if (matchesApi(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(getErrorResponse(authException, LocaleContextHolder.getLocale()));
            log.debug("API {} not authenticated, return error json", request.getRequestURI());
        } else {
            log.debug("User not authenticated to {}, redirect to login page", request.getRequestURI());
            setCallbackCookie(request, response);
            response.sendRedirect(OcpConstants.LOGIN_PAGE);
        }
    }

    private void setCallbackCookie(HttpServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException {
        if (ERROR_PAGE.equals(request.getRequestURI())) {
            return;
        }
        StringBuilder callbackBuilder = new StringBuilder(request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            callbackBuilder.append("?");
            for (String key : parameterMap.keySet()) {
                String encodedValue =
                        URLEncoder.encode(String.join(",", parameterMap.get(key)), StandardCharsets.UTF_8.name());
                callbackBuilder.append(key).append("=").append(encodedValue).append("&");
            }
            callbackBuilder.deleteCharAt(callbackBuilder.length() - 1);
        }
        Cookie cookie = new Cookie(CALLBACK_COOKIE_NAME, callbackBuilder.toString());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private boolean matchesApi(final HttpServletRequest request) {
        return Arrays.stream(apiRequestMatchers)
                .anyMatch(antPathRequestMatcher -> antPathRequestMatcher.matches(request));
    }

    private String getErrorResponse(Throwable ex, Locale locale) throws IOException {
        String errorMessage = i18nService.getLocalizedMessage(ErrorCodes.IAM_USER_NOT_AUTHENTICATED, null, locale);
        ApiError apiError = new ApiError(errorMessage, ex);
        ErrorResponse errorResponse = ErrorResponse.error(HttpStatus.UNAUTHORIZED, apiError, TraceUtils.getTraceId(),
                TraceUtils.getDuration());
        return JsonUtils.toJsonString(errorResponse);
    }

}
