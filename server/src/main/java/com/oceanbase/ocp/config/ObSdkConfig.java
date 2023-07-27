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

package com.oceanbase.ocp.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.property.PropertyService;
import com.oceanbase.ocp.obsdk.config.Configurations;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Getter
@Slf4j
public class ObSdkConfig {

    public static final String HOLDER_CAPACITY_KEY = "obsdk.connector.holder.capacity";
    public static final String CONNECTOR_MAX_IDLE_SECONDS_KEY = "obsdk.connector.max-idle.seconds";
    public static final String CONNECTOR_CLEAN_PERIOD_KEY = "obsdk.connector.cleanup.period.seconds";
    public static final String PRINT_SQL_KEY = "obsdk.print.sql";
    public static final String SLOW_QUERY_THRESHOLD_KEY = "obsdk.slow.query.threshold.millis";
    public static final String CONNECTOR_INIT_TIMEOUT_KEY = "obsdk.connector.init.timeout.millis";
    public static final String DDL_GLOBAL_TIMEOUT_KEY = "obsdk.operation.global.timeout.millis";
    public static final String SOCKET_CONNECT_TIMEOUT_KEY = "obsdk.socket.connect.timeout.millis";
    public static final String SOCKET_READ_TIMEOUT_KEY = "obsdk.socket.read.timeout.millis";
    public static final String CONNECTOR_INIT_EXECUTOR_THREAD_COUNT = "obsdk.connector.init.executor.thread-count";

    @Resource
    private PropertyService propertyService;

    private final Map<String, Consumer<String>> configMap;

    public ObSdkConfig() {
        configMap = new HashMap<>();
        configMap.put(HOLDER_CAPACITY_KEY,
                t -> Configurations.setHolderCapacity(parseLong(t, 100L)));
        configMap.put(CONNECTOR_MAX_IDLE_SECONDS_KEY,
                t -> Configurations.setMaxIdleSeconds(parseLong(t, 3600L)));
        configMap.put(CONNECTOR_CLEAN_PERIOD_KEY,
                t -> Configurations.setCleanPeriod(parseLong(t, 300L)));
        configMap.put(PRINT_SQL_KEY,
                t -> Configurations.setSqlLogEnabled(parseBoolean(t)));
        configMap.put(SLOW_QUERY_THRESHOLD_KEY,
                t -> Configurations.setSlowQueryThresholdMillis(parseLong(t, 1000L)));
        configMap.put(CONNECTOR_INIT_TIMEOUT_KEY,
                t -> Configurations.setInitTimeoutMillis(parseLong(t, 5000L)));
        configMap.put(DDL_GLOBAL_TIMEOUT_KEY,
                t -> Configurations.setOperationGlobalTimeoutMillis(parseLong(t, 300_000L)));
        configMap.put(SOCKET_CONNECT_TIMEOUT_KEY,
                t -> Configurations.setSocketConnectTimeoutMillis(parseLong(t, 2000L)));
        configMap.put(SOCKET_READ_TIMEOUT_KEY, t -> Configurations.setSocketReadTimeoutMillis(parseLong(t, 1800_000L)));
        configMap.put(CONNECTOR_INIT_EXECUTOR_THREAD_COUNT,
                t -> Configurations.setConnectorInitThreadCount(parseLong(t, 16L)));
    }

    private Long parseLong(String value, Long defaultVale) {
        return Optional.ofNullable(value).map(Long::parseLong).orElse(defaultVale);
    }

    private Boolean parseBoolean(String value) {
        return Optional.ofNullable(value).map(Boolean::parseBoolean).orElse(true);
    }

    @EventListener
    public void envListener(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        for (String key : keys) {
            if (configMap.containsKey(key)) {
                String property = propertyService.getProperty(key);
                log.info("[ObSdkConfig] {} has been changed to {}", key, property);
                configMap.get(key).accept(property);
            }
        }
    }

    @PostConstruct
    private void init() {
        for (Entry<String, Consumer<String>> entry : configMap.entrySet()) {
            String property = propertyService.getProperty(entry.getKey());
            log.info("[ObSdkConfig] init, {} = {}", entry.getKey(), property);
            entry.getValue().accept(property);
        }
    }
}
