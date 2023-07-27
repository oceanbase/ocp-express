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
package com.oceanbase.ocp.common.util;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutorUtils {

    public static void shutdown(ExecutorService executorService, long timeoutSeconds) {
        if (Objects.isNull(executorService)) {
            return;
        }
        executorService.shutdown();
        log.debug("shutdown signal received, terminating...");
        try {
            if (executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                log.debug("Executor terminated success");
            } else {
                log.warn("terminate failed, forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("terminate failed, message={}", e.getMessage());
        }
    }

    public static ScheduledThreadPoolExecutor newScheduledPool(int poolSize, String threadFactoryName) {
        return new ScheduledThreadPoolExecutor(poolSize, new OcpThreadFactory(threadFactoryName));
    }

}
