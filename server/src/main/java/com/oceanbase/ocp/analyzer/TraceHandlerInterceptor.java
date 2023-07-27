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

package com.oceanbase.ocp.analyzer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.oceanbase.ocp.common.util.trace.TraceUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TraceHandlerInterceptor implements HandlerInterceptor {

    private static final String HEADER_TRACE_ID = "x-trace-id";
    private static final String HEADER_SPAN_ID = "x-span-id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(HEADER_TRACE_ID);
        String spanId = request.getHeader(HEADER_SPAN_ID);
        if (StringUtils.isNotEmpty(spanId)) {
            TraceUtils.trace(traceId, spanId);
        } else if (StringUtils.isNotEmpty(traceId)) {
            TraceUtils.trace(traceId);
        } else {
            TraceUtils.trace();
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        TraceUtils.clear();
    }
}
