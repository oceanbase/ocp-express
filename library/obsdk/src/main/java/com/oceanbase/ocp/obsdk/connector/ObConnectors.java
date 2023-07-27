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

package com.oceanbase.ocp.obsdk.connector;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.common.pattern.Lazy;
import com.oceanbase.ocp.common.util.trace.TraceDecorator;
import com.oceanbase.ocp.obsdk.config.DataSourceConfig;
import com.oceanbase.ocp.obsdk.connector.impl.DefaultConnector;
import com.oceanbase.ocp.obsdk.exception.ConnectorInitFailedException;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ObConnectors {



    private static long initTimeoutMillis = 5000;


    private static long socketConnectTimeoutMillis = 2000;


    private static long socketReadTimeoutMillis = 1800000;


    private static int initExecutorThreadCount = 16;


    private static ObConnectorHolder holder = new ObConnectorHolder();

    private static Lazy<ExecutorService> initExecutor =
            Lazy.of(() -> Executors.newFixedThreadPool(initExecutorThreadCount, new InitThreadFactory()));

    static class InitThreadFactory implements ThreadFactory {

        private static AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        InitThreadFactory() {
            namePrefix = "obsdk-init-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }


    private ObConnectors() {}

    public static ObConnector getObConnector(ConnectProperties connectProperties) {
        validate(connectProperties);

        synchronized (Mutex.get(connectProperties)) {
            ObConnector obConnector = holder.getObConnector(connectProperties);
            if (obConnector != null) {
                printServerInfo(obConnector.key());
                return obConnector;
            }

            obConnector = createObConnector(connectProperties);
            holder.put(obConnector);
            printServerInfo(obConnector.key());
            return obConnector;
        }
    }

    public static void invalidate(String clusterName) {
        holder.invalidate(clusterName);
    }

    public static void invalidate(String clusterName, String tenantName, String username) {
        holder.invalidate(clusterName, tenantName, username);
    }

    public static void clearAll() {
        holder.clearAll();
    }

    public static void cleanUp() {
        holder.cleanUp();
    }

    public static synchronized void setHolderCapacity(long capacity) {
        long restrictValue = ObSdkUtils.restrictValue(10, 200, capacity);
        holder.setCapacity(restrictValue);
        log.info("[obsdk] set cacheSize={}", restrictValue);
    }

    public static synchronized void setSocketConnectTimeout(long timeoutMillis) {
        ObConnectors.socketConnectTimeoutMillis = ObSdkUtils.restrictValue(1000, 30_000, timeoutMillis);
        log.info("[obsdk] set socketConnectTimeoutMillis={}", socketConnectTimeoutMillis);
    }

    public static synchronized void setSocketReadTimeout(long timeoutMillis) {
        ObConnectors.socketReadTimeoutMillis = ObSdkUtils.restrictValue(1000, 3600_000, timeoutMillis);
        log.info("[obsdk] set socketReadTimeoutMillis={}", socketReadTimeoutMillis);
    }

    public static synchronized void setConnectorInitThreadCount(long threadCount) {
        ObConnectors.initExecutorThreadCount = (int) ObSdkUtils.restrictValue(4, 256, threadCount);
        log.info("[obsdk] set socketInitThreadCount={}", threadCount);
    }

    public static long getHolderCapacity() {
        return holder.getCapacity();
    }

    public static void setMaxIdleSeconds(long seconds) {

        long restrictValue = ObSdkUtils.restrictValue(5 * 60, 5 * 60 * 60, seconds);
        holder.setMaxIdleSeconds(restrictValue);
        log.info("[obsdk] set maxIdleSeconds={}", restrictValue);
    }

    public static long getMaxIdleSeconds() {
        return holder.getMaxIdleSeconds();
    }

    public static void setInitTimeoutMillis(long timeoutMillis) {
        initTimeoutMillis = ObSdkUtils.restrictValue(1000, 10_000, timeoutMillis);
        log.info("[obsdk] set initTimeoutMillis={}", initTimeoutMillis);
    }

    public static long getInitTimeoutMillis() {
        return initTimeoutMillis;
    }

    private static ObConnector createObConnector(ConnectProperties connectProperties) {
        log.info("[obsdk] create new ob connector, connectProperties={}", connectProperties);

        if (ConnectionMode.DIRECT.equals(connectProperties.getConnectionMode())
                && CollectionUtils.isNotEmpty(connectProperties.getObsAddrList())) {
            ConnectorInitFailedException exception = null;
            for (ObServerAddr addr : connectProperties.getObsAddrList()) {
                ConnectProperties newConnectProperties =
                        connectProperties.withAddress(addr.getAddress()).withPort(addr.getPort());
                try {
                    return newObConnector(newConnectProperties);
                } catch (ConnectorInitFailedException e) {
                    log.warn("[obsdk] try to create ob connector of 'direct' mode failed, observer addr:{}, message:{}",
                            addr, e.getMessage());
                    exception = e;
                }
            }
            assert exception != null;
            log.error("[obsdk] create ob connector of 'direct' mode failed, message:{}", exception.getMessage());
            throw exception;
        } else {

            return newObConnector(connectProperties);
        }
    }

    private static ObConnector newObConnector(ConnectProperties connectProperties) {
        ObConnector obConnector = new DefaultConnector(connectProperties);
        FutureTask<Boolean> futureTask = new FutureTask<>(new TraceDecorator().decorate(() -> {
            obConnector.init(DataSourceConfig.builder().socketConnectTimeout(socketConnectTimeoutMillis)
                    .socketReadTimeout(socketReadTimeoutMillis).build());
            return true;
        }));
        initExecutor.get().execute(futureTask);
        try {
            futureTask.get(initTimeoutMillis, TimeUnit.MILLISECONDS);
            log.info("[obsdk] create new ob connector success");
            return obConnector;
        } catch (ExecutionException e) {
            futureTask.cancel(true);
            String message = String.format("[obsdk] init ob connector failed, connectProperties=%s, cause:%s",
                    connectProperties, e.getMessage());
            log.error(message);
            throw new ConnectorInitFailedException(connectProperties, message, e);
        } catch (InterruptedException | TimeoutException e) {
            futureTask.cancel(true);
            obConnector.close();
            String message = String.format(
                    "[obsdk] init ob connector timeout, connectProperties=%s, initTimeoutMillis=%d",
                    connectProperties, initTimeoutMillis);
            log.error(message);
            throw new ConnectorInitFailedException(connectProperties, message, e);
        }
    }

    private static void validate(ConnectProperties connectProperties) {
        Validate.notNull(connectProperties, "[obsdk] the input connectProperties is null.");
        connectProperties.validate();
    }

    private static void printServerInfo(ObConnectorKey connectorKey) {

        log.info("[obsdk]:connected server ip:{}, sql port:{}", connectorKey.getAddress(), connectorKey.getPort());
    }
}
