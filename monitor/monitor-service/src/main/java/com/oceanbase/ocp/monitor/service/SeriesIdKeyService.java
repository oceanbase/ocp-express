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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.github.benmanes.caffeine.cache.Cache;

import com.oceanbase.ocp.common.util.IdGenerator;
import com.oceanbase.ocp.monitor.entity.SeriesKeyId;
import com.oceanbase.ocp.monitor.helper.MetricSearchContainer;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryParam;
import com.oceanbase.ocp.monitor.model.metric.Metric;
import com.oceanbase.ocp.monitor.param.OcpPrometheusLabel;
import com.oceanbase.ocp.monitor.storage.SeriesKeyIdPersistent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SeriesIdKeyService {

    private final ConcurrentHashMap<String, Long> seriesKey2SeriesId = new ConcurrentHashMap<>(10240);
    private final ConcurrentHashMap<Long, String> seriesId2SeriesKey = new ConcurrentHashMap<>(10240);

    private final Map<String, MetricSearchContainer> metricName2SearchContainer = new ConcurrentHashMap<>(256);

    private volatile long maxSyncedSeriesId = 0;

    private static final int PAGE_SIZE = 20000;

    @Autowired
    private SeriesKeyIdPersistent seriesKeyIdPersistent;

    @Autowired
    @Qualifier("scanSeriesIdsCache")
    private Cache<OcpPrometheusQueryParam, List<Long>> scanSeriesIdsCache;

    @Autowired
    private MeterRegistry meterRegistry;

    private DistributionSummary seriesStoreDuration;
    private Counter seriesQueries;
    private Counter seriesHits;

    @PostConstruct
    public void initialLoad() {
        seriesStoreDuration = DistributionSummary.builder("ocp_monitor_series_store_duration")
                .description("Series id store duration")
                .baseUnit(BaseUnits.MILLISECONDS)
                .register(meterRegistry);
        Gauge.builder("ocp_monitor_series_cache_size", this::cacheSize)
                .description("Series id cache size")
                .register(meterRegistry);
        seriesQueries = Counter.builder("ocp_monitor_series_queries_total")
                .description("Series id queries count")
                .register(meterRegistry);
        seriesHits = Counter.builder("ocp_monitor_series_hits_total")
                .description("Series id hits count")
                .register(meterRegistry);
    }

    public int cacheSize() {
        return seriesKey2SeriesId.size();
    }

    /**
     * @return metricName2KeyValueCount Map
     */
    public Map<String, Integer> getMetricSearchStats() {
        Map<String, Integer> metricName2KeyValueCount = new HashMap<>();
        for (Map.Entry<String, MetricSearchContainer> entry : metricName2SearchContainer.entrySet()) {
            metricName2KeyValueCount.put(entry.getKey(), entry.getValue().keyValueCount());
        }
        return metricName2KeyValueCount;
    }

    public Long getSeriesId(Metric metric) {
        String seriesKey = metric.getSeriesKey();
        Long seriesId = seriesKey2SeriesId.get(seriesKey);
        seriesQueries.increment();
        if (seriesId != null) {
            seriesHits.increment();
            return seriesId;
        } else {
            seriesId = seriesKeyIdPersistent.querySeriesId(seriesKey);
            if (seriesId == null) {
                seriesId = IdGenerator.getInstance().getNextId();
            }
            long start = System.currentTimeMillis();
            try {
                seriesKeyIdPersistent.replaceSeriesKeyId(seriesKey, seriesId);
                cacheAndBuildSearchContainer(metric, seriesId);
            } finally {
                seriesStoreDuration.record(System.currentTimeMillis() - start);
            }
        }
        return seriesId;
    }

    public void syncKeyId() {
        log.info("Sync series key and id, currentMaxSeriesId={}", this.maxSyncedSeriesId);
        List<SeriesKeyId> newSeriesKeyIds = seriesKeyIdPersistent.fetchAfterId(this.maxSyncedSeriesId, PAGE_SIZE);
        while (newSeriesKeyIds != null && !newSeriesKeyIds.isEmpty()) {
            for (SeriesKeyId seriesKeyId : newSeriesKeyIds) {
                try {
                    Metric metric = Metric.parse(seriesKeyId.getSeriesKey());
                    Long seriesId = seriesKeyId.getSeriesId();
                    cacheAndBuildSearchContainer(metric, seriesId);
                    if (seriesId > this.maxSyncedSeriesId) {
                        this.maxSyncedSeriesId = seriesId;
                    }
                } catch (Throwable throwable) {
                    log.warn("Cache series info failed.", throwable);
                }
            }
            log.info("Sync series key and id, newSeriesIdsCount={}, maxSyncedSeriesId={}",
                    newSeriesKeyIds.size(), this.maxSyncedSeriesId);
            newSeriesKeyIds = seriesKeyIdPersistent.fetchAfterId(this.maxSyncedSeriesId, PAGE_SIZE);
        }
    }

    private synchronized void cacheAndBuildSearchContainer(Metric metric, Long seriesId) {
        seriesKey2SeriesId.put(metric.getSeriesKey(), seriesId);
        seriesId2SeriesKey.put(seriesId, metric.getSeriesKey());
        MetricSearchContainer searchContainer =
                metricName2SearchContainer.computeIfAbsent(metric.getName().toLowerCase(), MetricSearchContainer::new);
        searchContainer.addIfAbsent(seriesId, metric);
    }

    public List<Long> scanSeriesIds(OcpPrometheusQueryParam param) {
        return scanSeriesIdsCache.get(param, p -> matchSeriesIds(p.getMetric(), p.getLabels()));
    }

    public List<Long> matchSeriesIds(String metricName, List<OcpPrometheusLabel> labels) {
        Validate.notEmpty(metricName, "metricName is null or empty");
        MetricSearchContainer searchContainer = metricName2SearchContainer.get(metricName.toLowerCase());
        if (searchContainer == null) {
            return Collections.emptyList();
        }
        return searchContainer.select(labels);
    }

    public Map<Long, String> getSeriesId2KeyMap(List<Long> seriesIds) {
        if (CollectionUtils.isEmpty(seriesIds)) {
            return Collections.emptyMap();
        }
        Map<Long, String> seriesId2KeyMap = new HashMap<>(seriesIds.size());
        for (Long seriesId : seriesIds) {
            String seriesKey = seriesId2SeriesKey.get(seriesId);
            seriesId2KeyMap.put(seriesId, seriesKey);
        }
        return seriesId2KeyMap;
    }

}
