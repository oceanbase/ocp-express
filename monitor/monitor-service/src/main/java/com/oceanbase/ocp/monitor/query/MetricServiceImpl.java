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

package com.oceanbase.ocp.monitor.query;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.monitor.MetricQueryService;
import com.oceanbase.ocp.monitor.MetricService;
import com.oceanbase.ocp.monitor.helper.ModelConverter;
import com.oceanbase.ocp.monitor.model.MetricClass;
import com.oceanbase.ocp.monitor.model.MetricGroup;
import com.oceanbase.ocp.monitor.model.MetricMeta;
import com.oceanbase.ocp.monitor.model.MetricScope;
import com.oceanbase.ocp.monitor.model.MetricType;
import com.oceanbase.ocp.monitor.model.MetricValues;
import com.oceanbase.ocp.monitor.model.OcpPrometheusData;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryResult;
import com.oceanbase.ocp.monitor.model.TopMetricValue;
import com.oceanbase.ocp.monitor.model.TopMetricValues;
import com.oceanbase.ocp.monitor.model.metric.Metric;
import com.oceanbase.ocp.monitor.model.metric.OcpPrometheusMeasurement;
import com.oceanbase.ocp.monitor.model.metric.SeriesMetricValues;
import com.oceanbase.ocp.monitor.model.metric.ValueData;
import com.oceanbase.ocp.monitor.param.MetricQueryParams;
import com.oceanbase.ocp.monitor.storage.repository.MetricClassEntity;
import com.oceanbase.ocp.monitor.storage.repository.MetricClassRepository;
import com.oceanbase.ocp.monitor.storage.repository.MetricGroupEntity;
import com.oceanbase.ocp.monitor.storage.repository.MetricGroupRepository;
import com.oceanbase.ocp.monitor.storage.repository.MetricMetaRepository;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetricServiceImpl implements MetricService {

    @Autowired
    private MetricQueryService metricQueryService;

    @Autowired
    private MetricClassRepository metricClassRepository;

    @Autowired
    private MetricMetaRepository metricMetaRepository;

    @Autowired
    private MetricGroupRepository metricGroupRepository;

    @Autowired
    private ManagedCluster managedCluster;

    @Override
    public List<TopMetricValues> queryMetricsWithLabel(MetricQueryParams queryParams) {
        Map<String, List<OcpPrometheusQueryResult>> resultMap = metricQueryService.queryMetricBatch(queryParams)
                .entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Integer limit = queryParams.getLimit();
        List<TopMetricValues> result = new LinkedList<>();
        for (String metric : queryParams.getMetrics()) {
            if (!resultMap.containsKey(metric)) {
                log.debug("metric {} not in query result, skip", metric);
                continue;
            }
            List<OcpPrometheusQueryResult> queryResults = resultMap.get(metric);
            if (null != limit) {
                queryResults = getTopN(queryResults, limit);
            }
            for (OcpPrometheusQueryResult queryResult : queryResults) {
                TopMetricValues topMetricValues = new TopMetricValues();
                OcpPrometheusMeasurement measurement = queryResult.getMeasurement();
                topMetricValues.putAll(measurement.getLabels());
                List<MetricValues> dataList = new LinkedList<>();
                for (OcpPrometheusData data : queryResult.getData()) {
                    MetricValues metricValues = new MetricValues();
                    metricValues.setTimestamp(data.getTimestamp());
                    metricValues.put(metric, (data.getValue() > 0 ? data.getValue() : 0));
                    dataList.add(metricValues);
                }
                topMetricValues.setData(dataList);
                result.add(topMetricValues);
            }
        }
        return result;
    }

    @Override
    public List<TopMetricValue> queryRealtimeTopMetrics(MetricQueryParams queryParams) {
        Map<String, List<OcpPrometheusQueryResult>> resultMap = metricQueryService.queryMetricBatch(queryParams)
                .entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Integer limit = queryParams.getLimit();
        List<TopMetricValue> result = new LinkedList<>();
        for (String metric : queryParams.getMetrics()) {
            if (!resultMap.containsKey(metric)) {
                log.debug("metric {} not in query result, skip", metric);
                continue;
            }
            List<TopMetricValue> groupMetricResult = new LinkedList<>();
            List<OcpPrometheusQueryResult> queryResults = resultMap.get(metric);
            for (OcpPrometheusQueryResult queryResult : queryResults) {
                TopMetricValue topMetricValue = new TopMetricValue();
                OcpPrometheusMeasurement measurement = queryResult.getMeasurement();
                topMetricValue.putAll(measurement.getLabels());
                List<OcpPrometheusData> dataList = queryResult.getData().stream().sorted(
                        Comparator.comparing(OcpPrometheusData::getTimestamp).reversed()).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(dataList)) {
                    continue;
                }
                OcpPrometheusData data = dataList.get(0);
                topMetricValue.setMetric(metric);
                topMetricValue.setData(data.getValue() > 0 ? data.getValue() : 0);
                groupMetricResult.add(topMetricValue);
            }
            groupMetricResult =
                    groupMetricResult.stream().sorted(Comparator.comparingDouble(TopMetricValue::getData).reversed())
                            .limit(limit).collect(Collectors.toList());
            result.addAll(groupMetricResult);
        }
        return result;
    }

    private List<OcpPrometheusQueryResult> getTopN(List<OcpPrometheusQueryResult> dataList, int limit) {
        if (dataList.size() <= limit) {
            return dataList;
        } else {
            dataList.sort(Comparator.comparingDouble((OcpPrometheusQueryResult result) -> result.getData()
                    .stream().map(OcpPrometheusData::getValue).reduce(0.0, Double::sum)).reversed());
            List<OcpPrometheusQueryResult> result = new LinkedList<>();
            for (int i = 0; i < limit; i++) {
                result.add(dataList.get(i));
            }
            return result;
        }
    }

    @Override
    public List<SeriesMetricValues> querySeries(MetricQueryParams queryParams) {
        MetricQueryParams.validate(queryParams);

        Map<String, List<OcpPrometheusQueryResult>> metric2ResultListMap =
                metricQueryService.queryMetricBatch(queryParams);
        return metric2ResultListMap.entrySet().stream().flatMap(entry -> {
            String metricName = entry.getKey();
            List<OcpPrometheusQueryResult> resultList = entry.getValue();
            return resultList.stream().map(result -> {
                OcpPrometheusMeasurement measurement = result.getMeasurement();
                LinkedList<OcpPrometheusData> dataList = result.getData();
                List<ValueData> valueDataList = dataList.stream()
                        .map(data -> new ValueData(data.getTimestamp(), (data.getValue() > 0 ? data.getValue() : 0)))
                        .collect(Collectors.toList());
                SeriesMetricValues seriesMetricValues = new SeriesMetricValues();
                seriesMetricValues.setData(valueDataList);
                seriesMetricValues.setMetric(new Metric(metricName, measurement.getLabels()));
                return seriesMetricValues;
            });
        }).limit(Objects.isNull(queryParams.getLimit()) ? Integer.MAX_VALUE : queryParams.getLimit())
                .collect(Collectors.toList());
    }

    @Override
    public List<MetricValues> queryMetrics(MetricQueryParams queryParams) {
        MetricQueryParams.validate(queryParams);

        Map<String, List<OcpPrometheusQueryResult>> queryResults = metricQueryService.queryMetricBatch(queryParams);
        Map<Long, MetricValues> resultMap = new HashMap<>();
        for (String metric : queryParams.getMetrics()) {
            if (!queryResults.containsKey(metric)) {
                log.debug("metric {} not in query result, skip", metric);
                continue;
            }
            for (OcpPrometheusQueryResult queryResult : queryResults.get(metric)) {
                for (OcpPrometheusData data : queryResult.getData()) {
                    Long timestamp = data.getTimestamp();
                    if (!resultMap.containsKey(timestamp)) {
                        resultMap.put(timestamp, new MetricValues());
                        resultMap.get(timestamp).setTimestamp(timestamp);
                    }
                    resultMap.get(timestamp).put(metric, (data.getValue() > 0 ? data.getValue() : 0));
                }
            }
        }
        return new LinkedList<>(resultMap.values());
    }

    @Override
    public Page<MetricClass> listMetricClasses(MetricScope scope, MetricType type, Pageable pageable) {
        return metricClassRepository.findByScopeAndType(scope, type, pageable)
                .map(this::mapToMetricClassModel);
    }

    private MetricClass mapToMetricClassModel(MetricClassEntity entity) {
        MetricClass metricClass = ModelConverter.fromEntity(entity);
        List<MetricGroup> metricGroups = metricGroupRepository.findByClassKey(metricClass.getKey()).stream()
                .map(this::mapToMetricGroupModel)
                .filter(metricGroup -> !CollectionUtils.isEmpty(metricGroup.getMetrics()))
                .sorted(Comparator.comparingLong(MetricGroup::getId))
                .collect(Collectors.toList());
        metricClass.setMetricGroups(metricGroups);
        return metricClass;
    }

    private MetricGroup mapToMetricGroupModel(MetricGroupEntity entity) {
        MetricGroup metricGroup = ModelConverter.fromEntity(entity);
        List<MetricMeta> metricMetas = metricMetaRepository
                .findByGroupKeyAndClassKey(metricGroup.getKey(), metricGroup.getClassKey()).stream()
                .map(ModelConverter::fromEntity)
                .sorted(Comparator.comparingLong(MetricMeta::getId))
                .collect(Collectors.toList());

        String obVersion = managedCluster.obVersion();
        if (StringUtils.isNotEmpty(obVersion)) {
            metricMetas = metricMetas.stream().filter(
                    e -> StringUtils.isEmpty(e.getMinObVersion())
                            || ObSdkUtils.versionAfter(obVersion, e.getMinObVersion()))
                    .filter(e -> StringUtils.isEmpty(e.getMaxObVersion())
                            || ObSdkUtils.versionBefore(obVersion, e.getMaxObVersion()))
                    .collect(Collectors.toList());
        }

        metricGroup.setMetrics(metricMetas);
        return metricGroup;
    }

}
