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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.common.util.json.JacksonFactory;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.config.security.CustomAuthenticationEntryPoint;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.core.security.model.AuthenticatedUser;
import com.oceanbase.ocp.security.iam.UserLoginAttemptManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String LOCAL_IP = HostUtils.getLocalIp();

    private final ObjectMapper objectMapper = JacksonFactory.jsonMapper();

    private final UserLoginAttemptManager userLoginAttemptManager;

    public CustomAuthenticationSuccessHandler(UserLoginAttemptManager userLoginAttemptManager) {
        this.userLoginAttemptManager = userLoginAttemptManager;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        log.info("Authentication successful for {}", request.getRequestURI());
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        postAuthenticationSuccess(user);
        boolean needChangePassword = user.isNeedChangePassword();

        String targetUrl = getTargetUrlAndRemoveCookie(request, response);
        // Use the DefaultSavedRequest URL
        setJsonResponse(response, targetUrl, needChangePassword);
    }

    private String getTargetUrlAndRemoveCookie(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = "/";
        for (Cookie cookie : request.getCookies()) {
            if (CustomAuthenticationEntryPoint.CALLBACK_COOKIE_NAME.equals(cookie.getName())) {
                targetUrl = cookie.getValue();
                Cookie c = new Cookie("callback", null);
                c.setMaxAge(0);
                c.setPath("/");
                response.addCookie(c);
                break;
            }
        }
        return targetUrl;
    }

    private void postAuthenticationSuccess(AuthenticatedUser user) {
        userLoginAttemptManager.handleLoginSuccess(user);
    }

    private void setJsonResponse(HttpServletResponse response, String targetUrl, boolean needChangePassword)
            throws IOException {
        log.info("Redirecting to saved url: " + targetUrl);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(getSuccessResponse(targetUrl, needChangePassword));
    }

    private String getSuccessResponse(String targetUrl, boolean needChangePassword) throws IOException {
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("location", targetUrl);
        resMap.put("needChangePassword", needChangePassword);
        SuccessResponse<Map<String, Object>> successResponse =
                ResponseBuilder.single(resMap);
        successResponse.setTraceId(TraceUtils.getTraceId());
        successResponse.setDuration(TraceUtils.getDuration());
        successResponse.setServer(LOCAL_IP);
        return objectMapper.writeValueAsString(successResponse);
    }

}
