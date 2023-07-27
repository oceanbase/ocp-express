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

package com.oceanbase.ocp.obsdk.operator.sql.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanEntity {

    @JsonProperty("CollectTimeUs")
    public Long collectTime;

    @JsonProperty("ObClusterId")
    public Long obClusterId;

    @JsonProperty("ClusterName")
    public String clusterName;

    @JsonProperty("ObTenantId")
    public Long obTenantId;

    @JsonProperty("ObDbId")
    public Long obDbId;

    @JsonProperty("ObServerId")
    public Long obServerId;

    @JsonProperty("PlanId")
    public Long planId;

    @JsonProperty("FirstLoadTimeUs")
    public Long firstLoadTime;

    @JsonProperty("SqlId")
    public String sqlId;

    @JsonProperty("Type")
    public Integer type;

    @JsonProperty("PlanHash")
    public Long planHash;

    @JsonProperty("SchemaVersion")
    public Long schemaVersion;

    @JsonProperty("MergedVersion")
    public Long mergedVersion;

    @JsonProperty("PlanSize")
    public Integer planSize;

    @JsonProperty("CreateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Timestamp createTime;

    @JsonProperty("OutlineData")
    public String outlineData;

    @JsonProperty("OutlineId")
    public Long outlineId;

    @JsonProperty("PlanUnionHash")
    public String planUnionHash;
}
