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

package com.oceanbase.ocp.obops.internal.cluster;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.lang.Pair;
import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.core.constants.LabelNames;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.monitor.MetricService;
import com.oceanbase.ocp.monitor.model.metric.SeriesMetricValues;
import com.oceanbase.ocp.monitor.model.metric.ValueData;
import com.oceanbase.ocp.monitor.param.MetricQueryParams;
import com.oceanbase.ocp.obops.cluster.model.PerformanceStats;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClusterMetricManager {

    @Autowired
    private MetricService metricService;

    @Autowired
    private ManagedCluster managedCluster;

    public Map<String, PerformanceStats> serverPerformanceStats() {
        return performanceStatsGroupBySvr(LabelNames.OB_SEVER_IP, LabelNames.OB_SEVER_PORT);
    }

    public Map<String, PerformanceStats> performanceStatsGroupBySvr(String svrIpLabel, String svrPortLabel) {
        List<SeriesMetricValues> seriesMetricValues = latestMetricValuesGroupBy(Arrays.asList(svrIpLabel, svrPortLabel),
                Arrays.asList("active_session", "qps", "tps"));

        Map<String, List<SeriesMetricValues>> groupBy2MetricValues = seriesMetricValues.stream()
                .collect(Collectors.groupingBy(t -> {
                    String svrIp = t.getMetric().getLabels().get(svrIpLabel);
                    String svrPort = t.getMetric().getLabels().get(svrPortLabel);
                    return serverKey(svrIp, Integer.parseInt(svrPort));
                }));

        List<Pair<String, Map<String, Double>>> zoneMetricValues =
                groupBy2MetricValues.entrySet().stream().map(entry -> {
                    String groupByValue = entry.getKey();
                    List<SeriesMetricValues> seriesMetricValuesListOfGroup = entry.getValue();
                    Map<String, Double> metricName2ValueMap = extractToMetricValueMap(seriesMetricValuesListOfGroup);
                    return Pair.of(groupByValue, metricName2ValueMap);
                }).collect(Collectors.toList());

        return zoneMetricValues.stream().collect(Collectors.toMap(Pair::getLeft, pair -> {
            Map<String, Double> right = pair.getRight();
            String s = JsonUtils.toJsonString(right);
            return JsonUtils.fromJson(s, PerformanceStats.class);
        }));
    }

    /**
     * @return List of Pair{groupByLabel, List of Pair{metricName, value}}
     */
    private List<SeriesMetricValues> latestMetricValuesGroupBy(List<String> groupBy, List<String> metricNames) {
        Validate.notEmpty(metricNames, "metricNames is empty");

        OffsetDateTime endTime = OffsetDateTime.now();
        OffsetDateTime startTime = endTime.minusMinutes(3);
        Map<String, String> labels = initLabelsFromClusterId();
        MetricQueryParams params = new MetricQueryParams();
        params.setStartTime(startTime);
        params.setEndTime(endTime);
        params.setInterval(0L);
        params.setMinStep(0L);
        params.setMetrics(metricNames);
        params.setLabels(labels);
        params.setGroupBy(groupBy);
        return metricService.querySeries(params);
    }

    private static Map<String, Double> extractToMetricValueMap(List<SeriesMetricValues> seriesMetricValuesList) {
        return seriesMetricValuesList.stream().map(seriesMetricValues -> {
            String metricName = seriesMetricValues.getMetric().getName();
            List<ValueData> dataList = seriesMetricValues.getData();
            Double latestValue = CollectionUtils.isEmpty(dataList) ? null : dataList.get(0).getValue();
            return Pair.of(metricName, latestValue);
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private Map<String, String> initLabelsFromClusterId() {
        Map<String, String> labels = new HashMap<>();
        labels.put(LabelNames.OB_CLUSTER, managedCluster.clusterName());
        labels.put(LabelNames.OB_CLUSTER_ID, String.valueOf(managedCluster.obClusterId()));
        return labels;
    }

    private static String serverKey(String svrIp, Integer svrPort) {
        return svrIp + ":" + svrPort;
    }
}
