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

package com.oceanbase.ocp.controller;

import static java.lang.Math.abs;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.PaginatedResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.monitor.MetricQueryService;
import com.oceanbase.ocp.monitor.MetricService;
import com.oceanbase.ocp.monitor.model.MetricClass;
import com.oceanbase.ocp.monitor.model.MetricScope;
import com.oceanbase.ocp.monitor.model.MetricType;
import com.oceanbase.ocp.monitor.model.MetricValues;
import com.oceanbase.ocp.monitor.model.TopMetricValue;
import com.oceanbase.ocp.monitor.model.TopMetricValues;
import com.oceanbase.ocp.monitor.model.metric.SeriesMetricValues;
import com.oceanbase.ocp.monitor.param.MetricQueryParams;
import com.oceanbase.ocp.monitor.param.MetricQueryStringParams;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping({"/api/v1"})
public class MonitorController {

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricQueryService metricQueryService;

    private static final int VALID_QUERY_RANGE_DAY = 15;

    @GetMapping(value = "/monitor/metricGroups", produces = {"application/json"})
    public PaginatedResponse<MetricClass> listMetricClasses(
            @PageableDefault(size = Integer.MAX_VALUE, sort = {"id"}, direction = Direction.ASC) Pageable pageable,
            @RequestParam(value = "type", defaultValue = "NORMAL") MetricType type,
            @RequestParam(value = "scope") MetricScope scope) {
        return ResponseBuilder.paginated(metricService.listMetricClasses(scope, type, pageable));
    }

    private void checkQueryRange(OffsetDateTime startTime, OffsetDateTime endTime) {
        boolean valid =
                abs(Duration.between(endTime.toInstant(), startTime.toInstant()).toDays()) < VALID_QUERY_RANGE_DAY;
        Validate.isTrue(valid, "Invalid query range, query range should not bigger than 60 days");
    }

