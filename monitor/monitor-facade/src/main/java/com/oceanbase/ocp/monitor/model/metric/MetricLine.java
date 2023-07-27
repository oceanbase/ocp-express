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

import java.time.format.DateTimeFormatter;

import com.oceanbase.ocp.common.util.time.TimeUtils;

import lombok.Data;

@Data
public class MetricLine {

    private final Metric metric;
    private final double value;
    private final long timestamp;

    public MetricLine(String name, MetricLabels labels, double value, long timestamp) {
        this.metric = new Metric(name, labels);
        this.value = value;
        this.timestamp = timestamp;
    }

    public MetricLine(Metric metric, double value, long timestamp) {
        this.metric = metric;
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeStr = formatter.format(TimeUtils.usToUtc(timestamp * 1000000));
        return metric.toString() + ", value: " + nf.format(value) + ", timestamp: " + timestamp + " : " + timeStr;
    }

}
