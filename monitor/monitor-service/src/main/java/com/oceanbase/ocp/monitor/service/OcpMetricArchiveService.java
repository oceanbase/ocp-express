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

package com.oceanbase.ocp.monitor.service;

import static com.oceanbase.ocp.monitor.constants.MonitorConstants.VALUE_NOT_EXIST;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.monitor.meter.CounterGroup;
import com.oceanbase.ocp.monitor.meter.DistributionSummaryGroup;
import com.oceanbase.ocp.monitor.model.metric.MetricData;
import com.oceanbase.ocp.monitor.model.storage.ValueNode;
import com.oceanbase.ocp.monitor.storage.IRollupMetricDataDao;
import com.oceanbase.ocp.monitor.storage.MetricDataWriteQueue;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OcpMetricArchiveService {

    private static final int MAX_BATCH_SIZE = MonitorConstants.ARCHIVE_MAX_BATCH_SIZE;

    @Autowired
    @Qualifier("secondPersistent")
    private IRollupMetricDataDao secondPersistent;
    @Autowired
    @Qualifier("minutePersistent")
    private IRollupMetricDataDao minutePersistent;

    @Autowired
    private MetricDataWriteQueue secondWriteQueue;

    private ScheduledExecutorService secondExecutor;

    @Autowired
    private MeterRegistry meterRegistry;

    private DistributionSummaryGroup metricArchiveDuration;
    private CounterGroup archiveCounterGroup;
    private CounterGroup metricRollupGroup;

    private static final String SECOND_METER_LABEL = "second";

    @PostConstruct
    public void init() {
        this.metricArchiveDuration = DistributionSummaryGroup.builder("ocp_monitor_metric_archive_duration")
                .description("ocp monitor metric archive time")
                .baseUnits(BaseUnits.MILLISECONDS)
                .labelNames("type")
                .build(meterRegistry);
        this.archiveCounterGroup = CounterGroup.builder("ocp_monitor_archive_node_second")
                .description("Counting value nodes archived to monitor db")
                .labelNames("type")
                .build(meterRegistry);
        this.metricRollupGroup = CounterGroup.builder("ocp_monitor_rollup_second_to_minute")
                .description("Counting data rollup second to minute")
                .labelNames("type")
                .build(meterRegistry);

        int coreSize = Runtime.getRuntime().availableProcessors();
        this.secondExecutor =
                new ScheduledThreadPoolExecutor(coreSize, new OcpThreadFactory("metric-archive-second-"));

        IntStream.rangeClosed(1, coreSize)
                .forEach(value -> {
                    this.secondExecutor.scheduleWithFixedDelay(this::archiveSecond, 60, 3, TimeUnit.SECONDS);
                });
    }

    public void archiveSecond() {
        metricArchiveDuration.time(SECOND_METER_LABEL, () -> {
            LinkedList<ValueNode> nodeList = secondWriteQueue.poll(OcpMetricArchiveService.MAX_BATCH_SIZE);
            if (nodeList.isEmpty()) {
                return;
            }
            while (!nodeList.isEmpty()) {
                try {
                    archiveCounterGroup.increment(SECOND_METER_LABEL, nodeList.size());
                    int affectRows = secondPersistent.write(nodeList);
                    log.info("Second type metric archive, timestamp={}, nodeCount={}, affectRows={}",
                            nodeList.get(0).getEpochSecondStart(), nodeList.size(), affectRows);
                    rollupSecond2Minute(nodeList);
                } catch (Throwable throwable) {
                    log.error("Archive second nodes failed.", throwable);
                } finally {
                    nodeList = secondWriteQueue.poll(OcpMetricArchiveService.MAX_BATCH_SIZE);
                }
            }
        });
    }

    private void rollupSecond2Minute(LinkedList<ValueNode> valueNodes) {
        if (valueNodes == null || valueNodes.isEmpty()) {
            return;
        }
        long timestamp = valueNodes.get(0).getEpochSecondStart();
        LinkedList<MetricData> dataList = new LinkedList<>();
        while (!valueNodes.isEmpty()) {
            try {
                ValueNode node = valueNodes.poll();
                double value = node.getFirstValidValue();
                if (value != VALUE_NOT_EXIST) {
                    dataList.add(new MetricData(node.getSeriesId(), node.getEpochSecondStart(), value));
                }
            } catch (Throwable throwable) {
                log.warn("Convert valueNode failed.", throwable);
            }
        }
        metricRollupGroup.increment(SECOND_METER_LABEL, dataList.size());
        int dataSize = dataList.size();
        int affectedRows = minutePersistent.write(dataList);
        log.info("Rollup second to minute, timestamp={}, nodeCount={}, validCount={}",
                timestamp, dataSize, affectedRows);
    }


    @PreDestroy
    public void destroy() {
        ExecutorUtils.shutdown(secondExecutor, 1);
    }

}
