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

import java.util.List;

import com.oceanbase.ocp.monitor.param.OcpPrometheusLabel;

import lombok.Data;

@Data
public class OcpPrometheusQueryParam {

    private String metric;

    private List<OcpPrometheusLabel> labels;

    private Long startTime;

    private Long endTime;

    private Long step;

    private boolean onlyCache;
}
