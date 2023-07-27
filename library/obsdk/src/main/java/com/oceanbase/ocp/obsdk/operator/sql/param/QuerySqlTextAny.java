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

package com.oceanbase.ocp.obsdk.operator.sql.param;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(builderClassName = "Builder")
public class QuerySqlTextAny {

    @NonNull
    @lombok.Builder.Default
    public Integer timeout = 30_000_000;

    public Long startTimeUs;

    public Long endTimeUs;

    @NonNull
    public String sqlId;

    public Long obClusterId;

    public String clusterName;

    public Long obTenantId;

    public Long obDbId;

    public Long obUserId;

    public Integer length;
}
