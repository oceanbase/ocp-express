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

package com.oceanbase.ocp.monitor.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.util.CollectionUtils;

import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.monitor.constants.VectorSelectorType;
import com.oceanbase.ocp.monitor.model.metric.Metric;
import com.oceanbase.ocp.monitor.model.metric.MetricLabels;
import com.oceanbase.ocp.monitor.param.OcpPrometheusLabel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 **/
@Slf4j
public class MetricSearchContainer {

    private final String metricName;
    private final String2IntMap string2IntMap = new String2IntMap();
    private final Map<String, Set<String>> labelKey2ValueSet = new ConcurrentHashMap<>();
    private final Map<MetricIndex, Set<Long>> metricIndex2SeriesIds = new ConcurrentHashMap<>();

    public MetricSearchContainer(String metricName) {
        Validate.notEmpty(metricName, "metricName is null or empty");
        this.metricName = metricName;
    }

    /**
     * Find matched series by labels.
     */
    public List<Long> select(List<OcpPrometheusLabel> prometheusLabels) {
        List<Long> seriesIds = new LinkedList<>();
        if (CollectionUtils.isEmpty(prometheusLabels)) {
            metricIndex2SeriesIds.values().forEach(seriesIds::addAll);
            log.warn("Label is null, return all seriesIds, metricName = {}, seriesId size = {}", metricName,
                    seriesIds.size());
            return seriesIds;
        }
        List<Set<Integer>> keyValues4MatchList = calcKeyValues4Match(prometheusLabels);

        if (!CollectionUtils.isEmpty(keyValues4MatchList)) {
            for (Map.Entry<MetricIndex, Set<Long>> entry : metricIndex2SeriesIds.entrySet()) {
                int[] prepare = prepare(keyValues4MatchList, entry.getKey().getKeyValues());
                if (prepare.length == 0) {
                    continue;
                }
                if (containsAll(entry.getKey().getKeyValues(), prepare)) {
                    seriesIds.addAll(entry.getValue());
                }
            }
        }

        return seriesIds;
    }

    private List<Set<Integer>> calcKeyValues4Match(List<OcpPrometheusLabel> labels) {
        List<Set<Integer>> resList = new ArrayList<>();
        for (OcpPrometheusLabel label : labels) {
            VectorSelectorType labelMatch = label.getLabelMatch();
            String labelKey = label.getLabelKey().toLowerCase();
            String labelValue = label.getLabelValue().toLowerCase();
            Set<Integer> set = new HashSet<>();
            switch (labelMatch) {
                case MATCH_EQ:
                    set.add(string2IntMap.getInt(labelKey + "=" + labelValue));
                    break;
                case MATCH_NEQ:
                    set = labelKey2ValueSet.get(labelKey).stream().filter(v -> !v.equals(labelValue))
                            .map(v -> string2IntMap.getInt(labelKey + "=" + v)).collect(
                                    Collectors.toSet());
                    break;
                case MATCH_REGEX:
                    set = labelKey2ValueSet.get(labelKey).stream()
                            .filter(v -> OcpPrometheusRegexMatcher.matchString(v, labelValue))
                            .map(v -> string2IntMap.getInt(labelKey + "=" + v)).collect(
                                    Collectors.toSet());
                    break;
                case MATCH_NOT_REGEX:
                    set = labelKey2ValueSet.get(labelKey).stream()
                            .filter(v -> !OcpPrometheusRegexMatcher.matchString(v, labelValue))
                            .map(v -> string2IntMap.getInt(labelKey + "=" + v)).collect(
                                    Collectors.toSet());
                    break;
                default:
                    throw new IllegalArgumentException(ErrorCodes.COMMON_ILLEGAL_ARGUMENT, labelMatch);
            }
            if (CollectionUtils.isEmpty(set)) {
                return new ArrayList<>();
            }
            resList.add(set);
        }
        return resList;
    }


    private int[] prepare(List<Set<Integer>> keyValues4MatchList, int[] keyValues) {
        int[] res = new int[keyValues4MatchList.size()];
        outer: for (int i = 0; i < keyValues4MatchList.size(); i++) {
            Set<Integer> set = keyValues4MatchList.get(i);
            if (!CollectionUtils.isEmpty(set)) {
                if (set.size() > 1) {
                    for (int value : keyValues) {
                        if (set.contains(value)) {
                            res[i] = value;
                            continue outer;
                        }
                    }
                    return new int[] {};
                } else {
                    res[i] = set.iterator().next();
                }
            }
        }
        if (res.length > 0) {
            Arrays.sort(res);
        }
        return res;
    }

    public void addIfAbsent(Long seriesId, Metric metric) {
        Validate.notNull(metric, "metric is null");
        Validate.isTrue(metricName.equalsIgnoreCase(metric.getName()));
        MetricLabels labels = metric.getLabels();
        Validate.notEmpty(labels, "labels is empty");

        MetricIndex metricIndex = new MetricIndex(calcKeyValues(labels));
        Set<Long> set = metricIndex2SeriesIds.computeIfAbsent(metricIndex, k -> new HashSet<>());
        set.add(seriesId);
        log.debug("Add metric, seriesId={}, metricName={}, labels={}",
                seriesId, metric.getName(), this.labelKey2ValueSet);
    }

    public int keyValueCount() {
        return string2IntMap.size();
    }

    boolean containsAll(int[] keyValues, int[] keyValues4Match) {
        if (keyValues4Match.length == 0) {
            return true;
        }
        int currentPos = 0;
        int currentMatchPos = 0;

        while (currentPos < keyValues.length) {
            if (keyValues[currentPos] > keyValues4Match[currentMatchPos]) {
                break;
            }
            if (keyValues[currentPos] == keyValues4Match[currentMatchPos]) {
                currentMatchPos++;
                if (currentMatchPos >= keyValues4Match.length) {
                    break;
                }
            }
            currentPos++;
        }
        return currentMatchPos >= keyValues4Match.length;
    }

    int[] calcKeyValues(MetricLabels labels) {
        if (CollectionUtils.isEmpty(labels)) {
            return new int[0];
        }
        int pos = 0;
        int[] keyValues = new int[labels.size()];
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue().toLowerCase();
            keyValues[pos++] =
                    string2IntMap.getInt(key + "=" + value);
            Set<String> valueSet = labelKey2ValueSet.computeIfAbsent(key, k -> new HashSet<>());
            valueSet.add(value);
        }
        Arrays.sort(keyValues);
        return keyValues;
    }

    @Data
    @EqualsAndHashCode
    private static class MetricIndex {

        private int[] keyValues;

        MetricIndex(int[] keyValues) {
            this.keyValues = keyValues;
        }
    }
}