    @GetMapping(value = "/monitor/top", produces = {"application/json"})
    public IterableResponse<Map<Object, Object>> queryMetricTop(
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime", required = false) OffsetDateTime endTime,
            @RequestParam(value = "metrics") String metrics,
            @RequestParam(value = "groupBy") String groupBy,
            @RequestParam(value = "interval", required = false, defaultValue = "0") Long interval,
            @RequestParam(value = "minStep", required = false, defaultValue = "0") Long minStep,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "maxPoints", required = false, defaultValue = "1440") Long maxPoints,
            @RequestParam(value = "labels", required = false) String labels) {
        checkQueryRange(startTime, endTime);
        MetricQueryStringParams queryStringParams = MetricQueryStringParams.builder().metricsStr(metrics)
                .groupByLabelsStr(groupBy).labelsStr(labels).build();
        MetricQueryParams param = MetricQueryParams.builder().startTime(startTime).endTime(endTime).interval(interval)
                .minStep(minStep).limit(limit).maxPoints(maxPoints).build().fromQueryStringParams(queryStringParams);
        List<TopMetricValues> result = metricService.queryMetricsWithLabel(param);
        List<Map<Object, Object>> resultMaps = result.stream().map(TopMetricValues::toMap).collect(Collectors.toList());
        return ResponseBuilder.iterable(resultMaps);
    }

    @GetMapping(value = "/monitor/top/realtime", produces = {"application/json"})
    public IterableResponse<Map<Object, Object>> queryRealtimeTopMetrics(
            @RequestParam(value = "endTime", required = false) OffsetDateTime endTime,
            @RequestParam(value = "duration", required = false, defaultValue = "60") Long duration,
            @RequestParam(value = "metrics") String metrics,
            @RequestParam(value = "groupBy") String groupBy,
            @RequestParam(value = "interval", required = false, defaultValue = "0") Long interval,
            @RequestParam(value = "minStep", required = false, defaultValue = "0") Long minStep,
            @RequestParam(value = "maxPoints", required = false, defaultValue = "1440") Long maxPoints,
            @RequestParam(value = "limit", required = false, defaultValue = "5") Integer limit,
            @RequestParam(value = "labels", required = false) String labels) {
        MetricQueryStringParams queryStringParams = MetricQueryStringParams.builder().metricsStr(metrics)
                .groupByLabelsStr(groupBy).labelsStr(labels).build();
        OffsetDateTime startTime = endTime.minusSeconds(duration);
        MetricQueryParams param = MetricQueryParams.builder().startTime(startTime)
                .endTime(endTime).interval(interval).minStep(minStep)
                .limit(limit).maxPoints(maxPoints).build().fromQueryStringParams(queryStringParams);
        List<TopMetricValue> result = metricService.queryRealtimeTopMetrics(param);
        List<Map<Object, Object>> resultMaps = result.stream().map(TopMetricValue::toMap).collect(Collectors.toList());
        return ResponseBuilder.iterable(resultMaps);
    }

    @GetMapping(value = "/monitor/metricsWithLabel", produces = {"application/json"})
    public IterableResponse<Map<Object, Object>> queryMetricsWithLabel(
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime", required = false) OffsetDateTime endTime,
            @RequestParam(value = "metrics") String metrics,
            @RequestParam(value = "groupBy") String groupBy,
            @RequestParam(value = "interval", required = false, defaultValue = "0") Long interval,
            @RequestParam(value = "minStep", required = false, defaultValue = "0") Long minStep,
            @RequestParam(value = "maxPoints", required = false, defaultValue = "1440") Long maxPoints,
            @RequestParam(value = "labels", required = false) String labels) {
        checkQueryRange(startTime, endTime);
        MetricQueryStringParams queryStringParams = MetricQueryStringParams.builder().metricsStr(metrics)
                .groupByLabelsStr(groupBy).labelsStr(labels).build();
        MetricQueryParams param = MetricQueryParams.builder().startTime(startTime).endTime(endTime).interval(interval)
                .minStep(minStep).maxPoints(maxPoints).build().fromQueryStringParams(queryStringParams);
        List<TopMetricValues> result = metricService.queryMetricsWithLabel(param);
        List<Map<Object, Object>> resultMaps = result.stream().map(TopMetricValues::toMap).collect(Collectors.toList());
        return ResponseBuilder.iterable(resultMaps);
    }

    @GetMapping(value = "/monitor/metric", produces = {"application/json"})
    public IterableResponse<Map<Object, Object>> queryMetric(
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime", required = false) OffsetDateTime endTime,
            @RequestParam(value = "metrics") String metrics,
            @RequestParam(value = "groupBy") String groupBy,
            @RequestParam(value = "interval", required = false, defaultValue = "0") Long interval,
            @RequestParam(value = "minStep", required = false, defaultValue = "0") Long minStep,
            @RequestParam(value = "maxPoints", required = false, defaultValue = "1440") Long maxPoints,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "labels", required = false) String labels) {
        checkQueryRange(startTime, endTime);
        MetricQueryStringParams queryStringParams = MetricQueryStringParams.builder().metricsStr(metrics)
                .groupByLabelsStr(groupBy).labelsStr(labels).build();
        MetricQueryParams param = MetricQueryParams.builder().startTime(startTime).endTime(endTime).interval(interval)
                .minStep(minStep).limit(limit).maxPoints(maxPoints).build().fromQueryStringParams(queryStringParams);
        List<MetricValues> result = metricService.queryMetrics(param);
        List<Map<Object, Object>> resultMaps = result.stream().map(MetricValues::toMap).collect(Collectors.toList());
        return ResponseBuilder.iterable(resultMaps);
    }

    @GetMapping(value = "/monitor/metric/series", produces = {"application/json"})
    public IterableResponse<SeriesMetricValues> queryMetricSeries(
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime", required = false) OffsetDateTime endTime,
            @RequestParam(value = "metrics") String metrics,
            @RequestParam(value = "groupBy") String groupBy,
            @RequestParam(value = "interval", required = false, defaultValue = "0") Long interval,
            @RequestParam(value = "minStep", required = false, defaultValue = "0") Long minStep,
            @RequestParam(value = "max_points", required = false, defaultValue = "1440") Long maxPoints,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "labels", required = false) String labels) {
        checkQueryRange(startTime, endTime);
        MetricQueryStringParams queryStringParams = MetricQueryStringParams.builder().metricsStr(metrics)
                .groupByLabelsStr(groupBy).labelsStr(labels).build();
        MetricQueryParams param = MetricQueryParams.builder().startTime(startTime).endTime(endTime).interval(interval)
                .minStep(minStep).limit(limit).maxPoints(maxPoints).build().fromQueryStringParams(queryStringParams);
        List<SeriesMetricValues> result = metricService.querySeries(param);
        return ResponseBuilder.iterable(result);
    }

}
