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
package com.oceanbase.ocp.monitor.model.metric;

import java.util.Map;
import java.util.Objects;

import com.oceanbase.ocp.monitor.util.MetricParser;

import lombok.Data;

@Data
public class Metric {

    private static final char LABELS_DELIMITER = '|';
    private static final char KEY_VALUE_DELIMITER = '=';
    private static final MetricParser METRIC_PARSER = new MetricParser(LABELS_DELIMITER, KEY_VALUE_DELIMITER);

    private final String name;
    private final MetricLabels labels;
    private transient String seriesKey;
    private final transient int hashCode;

    public Metric(String name, Map<String, String> labels, boolean createDefaultLabel) {
        this.name = name;
        if (labels instanceof MetricLabels) {
            this.labels = (MetricLabels) labels;
        } else {
            this.labels = new MetricLabels();
            if (labels != null) {
                this.labels.putAll(labels);
            }
        }
        if (this.labels.isEmpty() && createDefaultLabel) {
            this.labels.put("default", "default");
        }
        this.hashCode = Objects.hash(name, labels);
    }

    public Metric(String name, Map<String, String> labels) {
        this(name, labels, true);
    }

    public Metric(String name, MetricLabels labels, String seriesKey) {
        this.name = name;
        this.labels = labels;
        this.seriesKey = seriesKey;
        this.hashCode = Objects.hash(name, labels);
    }

    public static Metric parse(String seriesKey) {
        return METRIC_PARSER.parse(seriesKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Metric metric = (Metric) o;
        if (hashCode != metric.hashCode) {
            return false;
        }
        return name.equals(metric.name) && labels.equals(metric.labels);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public String getName() {
        return name;
    }

    public MetricLabels getLabels() {
        return labels;
    }

    public String getSeriesKey() {
        if (seriesKey == null) {
            seriesKey = calcSeriesKey();
        }
        return seriesKey;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                ", labels=" + labels +
                '}';
    }

    /**
     * metric|label1=value1|label2=value2...
     */
    private String calcSeriesKey() {
        return name + "|" + labels.getSeriesKeyPostfix();
    }
}
