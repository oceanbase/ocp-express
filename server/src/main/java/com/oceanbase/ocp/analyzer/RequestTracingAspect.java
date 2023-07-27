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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.common.util.LogContentUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.exception.OcpException;
import com.oceanbase.ocp.core.response.SuccessResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class RequestTracingAspect {

    private static final String LOCAL_IP = HostUtils.getLocalIp();
    private static final String ARGS_SPLITTER = ",";

    /**
     * Pointcut that matches all api controller methods.
     */
    @Pointcut("execution(* com.oceanbase.ocp..*Controller*.*(..))")
    public void controllerMethodPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the
        // advices.
    }

    /**
     * Advice that add logging info to http request, when a REST method is entered.
     *
     * @param joinPoint join point for advice.
     */
    @Before("controllerMethodPointcut()")
    public void traceRequest(JoinPoint joinPoint) {
        String traceId = TraceUtils.getTraceId();
        HttpServletRequest request = getRequest();
        log.info("API: [{} {}?{}, client={}, traceId={}, method={}, args={}]",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr(),
                traceId,
                joinPoint.getSignature(),
                getArgs(joinPoint));
    }

    /**
     * Advice that log logging info when a REST method is exited.
     *
     * @param joinPoint join point for advice.
     */
    @AfterReturning(pointcut = "controllerMethodPointcut()", returning = "retVal")
    public void traceResponse(JoinPoint joinPoint, Object retVal) {
        HttpServletRequest request = getRequest();
        long duration = TraceUtils.getDuration();
        String traceId = TraceUtils.getTraceId();
        log.info("API OK: [{} {} client={}, traceId={}, duration={} ms]",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), traceId, duration);
        if (retVal instanceof SuccessResponse<?>) {
            SuccessResponse<?> result = (SuccessResponse<?>) retVal;
            result.setDuration(duration);
            result.setTraceId(traceId);
            result.setServer(LOCAL_IP);
        }
    }

    /**
     * Advice that log logging info when a REST method throws an exception.
     *
     * @param joinPoint join point for advice.
     */
    @AfterThrowing(pointcut = "controllerMethodPointcut()", throwing = "e")
    public void traceException(JoinPoint joinPoint, Throwable e) {
        HttpServletRequest request = getRequest();
        long duration = TraceUtils.getDuration();
        String traceId = TraceUtils.getTraceId();
        if (e instanceof OcpException) {
            if (log.isDebugEnabled()) {
                log.debug("API Error: [{} {} client={}, traceId={}, duration={} ms, errorMsg={}]",
                        request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), traceId, duration,
                        e.getMessage(), e);
            } else {
                log.warn("API Error: [{} {} client={}, traceId={}, duration={} ms, errorMsg={}]",
                        request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), traceId, duration,
                        e.getMessage());
            }
        } else {
            log.error("Unexpected API Error: [{} {} client={}, traceId={}, duration={} ms]",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), traceId, duration, e);
        }
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private String getArgs(JoinPoint joinPoint) {
        if (joinPoint.getArgs() == null) {
            return null;
        }
        StringBuilder argsStr = new StringBuilder();
        for (Object object : joinPoint.getArgs()) {
            if (object instanceof MultipartFile ||
                    object instanceof HttpServletRequest ||
                    object instanceof HttpServletResponse ||
                    object == null) {
                continue;
            }
            argsStr.append(object).append(ARGS_SPLITTER);
        }
        return LogContentUtils.maskPasswordValue(argsStr.toString());
    }

}
