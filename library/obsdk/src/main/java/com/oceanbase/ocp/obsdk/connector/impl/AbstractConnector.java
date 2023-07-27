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

package com.oceanbase.ocp.obsdk.connector.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;

import com.oceanbase.ocp.obsdk.config.DataSourceConfig;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ConnectionMode;
import com.oceanbase.ocp.obsdk.connector.JdbcSocksProxy;
import com.oceanbase.ocp.obsdk.connector.ObConnector;
import com.oceanbase.ocp.obsdk.connector.ObConnectorKey;
import com.oceanbase.ocp.obsdk.connector.ObValidConnectionChecker;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractConnector implements ObConnector {

    private static final String VALID_QUERY_SQL = "SELECT 1 FROM DUAL";

    private static final int VALID_QUERT_TIMEOUT_SECONDS = 2;

    protected ConnectProperties connectProperties;

    private ObConnectorKey key;

    private DruidDataSource dataSource;

    AbstractConnector(ConnectProperties connectProperties) {
        this.connectProperties = connectProperties;
        this.key = ObConnectorKey.of(connectProperties);
    }

    protected abstract String getUrl();

    protected abstract String getDriverClassName();

    @Override
    public void init(DataSourceConfig config) throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setName("ob-connector-datasource_" + datasourceName());
        dataSource.setValidationQuery(VALID_QUERY_SQL);
        dataSource.setValidationQueryTimeout(VALID_QUERT_TIMEOUT_SECONDS);
        dataSource.setUsername(connectProperties.getFullUsername());
        dataSource.setPassword(connectProperties.getPassword());
        dataSource.setDriverClassName(getDriverClassName());
        dataSource.setUrl(getUrl());

        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(10);

        dataSource.setMaxWait(2000);

        dataSource.setTimeBetweenEvictionRunsMillis(60_000);

        dataSource.setMinEvictableIdleTimeMillis(300_000);
        dataSource.setValidConnectionChecker(new ObValidConnectionChecker());

        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        dataSource.setConnectionErrorRetryAttempts(2);
        dataSource.setBreakAfterAcquireFailure(true);
        Properties properties = new Properties();
        properties.setProperty("connectTimeout", String.valueOf(config.getSocketConnectTimeout()));
        properties.setProperty("socketTimeout", String.valueOf(config.getSocketReadTimeout()));

        JdbcSocksProxy proxy = connectProperties.getProxy();
        if (proxy != null) {
            properties.setProperty("socksProxyHost", proxy.getProxyAddress());
            properties.setProperty("socksProxyPort", String.valueOf(proxy.getProxyPort()));
        }
        dataSource.setConnectProperties(properties);
        try {
            dataSource.init();
        } catch (Exception e) {

            tryCloseDataSource(dataSource);
            log.error("[obsdk] init druid datasource failed. connectProperties={}, error message:{}", connectProperties,
                    e.getMessage());
            throw e;
        }
        log.info("[obsdk] init druid datasource success, poolingCount={}, connectProperties={}",
                dataSource.getPoolingCount(), connectProperties);
        this.dataSource = dataSource;
    }

    @Override
    public void close() {
        JdbcUtils.close(this.dataSource);
        this.dataSource = null;
    }

    @Override
    public DataSource dataSource() {
        return this.dataSource;
    }

    @Override
    public boolean isAlive() {
        if (this.dataSource == null) {
            return false;
        }
        if (this.dataSource.isClosed()) {
            return false;
        }


        Connection conn = null; // NOPMD
        try {
            conn = this.dataSource.getConnection();
            return ObSdkUtils.checkConnection(conn, VALID_QUERY_SQL, VALID_QUERT_TIMEOUT_SECONDS);
        } catch (Exception e) {
            log.warn("[obsdk] ob connector is not alive, get connection failed, connectProperties={}, error:{}",
                    connectProperties, e.getMessage());
            return false;
        } finally {
            JdbcUtils.close(conn);
        }
    }

    @Override
    public boolean isActive() {
        if (this.dataSource == null) {
            return false;
        }
        if (this.dataSource.isClosed()) {
            return false;
        }
        return this.dataSource.getActiveCount() > 0;
    }

    @Override
    public ObConnectorKey key() {
        return this.key;
    }

    private String datasourceName() {
        String extraInfo;
        if (ConnectionMode.DIRECT.equals(key.getConnectionMode())) {
            extraInfo = key.getTenantName();
        } else {
            extraInfo = String.join("_", key.getClusterName(),
                    String.valueOf(key.getObClusterId()), key.getTenantName());
        }
        return String.join("_", key.getConnectionMode().toString(),
                extraInfo, key.getUsername(), key.getAddress(), String.valueOf(key.getPort()));
    }


    private void tryCloseDataSource(DruidDataSource dataSource) {
        try {
            dataSource.close();
        } catch (Exception ex) {
            log.warn("failed to close dataSource, errMsg:{}, cause:{}", ex.getMessage(), ex.getCause());
        }
    }
}
