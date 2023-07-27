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

package com.oceanbase.ocp.common.util.trace;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;

public class TraceUtils {

    public static final String START_EPOCH_MILLI = "startEpochMilli";
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";

    private TraceUtils() {}

    /**
     * Add trace info to MDC context.
     */
    public static void trace() {
        trace(generateTraceId(), generateSpanId());
    }

    /**
     * Add trace info to MDC context.
     *
     * @param traceId customized trace ID
     */
    public static void trace(String traceId) {
        trace(traceId, generateSpanId());
    }

    /**
     * Add trace info to MDC context.
     *
     * @param traceId customized trace ID
     * @param spanId customized span ID
     */
    public static void trace(String traceId, String spanId) {
        MDC.put(START_EPOCH_MILLI, String.valueOf(Instant.now().toEpochMilli()));
        MDC.put(TRACE_ID, traceId);
        MDC.put(SPAN_ID, spanId);
    }

    /**
     * Init span of request. Integrate the trace context of the parent thread, and
     * add spanId to the context.
     *
     * @param traceContext parent thread trace context
     */
    public static void span(Map<String, String> traceContext) {
        if (traceContext != null) {
            MDC.setContextMap(traceContext);
        }
        MDC.put(SPAN_ID, generateSpanId());
    }

    /**
     * Get trace context from MDC.
     *
     * @return MDC context
     */
    public static Map<String, String> getTraceContext() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Clean MDC context.
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Get request duration.
     *
     * @return duration milli-second request
     */
    public static long getDuration() {
        final String startEpochMilli = MDC.get(START_EPOCH_MILLI);
        if (startEpochMilli == null) {
            return 0L;
        }
        return System.currentTimeMillis() - Long.parseLong(startEpochMilli);
    }

    /**
     * Get trace id from MDC.
     *
     * @return traceId
     */
    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null) {
            traceId = "";
        }
        return traceId;
    }

    /**
     * Set trace id to MDC context.
     *
     * @param traceId traceId
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toLowerCase();
    }

    private static String generateSpanId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toLowerCase();
    }

}
