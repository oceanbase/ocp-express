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

package com.oceanbase.ocp.config.security.handler;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.exception.TooManyAttemptException;
import com.oceanbase.ocp.core.i18n.ErrorCode;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.i18n.I18nService;
import com.oceanbase.ocp.core.response.ErrorResponse;
import com.oceanbase.ocp.core.response.error.ApiError;
import com.oceanbase.ocp.core.util.WebRequestUtils;
import com.oceanbase.ocp.security.iam.UserLoginAttemptManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final I18nService i18nService;
    private final UserLoginAttemptManager userLoginAttemptManager;

    public CustomAuthenticationFailureHandler(I18nService i18nService,
            UserLoginAttemptManager userLoginAttemptManager) {
        this.i18nService = i18nService;
        this.userLoginAttemptManager = userLoginAttemptManager;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        final String username = request.getParameter("username");
        final Long userId = (Long) (request.getAttribute("userId"));
        final Throwable rootCause = ExceptionUtils.getRootCause(exception);
        final Integer remainingAttempts = (Integer) request.getAttribute("remainingAttempts");

        String errorMessage = getErrorMessage(username, remainingAttempts, rootCause);
        userLoginAttemptManager.handleLoginFail(username, rootCause);

        log.warn("Authentication failure: ip={}, userId={}, username={}, message={}",
                WebRequestUtils.getClientAddress(request), userId, username, errorMessage);

        // Handle Authentication exceptions
        setErrorResponse(response, errorMessage, rootCause);
    }

    private String getErrorMessage(String username, Integer remainAttempts, Throwable rootCause) {
        final Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = getLocalizedMessage(ErrorCodes.IAM_USER_NOT_AUTHENTICATED, locale);
        if (rootCause instanceof LockedException) {
            errorMessage = getLocalizedMessage(ErrorCodes.IAM_USER_ACCOUNT_LOCKED, locale);
        } else if (rootCause instanceof DisabledException) {
            errorMessage = getLocalizedMessage(ErrorCodes.IAM_USER_ACCOUNT_DISABLED, locale);
        } else if (rootCause instanceof AccountExpiredException) {
            errorMessage = getLocalizedMessage(ErrorCodes.IAM_USER_ACCOUNT_EXPIRED, locale);
        } else if (rootCause instanceof CredentialsExpiredException) {
            errorMessage = getLocalizedMessage(ErrorCodes.IAM_CREDENTIALS_EXPIRED, locale);
        } else if (rootCause instanceof BadCredentialsException) {
            final int remainingAttempts = remainAttempts;
            errorMessage = getLocalizedMessage(ErrorCodes.IAM_BAD_CREDENTIALS, locale, remainingAttempts);
        } else if (rootCause instanceof UsernameNotFoundException) {
            final int remainingAttempts = userLoginAttemptManager.getRemainAttemptsByUsername(username);
            errorMessage = getLocalizedMessage(ErrorCodes.IAM_USERNAME_NOT_FOUND, locale, remainingAttempts);
        } else if (rootCause instanceof TooManyAttemptException) {
            errorMessage = getLocalizedMessage(ErrorCodes.IAM_USER_LOGIN_BLOCKED, locale);
        } else {
            log.error("Unexpected authentication failure: username=" + username, rootCause);
        }
        return errorMessage;
    }

    private void setErrorResponse(HttpServletResponse response, String message, Throwable ex)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        // Build ErrorResponse with an ApiError instance
        ApiError apiError = new ApiError(message, ex);
        ErrorResponse errorResponse =
                ErrorResponse.error(HttpStatus.UNAUTHORIZED, apiError, TraceUtils.getTraceId(),
                        TraceUtils.getDuration());
        response.getWriter().write(JsonUtils.toJsonString(errorResponse));
    }

    /**
     * Return a localized error message based on the message key, args and locale.
     */
    private String getLocalizedMessage(ErrorCode errorCode, Locale locale, Object... args) {
        return i18nService.getMessage(errorCode.getKey(), args, errorCode.toString(), locale);
    }

}
