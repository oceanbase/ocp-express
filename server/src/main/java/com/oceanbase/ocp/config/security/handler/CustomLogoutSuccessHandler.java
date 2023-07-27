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
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.common.util.json.JacksonFactory;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final String LOCAL_IP = HostUtils.getLocalIp();

    private final ObjectMapper objectMapper = JacksonFactory.jsonMapper();

    private final String redirectUrl;

    public CustomLogoutSuccessHandler(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if (authentication != null && authentication.getDetails() != null) {
            try {
                request.getSession().invalidate();
            } catch (Exception e) {
                log.warn("An error occurred while logging out {}", e.getMessage());
            }
        }

        // Send redirectUrl as JSON response
        setJsonResponse(response, redirectUrl);
    }

    private void setJsonResponse(HttpServletResponse response, String targetUrl) throws IOException {
        log.info("Redirecting to logout Url: " + targetUrl);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(getSuccessResponse(targetUrl));

    }

    private String getSuccessResponse(String targetUrl) throws IOException {
        SuccessResponse<Map<String, String>> successResponse =
                ResponseBuilder.single(Collections.singletonMap("location", targetUrl));
        successResponse.setTraceId(TraceUtils.getTraceId());
        successResponse.setDuration(TraceUtils.getDuration());
        successResponse.setServer(LOCAL_IP);
        return objectMapper.writeValueAsString(successResponse);
    }
}
