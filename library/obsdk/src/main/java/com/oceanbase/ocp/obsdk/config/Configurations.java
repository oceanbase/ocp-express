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

package com.oceanbase.ocp.obsdk.config;

import com.oceanbase.ocp.obsdk.connector.ObConnectors;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Configurations {

    private static long cleanPeriod = 300;
    private static boolean sqlLogEnabled = true;
    private static long slowQueryThresholdMillis = 1000L;
    private static long operationGlobalTimeoutMillis = 300_000L;

    // Suppresses default constructor, ensuring non-instantiability.
    private Configurations() {}

    public static void setCleanPeriod(long seconds) {
        cleanPeriod = ObSdkUtils.restrictValue(30, 1800, seconds);
        log.info("[obsdk] set cleanPeriod={}", cleanPeriod);
    }

    public static long getCleanPeriod() {
        return cleanPeriod;
    }

    public static void setSqlLogEnabled(boolean enabled) {
        sqlLogEnabled = enabled;
        log.info("[obsdk] set sqlLogEnabled={}", sqlLogEnabled);
    }

    public static boolean isSqlLogEnabled() {
        return sqlLogEnabled;
    }

    public static void setSlowQueryThresholdMillis(long thresholdMillis) {
        slowQueryThresholdMillis = thresholdMillis;
        log.info("[obsdk] set slowQueryThresholdMillis={}", slowQueryThresholdMillis);
    }

    public static long getSlowQueryThresholdMillis() {
        return slowQueryThresholdMillis;
    }

    public static void setOperationGlobalTimeoutMillis(long timoutMillis) {
        operationGlobalTimeoutMillis = ObSdkUtils.restrictValue(10_000, 7200_000, timoutMillis);
        log.info("[obsdk] set ddlGlobalTimeoutMillis={}", operationGlobalTimeoutMillis);
    }

    public static long getOperationGlobalTimeoutMillis() {
        return operationGlobalTimeoutMillis;
    }

    public static void setHolderCapacity(long capacity) {
        ObConnectors.setHolderCapacity(capacity);
    }

    public static long getHolderCapacity() {
        return ObConnectors.getHolderCapacity();
    }

    public static void setMaxIdleSeconds(long seconds) {
        ObConnectors.setMaxIdleSeconds(seconds);
    }

    public static long getMaxIdleSeconds() {
        return ObConnectors.getMaxIdleSeconds();
    }

    public static void setInitTimeoutMillis(long timeoutMillis) {
        ObConnectors.setInitTimeoutMillis(timeoutMillis);
    }

    public static long getInitTimeoutMillis() {
        return ObConnectors.getInitTimeoutMillis();
    }

    public static void setSocketConnectTimeoutMillis(long timeoutMillis) {
        ObConnectors.setSocketConnectTimeout(timeoutMillis);
    }

    public static void setSocketReadTimeoutMillis(long timeoutMillis) {
        ObConnectors.setSocketReadTimeout(timeoutMillis);
    }

    public static synchronized void setConnectorInitThreadCount(long threadCount) {
        ObConnectors.setConnectorInitThreadCount(threadCount);
    }
}
