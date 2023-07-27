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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.coordinator.worker.subtask.AbstractSubtaskWorker;
import com.oceanbase.ocp.task.engine.coordinator.worker.subtask.CancelingSubtaskWorker;
import com.oceanbase.ocp.task.engine.coordinator.worker.subtask.FailedSubtaskWorker;
import com.oceanbase.ocp.task.engine.coordinator.worker.subtask.ReadySubtaskWorker;
import com.oceanbase.ocp.task.engine.coordinator.worker.subtask.RunningSubtaskWorker;
import com.oceanbase.ocp.task.engine.runner.RunnerFactory;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubtaskCoordinator implements Closeable {

    private final ReadySubtaskWorker readySubtaskWorker;
    private final RunningSubtaskWorker runningSubtaskWorker;
    private final CancelingSubtaskWorker cancelingSubtaskWorker;
    private final FailedSubtaskWorker failedSubtaskWorker;

    private final SubtaskCoordinatorConfig config;

    private ScheduledThreadPoolExecutor selectorExecutor;
    private ThreadPoolExecutor runnerExecutor;
    private ThreadPoolExecutor manualRunnerExecutor;

    private Map<String, DistributionSummary> meterMap;
    private MeterRegistry meterRegistry;
    private boolean measurable = false;

    public SubtaskCoordinator(SubtaskCoordinatorConfig conf) {
        this.config = conf;
        RunnerFactory runnerFactory = new RunnerFactory(conf);

        this.readySubtaskWorker = new ReadySubtaskWorker(conf, runnerFactory);
        this.cancelingSubtaskWorker = new CancelingSubtaskWorker(conf);
        this.runningSubtaskWorker = new RunningSubtaskWorker(conf, runnerFactory);
        this.failedSubtaskWorker = new FailedSubtaskWorker(conf);
    }

    public final SubtaskCoordinator startup() {
        log.info("Start subtask coordinator");
        this.selectorExecutor = new ScheduledThreadPoolExecutor(4, new OcpThreadFactory("subtask-coordinator"));
        selectorExecutor.scheduleWithFixedDelay(() -> this.execute(readySubtaskWorker), 60, 1, TimeUnit.SECONDS);
        selectorExecutor.scheduleWithFixedDelay(() -> this.execute(runningSubtaskWorker), 60, 1, TimeUnit.SECONDS);
        selectorExecutor.scheduleWithFixedDelay(() -> this.execute(cancelingSubtaskWorker), 60, 1, TimeUnit.SECONDS);
        selectorExecutor.scheduleWithFixedDelay(() -> this.execute(failedSubtaskWorker), 60, 1, TimeUnit.SECONDS);

        this.runnerExecutor =
                new ThreadPoolExecutor(config.getSubtaskExecutorCorePoolSize(), config.getSubtaskExecutorMaxPoolSize(),
                        120L, TimeUnit.SECONDS,
                        new SynchronousQueue<>(), new OcpThreadFactory("subtask-executor"));
        this.manualRunnerExecutor = new ThreadPoolExecutor(config.getManualSubtaskExecutorCorePoolSize(),
                config.getSubtaskExecutorMaxPoolSize(), 120L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new OcpThreadFactory("manual-subtask-executor"));

        AbstractSubtaskWorker.setRunnerExecutor(runnerExecutor);
        AbstractSubtaskWorker.setManualRunnerExecutor(manualRunnerExecutor);
        return this;
    }

    public SubtaskCoordinator addMeterRegistry(MeterRegistry meterRegistry) {
        if (meterRegistry != null) {
            this.meterRegistry = meterRegistry;
            this.measurable = true;
            this.meterMap = new ConcurrentHashMap<>(8);
        }
        return this;
    }

    private void execute(AbstractSubtaskWorker worker) {
        long start = System.currentTimeMillis();
        try {
            worker.work();
            String name = worker.name();
            long end = System.currentTimeMillis();
            log.debug("Schedule subtask ended, name={}, duration={}", name, end - start);
            record(worker.metricName(), start, end);
        } catch (Throwable t) {
            log.info("Worker schedule failed, name={}", worker.name());
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
        ExecutorUtils.shutdown(runnerExecutor, 5);
        ExecutorUtils.shutdown(manualRunnerExecutor, 5);
    }

}
