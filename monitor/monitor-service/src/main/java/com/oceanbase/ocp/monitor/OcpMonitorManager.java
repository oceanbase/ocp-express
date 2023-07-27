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
package com.oceanbase.ocp.monitor;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.common.util.trace.TraceDecorator;
import com.oceanbase.ocp.monitor.meter.DistributionSummaryGroup;
import com.oceanbase.ocp.monitor.model.exporter.ExporterAddress;
import com.oceanbase.ocp.monitor.service.OcpCacheStatServiceImpl;
import com.oceanbase.ocp.monitor.service.SeriesIdKeyService;
import com.oceanbase.ocp.monitor.util.MonitorPropertyUtils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OcpMonitorManager {

    private final ScheduledExecutorService secondCollectExecutor =
            ExecutorUtils.newScheduledPool(1, "prometheus-second-schedule-");

    private final ScheduledExecutorService exporterCheckExecutor =
            ExecutorUtils.newScheduledPool(1, "prometheus-exporter-check");

    private final ScheduledExecutorService cacheMonitorExecutor =
            ExecutorUtils.newScheduledPool(1, "prometheus-cache-monitor");

    @Autowired
    private SeriesIdKeyService seriesIdKeyService;

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private OcpMetricCollectService collectService;

    @Autowired
    private OcpCacheStatServiceImpl monitorService;

    @Autowired
    private MonitorProperties monitorProperties;

    @Autowired
    private MeterRegistry meterRegistry;

    private DistributionSummaryGroup collectScheduledDuration;

    private Counter scheduledExporterCounter;

    public OcpMonitorManager() {}

    @PreDestroy
    public void onDestroy() {
        log.info("monitor manager destroy...");
        ExecutorUtils.shutdown(secondCollectExecutor, 1);
        ExecutorUtils.shutdown(exporterCheckExecutor, 1);
        ExecutorUtils.shutdown(cacheMonitorExecutor, 1);
        log.info("monitor manager destroyed");
    }

    public void init() {
        log.info("OcpMonitorManager init start...");
        initScheduleTask();

        initMetrics();
        log.info("OcpMonitorManager init success.");
    }

    private void initScheduleTask() {
        secondCollectExecutor.scheduleAtFixedRate(new TraceDecorator().decorate(new CollectSecondTask()),
                10, 1, TimeUnit.SECONDS);

        cacheMonitorExecutor.scheduleAtFixedRate(
                new TraceDecorator().decorate(new RunOnceTask("cacheMonitor", () -> monitorService.printCacheStat())),
                60, 60, TimeUnit.SECONDS);

        exporterCheckExecutor.scheduleAtFixedRate(
                new TraceDecorator().decorate(
                        new RunOnceTask("checkInactiveExporters", () -> exporterService.validateInactiveExporters())),
                65, monitorProperties.getInactiveValidateIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void initMetrics() {
        collectScheduledDuration = DistributionSummaryGroup.builder("ocp_monitor_collect_scheduled_duration")
                .description("OCP monitor collect schedule duration")
                .baseUnits(BaseUnits.MILLISECONDS)
                .labelNames("type")
                .build(meterRegistry);
        scheduledExporterCounter = Counter.builder("ocp_monitor_collect_scheduled_exporter_count")
                .description("OCP collect schedule exporter count")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 120_000)
    private void printMetricEngineStats() {
        try {
            monitorService.printCacheStat();
        } catch (Throwable throwable) {
            log.error("Print cache stats failed.", throwable);
        }
    }

    @Scheduled(fixedDelay = 60_000)
    private void syncSeriesKeyId() {
        try {
            seriesIdKeyService.syncKeyId();
        } catch (Throwable throwable) {
            log.error("Sync series key id failed.", throwable);
        }
    }

    class CollectSecondTask implements Runnable {

        private long nextLogTimeMillis = System.currentTimeMillis();
        private long lastTaskTime = -1;

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            int collectTaskCount = 0;

            if (lastTaskTime > 0 && now - lastTaskTime < 500) {
                log.info("Second collect drop timeout requests.");
                lastTaskTime = now;
                return;
            }

            try {
                List<ExporterAddress> exporters = exporterService.getAllActiveExporters();
                if (CollectionUtils.isEmpty(exporters)) {
                    return;
                }

                long collectAt = now / 1000;
                if (collectAt % MonitorPropertyUtils.getStandardSecondCollectInterval() != 0) {
                    return;
                }

                log.debug(" Collect exporter, collectAt = {}", collectAt);
                for (ExporterAddress addr : exporters) {
                    try {
                        scheduledExporterCounter.increment(1);
                        collectService.collect(addr, collectAt);
                    } catch (Exception e) {
                        log.error("Collect single exporter failed, exporter address = " + addr.getExporterUrl(), e);
                    }
                    collectTaskCount++;
                }
                if (now >= nextLogTimeMillis) {
                    log.info("create pull exporter, exporterCount={}, collectTaskCount={}",
                            exporters.size(), collectTaskCount);
                    long logIntervalMillis = 60 * 1000L;
                    nextLogTimeMillis += logIntervalMillis;
                }
            } catch (Throwable t) {
                log.error("collect schedule failed ", t);
            } finally {
                if (System.currentTimeMillis() - now > 200) {
                    log.info("Collect schedule too slow, from={}, count={}, elapsed={}",
                            now, collectTaskCount, System.currentTimeMillis() - now);
                }
                collectScheduledDuration.time(1, now);
            }
        }
    }

    class RunOnceTask implements Runnable {

        private final String taskName;
        private final Runnable runnable;

        public RunOnceTask(String taskName, Runnable runnable) {
            this.taskName = taskName;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Throwable t) {
                log.error(taskName + " failed: ", t);
            }
        }

    }

}
