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

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.monitor.ExporterService;
import com.oceanbase.ocp.monitor.IIntervalMetricDataStore;
import com.oceanbase.ocp.monitor.OcpMetricCollectService;
import com.oceanbase.ocp.monitor.helper.ExporterRequestHelper;
import com.oceanbase.ocp.monitor.meter.CounterGroup;
import com.oceanbase.ocp.monitor.meter.DistributionSummaryGroup;
import com.oceanbase.ocp.monitor.model.exporter.ExporterAddress;
import com.oceanbase.ocp.monitor.model.metric.MetricLine;
import com.oceanbase.ocp.monitor.util.MetricLineParser;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("ocpMetricCollectService")
public class OcpMetricCollectServiceImpl implements OcpMetricCollectService {

    private static final long SLOW_PULL_THRESHOLD_MILLIS_SECOND_METRIC = 200L;

    @Autowired
    private IIntervalMetricDataStore secondStore;

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private ExporterRequestHelper exporterRequestHelper;

    private final ThreadPoolExecutor parseExec;

    private DistributionSummaryGroup collectRequestDuration;
    private CounterGroup parseMetricLines;
    private DistributionSummaryGroup metricParseDuration;
    private DistributionSummaryGroup metricStoreDuration;
    private CounterGroup collectRequestErrors;
    private CounterGroup collectMetricBytes;

    @Autowired
    private MeterRegistry meterRegistry;

    public OcpMetricCollectServiceImpl() {
        int coreSize = Runtime.getRuntime().availableProcessors() * 2;
        this.parseExec = new ThreadPoolExecutor(coreSize, coreSize, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10_000), new OcpThreadFactory("metric-parse-"),
                (r, executor) -> log.warn("Reject task {}, rejected from {}", r.toString(), executor.toString()));
    }

    @PostConstruct
    public void initMeterGroup() {
        collectRequestDuration = DistributionSummaryGroup.builder("ocp_monitor_collect_request_duration")
                .description("OCP monitor collect request time")
                .baseUnits(BaseUnits.MILLISECONDS)
                .labelNames("type")
                .build(meterRegistry);
        collectRequestErrors = CounterGroup.builder("ocp_monitor_collect_request_errors")
                .description("OCP failed metric collection count")
                .labelNames("type")
                .build(meterRegistry);
        collectMetricBytes = CounterGroup.builder("ocp_monitor_collect_metric_bytes")
                .description("OCP metric bytes collected")
                .labelNames("type")
                .build(meterRegistry);
        metricParseDuration = DistributionSummaryGroup.builder("ocp_monitor_metric_parse_duration")
                .description("OCP monitor metric parse time")
                .baseUnits(BaseUnits.MILLISECONDS)
                .labelNames("type")
                .build(meterRegistry);
        metricStoreDuration = DistributionSummaryGroup.builder("ocp_monitor_metric_store_duration")
                .description("OCP monitor metric store time")
                .baseUnits(BaseUnits.MILLISECONDS)
                .labelNames("type")
                .build(meterRegistry);
        parseMetricLines = CounterGroup.builder("ocp_monitor_parse_metric_lines")
                .description("OCP metric lines parsed")
                .labelNames("type")
                .build(meterRegistry);
    }

    @Override
    public void collect(ExporterAddress addr, long collectAt) {
        asyncCollect(addr, collectAt, parseExec);
    }

    private void asyncCollect(ExporterAddress addr, long collectAt, ThreadPoolExecutor exec) {
        log.debug("Collect interval metric, instance={}, collectAt={}",
                addr.getExporterUrl(), collectAt);

        long start = System.currentTimeMillis();
        ListenableFuture<Response> whenResp = exporterRequestHelper.get(addr.getExporterUrl());
        if (whenResp == null) {
            return;
        }
        Runnable listener = () -> this.handleMetricResp(addr, collectAt, start, whenResp);
        whenResp.addListener(listener, exec);
    }

    private void handleMetricResp(ExporterAddress exporterAddr, long collectAt, long start,
            ListenableFuture<Response> whenResp) {
        try {
            Response response = whenResp.get();
            if (response.getStatusCode() != 200) {
                exporterService.inactiveExporter(exporterAddr.getExporterUrl());
                collectRequestErrors.increment(exporterAddr.getPath(), 1);
                log.warn("Collect failed, statusCode={}, content={}, url={}, collectAt={}",
                        response.getStatusCode(), response.getResponseBody(), exporterAddr.getExporterUrl(), collectAt);
                return;
            }
            long collectDoneAt = System.currentTimeMillis();
            exporterService.activeExporter(exporterAddr.getExporterUrl());
            String result;
            try {
                result = response.getResponseBody();
                collectMetricBytes.increment(exporterAddr.getPath(), result.length());
                parseAndStore(exporterAddr, collectAt, result);
            } catch (Exception e) {
                log.warn("store Raw Metric Failed", e);
            } finally {
                long collectCost = System.currentTimeMillis() - start;
                if (collectCost >= SLOW_PULL_THRESHOLD_MILLIS_SECOND_METRIC) {
                    long pullCost = collectDoneAt - start;
                    log.debug("Slow collect, exporter={}, collectCost={}, pullCost={}",
                            exporterAddr.getExporterUrl(), collectCost, pullCost);
                }
                TraceUtils.clear();
                collectRequestDuration.time(exporterAddr.getPath(), start);
            }
        } catch (Throwable e) {
            try {
                exporterService.inactiveExporter(exporterAddr.getExporterUrl());
                collectRequestErrors.increment(exporterAddr.getPath(), 1);
            } catch (Throwable throwable) {
                log.info("hh", throwable);
            }
            log.info("Collect failed, exporter={}, collectAt={}, message={}, rootCause={}",
                    exporterAddr.getExporterUrl(), collectAt, e.getMessage(), ExceptionUtils.getRootCauseMessage(e));
        }
    }

    private void parseAndStore(ExporterAddress addr, long collectAt, String rawMetric) {
        LinkedList<MetricLine> lines = metricParseDuration.time(addr.getPath(),
                () -> MetricLineParser.parseResultToLines(rawMetric, collectAt));
        parseMetricLines.increment(addr.getPath(), lines.size());
        metricStoreDuration.time(addr.getPath(), () -> secondStore.store(lines));
    }

    @PreDestroy
    public void destroy() {
        ExecutorUtils.shutdown(parseExec, 1);
    }

}
