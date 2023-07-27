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
package com.oceanbase.ocp.monitor.param;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricQueryParams {

    @JsonProperty("startTime")
    private OffsetDateTime startTime;

    @JsonProperty("endTime")
    private OffsetDateTime endTime;

    @JsonProperty("metrics")
    @Builder.Default
    private List<String> metrics = new ArrayList<>();

    @JsonProperty("labels")
    @Builder.Default
    private Map<String, String> labels = new HashMap<>();

    @JsonProperty("interval")
    private Long interval;

    @JsonProperty("minStep")
    private Long minStep;

    @JsonProperty("groupBy")
    @Builder.Default
    private List<String> groupBy = new ArrayList<>();

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("prometheusLabels")
    @Builder.Default
    private List<OcpPrometheusLabel> prometheusLabels = new ArrayList<>();

    @JsonProperty("maxPoints")
    @Builder.Default
    private Long maxPoints = 1440L;

    static Pattern pattern = Pattern.compile("^.*(!=|=~|!~).*");
    static Pattern patternEq = Pattern.compile("^.*(=|:).*");

    public MetricQueryParams fromQueryStringParams(MetricQueryStringParams queryStringParams) {
        String metricsStr = queryStringParams.getMetricsStr();
        if (StringUtils.isNotEmpty(metricsStr)) {
            this.metrics = Arrays.stream(metricsStr.split(","))
                    .map(StringUtils::trim).collect(Collectors.toList());
        }

        String groupByLabelsStr = queryStringParams.getGroupByLabelsStr();
        if (StringUtils.isNotEmpty(groupByLabelsStr)) {
            this.groupBy = Arrays.stream(groupByLabelsStr.split(","))
                    .map(StringUtils::trim).collect(Collectors.toList());
        }

        String labelsStr = queryStringParams.getLabelsStr();
        if (StringUtils.isNotEmpty(labelsStr)) {
            String[] labelPairs = labelsStr.split(",");
            for (String labelPair : labelPairs) {
                Matcher matcher = pattern.matcher(labelPair);
                if (matcher.find()) {
                    prometheusLabels.add(parseLabel(labelPair, matcher));
                } else {
                    Matcher matcherEq = patternEq.matcher(labelPair);
                    if (matcherEq.find()) {
                        prometheusLabels.add(parseLabel(labelPair, matcherEq));
                        parseEqLabels(labelPair, matcherEq, labels);
                    }
                }
            }
        }
        return this;
    }

    private static final String COLON = ":";
    private static final String EQUAL_SIGN = "=";

    private OcpPrometheusLabel parseLabel(String labelPair, Matcher matcher) {
        String labelSelector = matcher.group(1);
        int index = labelPair.indexOf(labelSelector);
        String labelKey = labelPair.substring(0, index).trim();
        String labelValue = labelPair.substring(index + labelSelector.length()).trim();
        if (COLON.equals(labelSelector) || EQUAL_SIGN.equals(labelSelector)) {
            return new OcpPrometheusLabel(labelKey, labelValue);
        }
        return new OcpPrometheusLabel(labelKey, labelSelector, labelValue);
    }

    private void parseEqLabels(String labelPair, Matcher matcher, Map<String, String> labels) {
        String labelSelector = matcher.group(1);
        int index = labelPair.indexOf(labelSelector);
        String labelKey = labelPair.substring(0, index).trim();
        String labelValue = labelPair.substring(index + labelSelector.length()).trim();
        if (COLON.equals(labelSelector) || EQUAL_SIGN.equals(labelSelector)) {
            labels.put(labelKey, labelValue);
        }
    }

    public long getStartTimestamp() {
        Validate.notNull(this.startTime, "startTime in MetricQueryParam require not null");
        return this.startTime.toEpochSecond();
    }

    public long getEndTimestamp() {
        return (this.endTime == null ? OffsetDateTime.now().toEpochSecond() : this.endTime.toEpochSecond());
    }

    public Set<String> labelsSet() {
        return labels.keySet();
    }

    public Set<String> groupBySet() {
        return new HashSet<>(groupBy);
    }

    public static void validate(MetricQueryParams queryParams) {
        Validate.notNull(queryParams, "queryParams require not null");
        Validate.notEmpty(queryParams.metrics, "queryParams.metrics require not empty");
        Validate.notNull(queryParams.groupBy, "queryParams.groupBy require not empty");
        Validate.notNull(queryParams.startTime, "queryParams.startTime require not null");
        Validate.notNull(queryParams.endTime, "queryParams.endTime require not null");
    }

}
