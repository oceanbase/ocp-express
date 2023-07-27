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
package com.oceanbase.ocp.monitor.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.oceanbase.ocp.monitor.model.metric.OcpPrometheusMeasurement;

import lombok.Data;

@Data
public class OcpPrometheusQueryResult {

    private boolean isScalar;
    private double value;
    private OcpPrometheusMeasurement measurement;
    private int interval;
    private LinkedList<OcpPrometheusData> data;
    private OcpPrometheusDataContainer scanDataIter;

    public OcpPrometheusQueryResult(OcpPrometheusMeasurement measurement, int interval,
            OcpPrometheusDataContainer scanDataIter) {
        this.measurement = measurement;
        this.interval = interval;
        this.data = new LinkedList<>();
        this.scanDataIter = scanDataIter;
        this.isScalar = false;
    }

    public OcpPrometheusQueryResult(OcpPrometheusMeasurement measurement, int interval, List<OcpPrometheusData> data) {
        this.measurement = measurement;
        this.interval = interval;
        this.data = new LinkedList<>(data);
        this.isScalar = false;
    }

    public OcpPrometheusQueryResult(double value) {
        this.measurement = new OcpPrometheusMeasurement();
        this.isScalar = true;
        this.data = new LinkedList<>();
        this.value = value;
    }

    public OcpPrometheusQueryResult() {}

    public boolean getIsScalar() {
        return this.isScalar;
    }

    @JsonIgnore
    public OcpPrometheusData getLastData() {
        return this.data.size() > 0 ? this.data.getLast() : new OcpPrometheusData();
    }

    public int getDataSize() {
        return this.data.size();
    }

    public void prepareForTs(long ts) {
        Iterator<OcpPrometheusData> dataIter = this.data.iterator();
        while (dataIter.hasNext()) {
            OcpPrometheusData d = dataIter.next();
            if (d.getTimestamp() < ts - this.interval) {
                dataIter.remove();
            } else {
                break;
            }
        }
        if (this.scanDataIter.isEmpty()) {
            return;
        }
        Iterator<OcpPrometheusData> iterator = this.scanDataIter.iterator();
        while (iterator.hasNext()) {
            OcpPrometheusData v = iterator.next();
            if (v.getTimestamp() >= ts - this.interval && v.getTimestamp() <= ts) {
                this.data.add(v);
                iterator.remove();
            }
            if (v.getTimestamp() >= ts) {
                break;
            }
        }
    }

}
