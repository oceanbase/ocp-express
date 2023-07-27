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

import com.oceanbase.ocp.monitor.constants.VectorSelectorType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcpPrometheusLabel {

    private String labelKey;
    private String labelValue;
    private VectorSelectorType labelMatch = VectorSelectorType.MATCH_EQ;

    public OcpPrometheusLabel(String labelKey, String labelValue) {
        this.labelKey = labelKey;
        this.labelValue = labelValue;
    }

    public OcpPrometheusLabel(String labelKey, String labelMatch, String labelValue) {
        this.labelKey = labelKey;
        this.labelMatch = VectorSelectorType.fromValue(labelMatch);
        this.labelValue = labelValue;
    }

    public String labelStr() {
        return String.format("%s_%s_%s", labelKey, labelMatch.getValue(), labelValue);
    }
}
