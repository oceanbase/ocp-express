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

package com.oceanbase.ocp.obops.tenant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitConfig {

    private Double maxCpuCoreCount;
    private Double minCpuCoreCount;

    private Long maxMemoryByte;

    @JsonProperty
    public Long getMaxMemorySize() {
        if (maxMemoryByte == null) {
            return null;
        }
        return maxMemoryByte / (1024 * 1024 * 1024);
    }

    private Long minMemoryByte;

    @JsonProperty
    public Long getMinMemorySize() {
        if (minMemoryByte == null) {
            return null;
        }
        return minMemoryByte / (1024 * 1024 * 1024);
    }

    @Deprecated
    @JsonIgnore
    private Long maxDiskSizeByte;

    @Deprecated
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Long maxDiskSize;

    @JsonIgnore
    private Long maxIops;

    @JsonIgnore
    private Long minIops;

    @Deprecated
    @JsonIgnore
    private Long maxSessionNum;

    @JsonIgnore
    private String name;
}
