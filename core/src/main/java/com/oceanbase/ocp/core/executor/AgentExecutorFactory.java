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
package com.oceanbase.ocp.core.executor;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.executor.config.Configuration;
import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.executor.internal.auth.Authentication;
import com.oceanbase.ocp.executor.internal.auth.http.HttpAuthentication;
import com.oceanbase.ocp.executor.internal.connector.ConnectProperties;
import com.oceanbase.ocp.executor.internal.constant.enums.HttpAuthType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AgentExecutorFactory {

    @Value("${ocp.operation.executor.http.thread.core-pool-size:10}")
    private int httpThreadCorePoolSize;

    @Value("${ocp.operation.executor.http.thread.max-pool-size:200}")
    private int httpThreadMaximumPoolSize;

    @Value("${ocp.operation.executor.http.thread.keep-alive-time:120000}")
    private long httpThreadKeepAliveTime;

    @Value("${ocp.operation.executor.http.socket-timeout:3000}")
    private int httpSocketTimeout;

    @Value("${ocp.operation.executor.http.connect-timeout:5000}")
    private int httpConnectTimeout;

    @Value("${ocp.operation.executor.http.read-timeout:5000}")
    private int httpReadTimeout;

    @Value("${ ocp.operation.executor.http.connection.max-pool-size:100}")
    private int httpConnectionMaxPoolSize;

    @Value("${ocp.operation.executor.http.connection.max-per-route:10}")
    private int httpConnectionMaxPerRoute;

    @Value("${ocp.operation.executor.connector.cache.max-size:1000}")
    private int connectorCacheMaxSize;

    @Value("${ocp.operation.executor.connector.cache.max-idle-seconds:3600}")
    private int connectorCacheMaxIdleSeconds;

    private static final String OCP_SERVER_IP_HEADER = "X-OCP-Server-IP";

    private static final String TRACE_ID_HEADER = "X-OCP-Trace-ID";

    private String username;

    private String password;

    private HttpAuthType authType;

    private Configuration configuration;

    @PostConstruct
    public void setup() {
        this.configuration = Configuration.builder()
                .httpThreadCorePoolSize(httpThreadCorePoolSize)
                .httpThreadMaximumPoolSize(httpThreadMaximumPoolSize)
                .httpThreadKeepAliveTime(httpThreadKeepAliveTime)
                .httpSocketTimeout(httpSocketTimeout)
                .httpConnectTimeout(httpConnectTimeout)
                .httpReadTimeout(httpReadTimeout)
                .httpConnectionMaxPoolSize(httpConnectionMaxPoolSize)
                .httpConnectionMaxPerRoute(httpConnectionMaxPerRoute)
                .connectorCacheMaxSize(connectorCacheMaxSize)
                .connectorCacheMaxIdleSeconds(connectorCacheMaxIdleSeconds)
                .build();
    }

    public void setAuthInfo(String username, String password, HttpAuthType authType) {
        log.info("Init agent executor, username={}, authType={}", username, authType);
        this.username = username;
        this.password = password;
        this.authType = authType;
    }

    private static Map<String, Object> defaultHeaders() {
        Map<String, Object> headers = new HashMap<>(2);
        headers.put(OCP_SERVER_IP_HEADER, HostUtils.getLocalIp());
        headers.put(TRACE_ID_HEADER, TraceUtils.getTraceId());
        return headers;
    }

    public AgentExecutor create(String hostAddress, int port) {
        if (username == null) {
            throw new RuntimeException("Agent username not specified.");
        }

        HttpAuthentication authentication;
        switch (authType) {
            case BASIC:
                authentication = HttpAuthentication.basic(username, password);
                break;
            case OCP_DIGEST:
                authentication = HttpAuthentication.ocpDigest(username, password);
                break;
            case SSL:
            default:
                throw new RuntimeException("Unsupported http auth type. type=" + authType);
        }
        ConnectProperties properties = ConnectProperties.builder()
                .hostAddress(hostAddress)
                .httpPort(port)
                .extHttpHeaders(defaultHeaders())
                .authentication(Authentication.builder()
                        .httpAuth(authentication)
                        .build())
                .build();
        return new AgentExecutor(properties, configuration);
    }

}
