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
package com.oceanbase.ocp.monitor.helper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.monitor.config.AsyncHttpConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExporterRequestHelper {

    /** OCP Trace ID */
    private static final String TRACE_ID_HEADER = "X-OCP-Trace-ID";

    private static final long SLOW_THRESHOLD = 100L;

    private final AsyncHttpClient client;
    private String authVal;

    public ExporterRequestHelper() {
        HttpContext context = new HttpContext(1000, 1000, 2000);

        AsyncHttpConfig config = AsyncHttpConfig.builder()
                .requestTimeout(context.getRequestTimeout())
                .connectTimeout(context.getConnectTimeout())
                .readTimeout(context.getReadTimeout())
                .build();
        client = newHttpClient(config);
    }

    public void setBasicAuth(String username, String password) {
        if (username != null && password != null) {
            this.authVal =
                    Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        }
    }

    public ListenableFuture<Response> get(String exporterUrl) {
        long start = System.currentTimeMillis();
        ListenableFuture<Response> whenResponse = buildRequest(exporterUrl).execute();
        long end = System.currentTimeMillis();
        if (end - start > SLOW_THRESHOLD) {
            log.info("Get http client too slow, url={}, start={}, elapsed={}",
                    exporterUrl, start, end - start);
        }
        return whenResponse;
    }

    private BoundRequestBuilder buildRequest(String exporterUrl) {
        return client
                .prepareGet(exporterUrl)
                .addHeader("Authorization", "Basic " + authVal)
                .addHeader(TRACE_ID_HEADER, TraceUtils.getTraceId());
    }

    private AsyncHttpClient newHttpClient(AsyncHttpConfig config) {
        Builder configBuilder = Dsl.config()
                .setConnectTimeout(config.getConnectTimeout())
                .setReadTimeout(config.getReadTimeout())
                .setRequestTimeout(config.getRequestTimeout())
                .setThreadFactory(new AsyncHttpThreadFactory())
                .setMaxRequestRetry(0);
        return Dsl.asyncHttpClient(configBuilder.build());
    }

    private static class AsyncHttpThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public AsyncHttpThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable runnable) {
            String namePrefix = "pool-async-http-thread-";
            Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    @Data
    @lombok.Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class HttpContext {

        private int connectTimeout;

        private int readTimeout;

        private int requestTimeout;

    }
}
