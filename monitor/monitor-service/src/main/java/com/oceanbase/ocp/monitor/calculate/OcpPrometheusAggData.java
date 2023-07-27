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
package com.oceanbase.ocp.monitor.calculate;

import com.oceanbase.ocp.monitor.model.OcpPrometheusData;
import com.oceanbase.ocp.monitor.model.metric.OcpPrometheusMeasurement;

import lombok.Data;

@Data
public class OcpPrometheusAggData {

    boolean withData;
    double sum;
    double count;
    double max;
    double min;
    OcpPrometheusMeasurement measurement;

    public OcpPrometheusAggData() {
        this.withData = false;
        this.sum = 0;
        this.count = 0;
        this.max = 0;
        this.min = Double.MAX_VALUE;
    }

    public OcpPrometheusAggData(OcpPrometheusMeasurement measurement) {
        this.withData = false;
        this.sum = 0;
        this.count = 0;
        this.max = 0;
        this.min = Double.MAX_VALUE;
        this.measurement = measurement;
    }

    public void addValue(OcpPrometheusData data) {
        this.withData = true;
        double v = data.getValue();
        this.sum += Double.isNaN(v) ? 0 : v;
        this.count += 1;
        this.max = Double.isNaN(v) ? max : Math.max(v, max);
        this.min = Double.isNaN(v) ? min : Math.min(v, min);
    }

    public double getAvg() {
        return this.count > 0 ? this.sum / this.count : 0;
    }

    public boolean hasData() {
        return this.withData;
    }

}
