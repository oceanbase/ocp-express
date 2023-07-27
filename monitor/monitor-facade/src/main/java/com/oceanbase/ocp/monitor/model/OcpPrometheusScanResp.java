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

import com.oceanbase.ocp.monitor.model.metric.OcpPrometheusMeasurement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcpPrometheusScanResp {

    private String measurement;
    private OcpPrometheusMeasurement measurementParse;

    /**
     * data iterator
     */
    private OcpPrometheusDataContainer data;

    public OcpPrometheusScanResp(String measurement, OcpPrometheusDataContainer data) {
        this.measurement = measurement;
        this.data = data;
        this.measurementParse = new OcpPrometheusMeasurement(measurement);
    }
}
