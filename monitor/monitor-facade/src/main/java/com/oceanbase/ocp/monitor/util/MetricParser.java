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
package com.oceanbase.ocp.monitor.util;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.monitor.model.metric.Metric;
import com.oceanbase.ocp.monitor.model.metric.MetricLabels;

public class MetricParser {

    private final char labelsDelimiter;
    private final char keyValueDelimiter;

    public MetricParser(char labelsDelimiter, char keyValueDelimiter) {
        this.labelsDelimiter = labelsDelimiter;
        this.keyValueDelimiter = keyValueDelimiter;
    }

    public Metric parse(String seriesKey) {
        Validate.notEmpty(seriesKey, "seriesKey is null or empty");
        int idx = firstIdxOf(seriesKey, 0, labelsDelimiter);
        if (idx == -1) {
            return new Metric(seriesKey, new MetricLabels());
        }
        String metricName = seriesKey.substring(0, idx);
        MetricLabels labels = parseLabels(seriesKey, idx + 1);
        return new Metric(metricName, labels);
    }

    private MetricLabels parseLabels(String seriesKey, int labelStart) {
        MetricLabels metricLabels = new MetricLabels();
        while (labelStart < seriesKey.length()) {
            int keyStart = labelStart;
            int keyEnd = firstIdxOf(seriesKey, keyStart, keyValueDelimiter);
            if (keyEnd == -1) {
                return metricLabels;
            }
            int valueStart = keyEnd + 1;
            int valueEnd = firstIdxOf(seriesKey, valueStart, labelsDelimiter);
            if (valueEnd == -1) {
                valueEnd = seriesKey.length();
                metricLabels.addLabel(seriesKey.substring(keyStart, keyEnd), seriesKey.substring(valueStart));
            } else {
                metricLabels.addLabel(seriesKey.substring(keyStart, keyEnd), seriesKey.substring(valueStart, valueEnd));
            }
            labelStart = valueEnd + 1;
        }
        return metricLabels;
    }

    private int firstIdxOf(String str, int start, char aChar) {
        int idx = start;
        while (idx < str.length()) {
            if (str.charAt(idx) == aChar) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

}
