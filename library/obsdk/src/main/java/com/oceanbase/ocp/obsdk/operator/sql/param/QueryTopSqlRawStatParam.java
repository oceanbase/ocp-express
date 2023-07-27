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
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(builderClassName = "Builder")
public class QueryTopSqlRawStatParam {

    @NonNull
    @lombok.Builder.Default
    public Integer timeout = 30_000_000;

    @NonNull
    public Long startTimeUs;

    @NonNull
    public Long endTimeUs;

    public Long obTenantId;

    @Default
    public String serverIp = null;

    @Default
    public Integer serverPort = -1;

    public String sqlId;

    @lombok.Builder.Default
    public boolean includeInner = false;

    @lombok.Builder.Default
    public Long limit = 2000L;

    public String sqlText;
}
