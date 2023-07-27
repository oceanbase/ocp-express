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

package com.oceanbase.ocp.obops.cluster.model;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PerformanceStats {

    @JsonProperty("active_session")
    private Double activeSession;

    @JsonProperty("qps")
    private Double qps;

    @JsonProperty("tps")
    private Double tps;

    @JsonProperty("unit_num")
    private Double unitNum;

    public void addFrom(PerformanceStats perfStats) {
        Validate.notNull(perfStats, "perfStats is null");
        this.activeSession = safeAdd(this.activeSession, perfStats.getActiveSession());
        this.qps = safeAdd(this.qps, perfStats.getQps());
        this.tps = safeAdd(this.tps, perfStats.getTps());
        this.unitNum = safeAdd(this.unitNum, perfStats.getUnitNum());
    }

    private Double safeAdd(Double src, Double target) {
        if (src == null) {
            return target;
        }
        if (target == null) {
            return src;
        }
        return src + target;
    }
}
