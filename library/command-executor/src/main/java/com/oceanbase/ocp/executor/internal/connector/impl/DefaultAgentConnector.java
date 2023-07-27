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

package com.oceanbase.ocp.executor.internal.connector.impl;

import java.util.concurrent.ExecutorService;

import org.apache.http.config.SocketConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import com.oceanbase.ocp.executor.config.Configuration;
import com.oceanbase.ocp.executor.internal.auth.Authentication;
import com.oceanbase.ocp.executor.internal.auth.ExecutorAuthorizer;
import com.oceanbase.ocp.executor.internal.connector.ConnectProperties;
import com.oceanbase.ocp.executor.internal.connector.ConnectorKey;

import jakarta.ws.rs.client.Client;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultAgentConnector extends AbstractConnector<Client> {

    private final ExecutorService executorService;

    private final ConnectorKey cacheKey;

    private final Configuration configuration;

    private JerseyClient jerseyClient;

    public DefaultAgentConnector(ExecutorService executorService, ConnectProperties connectProperties,
            Configuration configuration) {
        super(connectProperties);
        this.executorService = executorService;
        this.cacheKey = ConnectorKey.http(connectProperties);
        this.configuration = configuration;
    }

    @Override
    public void init() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, configuration.getHttpConnectTimeout());
        clientConfig.property(ClientProperties.READ_TIMEOUT, configuration.getHttpReadTimeout());
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        clientConfig.executorService(executorService);
        // Connection Manager
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        connectionManager.setDefaultSocketConfig(
                SocketConfig.custom().setSoTimeout(configuration.getHttpSocketTimeout()).build());
        connectionManager.setMaxTotal(configuration.getHttpConnectionMaxPoolSize());
        connectionManager.setDefaultMaxPerRoute(configuration.getHttpConnectionMaxPerRoute());
        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);

        clientConfig.register(new CustomObjectMapperProvider());
        JerseyClient client = JerseyClientBuilder.createClient(clientConfig);

        Authentication authentication = getConnectProperties().getAuthentication();
        if (authentication != null && authentication.getHttpAuth() != null) {
            ExecutorAuthorizer.authorize(client, getConnectProperties().getAuthentication().getHttpAuth());
        }
        this.jerseyClient = client;
    }

    @Override
    public void close() {
        checkInited();
        jerseyClient.close();
    }

    @Override
    public Client connector() {
        return this.jerseyClient;
    }

    @Override
    public boolean isAlive() {
        checkInited();
        return jerseyClient != null && !jerseyClient.getExecutorService().isShutdown();
    }

    @Override
    public ConnectorKey cacheKey() {
        return cacheKey;
    }

}
