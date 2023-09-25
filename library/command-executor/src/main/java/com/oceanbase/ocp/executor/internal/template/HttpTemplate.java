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

package com.oceanbase.ocp.executor.internal.template;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;

import org.glassfish.jersey.client.ClientProperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.reflect.TypeToken;

import com.oceanbase.ocp.common.util.time.TimeUtils;
import com.oceanbase.ocp.executor.internal.auth.http.DigestAuthConfig;
import com.oceanbase.ocp.executor.internal.util.DigestSignature;
import com.oceanbase.ocp.executor.config.Configuration;
import com.oceanbase.ocp.executor.exception.HttpRequestFailedException;
import com.oceanbase.ocp.executor.internal.connector.ConnectProperties;
import com.oceanbase.ocp.executor.internal.connector.Connector;
import com.oceanbase.ocp.executor.internal.connector.Connectors;
import com.oceanbase.ocp.executor.internal.constant.enums.HttpAuthType;
import com.oceanbase.ocp.executor.internal.util.LogMaskUtils;
import com.oceanbase.ocp.executor.model.monitor.HttpConfig;
import com.oceanbase.ocp.executor.model.response.AgentResponse;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class HttpTemplate {

    /**
     * <protocol>://<ip>:<port>
     */
    private final String domain;

    private final ConnectProperties connectProperties;

    private final Connector<Client> agentConnector;

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    /** OCP Trace ID */
    private static final String TRACE_ID_HEADER = "X-OCP-Trace-ID";

    @SuppressWarnings("unchecked")
    public HttpTemplate(ConnectProperties connectProperties, Configuration configuration) {
        this.connectProperties = connectProperties;
        this.agentConnector = (Connector<Client>) Connectors.getAgentConnector(connectProperties, configuration);
        HttpAuthType authType = connectProperties.getAuthentication().getHttpAuth().getAuthType();
        domain = String.format("%s://%s:%d", getProtocol(authType), connectProperties.getHostAddress(),
                connectProperties.getHttpPort());
    }

    public <T> AgentResponse<T> get(String apiPath, Class<T> responseClass) {
        return doGet(apiPath, responseClass);
    }

    public <T> AgentResponse<T> post(String apiPath, Class<T> responseClass, Object requestBody) {
        return doPost(apiPath, responseClass, requestBody);
    }

    public Response postRaw(String apiPath, Object requestBody, HttpConfig config) {
        return doPostRaw(apiPath, requestBody, config);
    }

    private <T> AgentResponse<T> doGet(String apiPath, Class<T> responseClass) {
        log.info("GET request to agent, url:{} ", domain + apiPath);
        WebTarget target = getClient().target(domain + apiPath);
        Builder invoker =
                decorateHeaders(target.request(APPLICATION_JSON), "GET", apiPath);
        Type type = getResponseDataType(responseClass);
        AgentResponse<T> response = safeExecute(() -> (invoker.get(new GenericType<>(type))));
        checkSuccess(response);
        return response;
    }

    private <T> AgentResponse<T> doPost(String apiPath, Class<T> responseClass, Object requestBody) {
        log.info("POST request to agent, url:{}, request body:{}", domain + apiPath, safeToString(requestBody));
        WebTarget target = getClient().target(domain + apiPath);
        Builder invoker =
                decorateHeaders(target.request(APPLICATION_JSON), "POST", apiPath);
        Type type = getResponseDataType(responseClass);
        AgentResponse<T> response =
                safeExecute(() -> invoker.post(Entity.entity(requestBody, APPLICATION_JSON), new GenericType<>(type)));
        checkSuccess(response);
        return response;
    }

    private Response doPostRaw(String apiPath, Object requestBody, HttpConfig config) {
        log.info("POST request to agent, url:{}, request body:{}", domain + apiPath, safeToString(requestBody));
        WebTarget target = getClient().target(domain + apiPath);
        Builder invoker =
                decorateHeaders(target.request(APPLICATION_JSON), "POST", apiPath);
        if (config != null) {
            if (config.getConnectTimeout() != null && config.getConnectTimeout() > 0) {
                invoker.property(ClientProperties.CONNECT_TIMEOUT, config.getConnectTimeout());
            }
            if (config.getReadTimeout() != null && config.getReadTimeout() > 0) {
                invoker.property(ClientProperties.READ_TIMEOUT, config.getReadTimeout());
            }
        }
        return invoker.post(Entity.entity(requestBody, APPLICATION_JSON));
    }

    private Client getClient() {
        return agentConnector.connector();
    }

    private Builder decorateHeaders(Builder invokeBuilder, String method, String url) {
        Map<String, Object> extHttpHeaders = connectProperties.getExtHttpHeaders();
        if (extHttpHeaders != null && !extHttpHeaders.isEmpty()) {
            extHttpHeaders.forEach(invokeBuilder::header);
        }
        if (connectProperties.getAuthentication().getHttpAuth().getAuthType().equals(HttpAuthType.DIGEST)) {
            String date = TimeUtils.getCurrentDate("yyyy/MM/dd HH:mm:ss");
            String traceId = connectProperties.getExtHttpHeaders().get(TRACE_ID_HEADER).toString();
            String username = connectProperties.getAuthentication().getHttpAuth().getDigestAuthConfig().getUsername();
            String password = connectProperties.getAuthentication().getHttpAuth().getDigestAuthConfig().getPassword();
            DigestAuthConfig digestAuth = DigestAuthConfig.builder().username(username).password(password).build();
            DigestSignature digestSignature = DigestSignature.builder().method(method).url(url)
                    .contentType(APPLICATION_JSON).traceId(traceId).authConfig(digestAuth).date(date).build();
            String authorization = digestSignature.getAuthorizationHeader();
            invokeBuilder.header("Date", date).header("Authorization", authorization).header("Content-Type",
                    APPLICATION_JSON);
        }
        return invokeBuilder;
    }

    private static <T> Type getResponseDataType(Class<T> dataClass) {
        if (dataClass == null) {
            return new TypeToken<AgentResponse<?>>() {}.getType();
        } else {
            return TypeToken.getParameterized(AgentResponse.class, dataClass).getType();
        }
    }

    private static String getProtocol(HttpAuthType authType) {
        if (authType == HttpAuthType.SSL) {
            return "https";
        }
        return "http";
    }

    private static <T> void checkSuccess(AgentResponse<T> response) {
        if (response == null || !response.getSuccessful() || (response.getError()) != null) {
            throw new HttpRequestFailedException("[AgentClient]:http request is failed, response:" + response);
        }
    }

    private static String safeToString(Object object) {
        return object == null ? null : LogMaskUtils.mask(object.toString());
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    static <T> AgentResponse<T> safeExecute(Supplier<AgentResponse<T>> supplier) {
        try {
            return supplier.get();
        } catch (WebApplicationException ex) {
            try {
                Response response = ex.getResponse();
                return response.readEntity(AgentResponse.class);
            } catch (Exception ex2) {
                throw ex;
            }
        }
    }

}
