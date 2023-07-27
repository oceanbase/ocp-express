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
package com.oceanbase.ocp.task.engine.coordinator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.engine.config.TaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.coordinator.worker.AbstractTaskWorker;
import com.oceanbase.ocp.task.engine.coordinator.worker.ReadyTaskWorker;
import com.oceanbase.ocp.task.engine.coordinator.worker.RunningTaskWorker;
import com.oceanbase.ocp.task.entity.TaskInstanceEntity;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.runtime.Template;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskCoordinator implements Closeable {

    private final ReadyTaskWorker readyTaskWorker;
    private final RunningTaskWorker runningTaskWorker;

    private ScheduledThreadPoolExecutor selectorExecutor;

    private Map<String, DistributionSummary> meterMap;
    private MeterRegistry meterRegistry;
    private boolean measurable = false;

    public TaskCoordinator(TaskCoordinatorConfig conf) {
        AbstractTaskWorker.setDefaultConcurrency(conf.getDefaultSubtaskConcurrency());
        this.readyTaskWorker = new ReadyTaskWorker(conf);
        this.runningTaskWorker = new RunningTaskWorker(conf);
    }

    public final TaskCoordinator startup() {
        log.info("Start task coordinator");
        this.selectorExecutor = new ScheduledThreadPoolExecutor(2, new OcpThreadFactory("task-coordinator"));
        selectorExecutor.scheduleWithFixedDelay(() -> this.lockAndExecute(readyTaskWorker), 60, 1, TimeUnit.SECONDS);
        selectorExecutor.scheduleWithFixedDelay(() -> this.lockAndExecute(runningTaskWorker), 60, 1, TimeUnit.SECONDS);
        return this;
    }

    public final TaskCoordinator addMeterRegistry(MeterRegistry meterRegistry) {
        if (meterRegistry != null) {
            this.meterRegistry = meterRegistry;
            this.measurable = true;
            this.meterMap = new ConcurrentHashMap<>(8);
        }
        return this;
    }

    public TaskInstanceEntity submitTask(TaskType taskType, Template template, Argument argument, String creator) {
        return readyTaskWorker.submitTask(taskType, template, argument, creator);
    }

    private void lockAndExecute(AbstractTaskWorker worker) {
        String name = worker.name();
        try {
            long start = System.currentTimeMillis();
            worker.work();
            long end = System.currentTimeMillis();
            log.debug("Lock and execute task ended, name={}, start={}, end={}, duration={}",
                    name, start, end, end - start);
            record(worker.metricName(), start, end);
        } catch (Throwable t) {
            log.warn("Schedule failed.", t);
        }
    }

    private void record(String name, long start, long end) {
        if (!measurable) {
            return;
        }
        DistributionSummary meter = meterMap.get(name);
        if (meter == null) {
            meter = DistributionSummary.builder(name).baseUnit(BaseUnits.MILLISECONDS).register(meterRegistry);
            meterMap.put(name, meter);
        }
        meter.record(end - start);
    }

    @Override
    public void close() throws IOException {
        ExecutorUtils.shutdown(selectorExecutor, 5);
    }

}
