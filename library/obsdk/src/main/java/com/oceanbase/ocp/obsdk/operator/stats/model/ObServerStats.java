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

package com.oceanbase.ocp.obsdk.operator.stats.model;

import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerKey;

import lombok.Data;

@Data
public class ObServerStats {

    private String svrIp;
    private Integer svrPort;

    private String zone;

    private Long leaderCount;

    private Long unitNum;

    private Double cpuCapacity;

    private Double cpuCapacityMax;

    private Double cpuAssigned;

    private Double cpuAssignedMax;

    private Long memCapacity;

    private Long memCapacityMax;

    private Long memAssigned;

    private Long memAssignedMax;

    private Long diskCapacity;

    private Long diskInUse;

    public ObServerKey getKey() {
        return new ObServerKey(svrIp, svrPort);
    }

    public Long getLeaderCount() {
        return defaultIfNull(leaderCount, 0L);
    }

    public Long getUnitNum() {
        return defaultIfNull(unitNum, 0L);
    }

    public Double getCpuCapacity() {
        return defaultIfNull(cpuCapacity, 0.0d);
    }

    public Double getCpuCapacityMax() {
        return defaultIfNull(cpuCapacityMax, 0.0d);
    }

    public Double getCpuAssigned() {
        return defaultIfNull(cpuAssigned, 0.0d);
    }

    public Double getCpuAssignedMax() {
        return defaultIfNull(cpuAssignedMax, 0.0d);
    }

    public Long getMemCapacity() {
        return defaultIfNull(memCapacity, 0L);
    }

    public Long getMemCapacityMax() {
        return defaultIfNull(memCapacityMax, 0L);
    }

    public Long getMemAssigned() {
        return defaultIfNull(memAssigned, 0L);
    }

    public Long getMemAssignedMax() {
        return defaultIfNull(memAssignedMax, 0L);
    }

    public Long getDiskCapacity() {
        return defaultIfNull(diskCapacity, 0L);
    }

    public Long getDiskInUse() {
        return defaultIfNull(diskInUse, 0L);
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
