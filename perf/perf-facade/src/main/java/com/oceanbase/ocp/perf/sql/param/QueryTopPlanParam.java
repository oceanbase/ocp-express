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

package com.oceanbase.ocp.perf.sql.param;

import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.validateInterval;

import java.time.OffsetDateTime;

import com.oceanbase.ocp.common.util.time.Interval;

import lombok.Data;
import lombok.NonNull;

@Data
public class QueryTopPlanParam {

    @NonNull
    public Interval interval;

    @NonNull
    public Long tenantId;

    @NonNull
    public String sqlId;

    public Long serverId;

    public String dbName;

    private boolean attachPlanExplain;

    @lombok.Builder(builderClassName = "Builder")
    public QueryTopPlanParam(OffsetDateTime startTime, OffsetDateTime endTime, Long tenantId,
            String sqlId, Long serverId, String dbName, boolean attachPlanExplain) {
        this.interval = validateInterval(startTime, endTime);
        this.tenantId = tenantId;
        this.sqlId = sqlId;
        this.serverId = serverId;
        this.dbName = dbName;
        this.attachPlanExplain = attachPlanExplain;
    }
}

