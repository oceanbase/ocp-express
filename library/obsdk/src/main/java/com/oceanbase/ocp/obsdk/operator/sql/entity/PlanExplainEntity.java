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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PlanExplainEntity {

    @JsonProperty("ObTenantId")
    public Long obTenantId;

    @JsonProperty("ObServerId")
    public Long obServerId;

    @JsonProperty("PlanId")
    public Long planId;

    @JsonProperty("Operator")
    public String operator;

    @JsonProperty("ObjectName")
    public String objectName;

    @JsonProperty("Rows")
    public Long rows;

    @JsonProperty("Cost")
    public Long cost;

    @JsonProperty("Property")
    public String property;

    @JsonProperty("PlanUnionHash")
    public String planUnionHash;
}
