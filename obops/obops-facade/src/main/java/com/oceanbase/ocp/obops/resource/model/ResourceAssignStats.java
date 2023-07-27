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
package com.oceanbase.ocp.obops.resource.model;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Validated
@Data
public class ResourceAssignStats {

    private Long unitCount;

    @JsonProperty("cpuCoreTotal")
    private Double cpuCoreTotal;

    @JsonProperty("cpuCoreAssigned")
    private Double cpuCoreAssigned;

    @JsonProperty("cpuCoreAssignedPercent")
    private Double cpuCoreAssignedPercent;

    @JsonProperty("memoryTotal")
    private String memoryTotal;

    @JsonProperty("memoryAssigned")
    private String memoryAssigned;

    @JsonProperty("memoryInBytesTotal")
    private Long memoryInBytesTotal;

    @JsonProperty("memoryInBytesAssigned")
    private Long memoryInBytesAssigned;

    @JsonProperty("memoryAssignedPercent")
    private Double memoryAssignedPercent;

    @JsonProperty("diskTotal")
    private String diskTotal;

    @JsonProperty("diskAssigned")
    private String diskAssigned;

    @JsonProperty("diskInBytesTotal")
    private Long diskInBytesTotal;

    @JsonProperty("diskInBytesAssigned")
    private Long diskInBytesAssigned;

    @JsonProperty("diskAssignedPercent")
    private Double diskAssignedPercent;

}
