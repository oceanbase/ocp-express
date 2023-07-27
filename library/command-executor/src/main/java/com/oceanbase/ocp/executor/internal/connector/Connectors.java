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

package com.oceanbase.ocp.executor.internal.connector;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.executor.config.Configuration;
import com.oceanbase.ocp.executor.internal.connector.impl.DefaultAgentConnector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Connectors {

    private static ExecutorService ES = null;

    private static final Function<Configuration, ExecutorService> EXECUTOR_SERVICE_FUNC = c -> {
        log.info("init global executor service.");
        return new ThreadPoolExecutor(
                c.getHttpThreadCorePoolSize(),
                c.getHttpThreadMaximumPoolSize(),
                c.getHttpThreadKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new AgentClientThreadFactory());
    };

    private static final Function<Configuration, ConnectorHolder> CONNECTOR_HOLDER_FC = c -> {
        log.info("init global connector holder.");
        return new ConnectorHolder(c.getConnectorCacheMaxSize(), c.getConnectorCacheMaxIdleSeconds());
    };

    private Connectors() {}

    public static Connector<?> getAgentConnector(ConnectProperties connectProperties, Configuration configuration) {
        ES = EXECUTOR_SERVICE_FUNC.apply(configuration);
        return getConnector(ConnectorKey.http(connectProperties), configuration,
                () -> new DefaultAgentConnector(ES, connectProperties, configuration));
    }

    private static Connector<?> getConnector(ConnectorKey connectorKey, Configuration configuration,
            Supplier<Connector<?>> supplier) {
        ConnectorHolder connectorHolder = CONNECTOR_HOLDER_FC.apply(configuration);
        synchronized (Mutex.get(connectorKey)) {
            Connector<?> connector = connectorHolder.get(connectorKey);
            if (connector == null) {
                connector = supplier.get();
                connector.init();
                connector = connectorHolder.put(connector);
            }
            return connector;
        }
    }

    private static class AgentClientThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public AgentClientThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable runnable) {
            String namePrefix = "pool-command-executor-thread-";
            Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    private static class Mutex {

        private static final Map<ConnectorKey, Object> MUTEX_MAP = new WeakHashMap<>(16);

        public synchronized static Object get(ConnectorKey key) {
            return MUTEX_MAP.compute(key, (k, v) -> v == null ? new Object() : v);
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (ES != null) {
                ExecutorUtils.shutdown(ES, 3);
                log.warn("[Connectors]: shutdown executor service.");
            }
        }));
    }

}
