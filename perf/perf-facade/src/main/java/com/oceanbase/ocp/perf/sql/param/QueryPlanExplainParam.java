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
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.validatePlanUid;

import java.time.OffsetDateTime;

import com.oceanbase.ocp.common.util.time.Interval;
import com.oceanbase.ocp.obsdk.operator.sql.model.PlanUid;

import lombok.Data;
import lombok.NonNull;

@Data
public class QueryPlanExplainParam {

    @NonNull
    public Interval interval;

    @NonNull
    public Long clusterId;

    @NonNull
    public Long tenantId;

    @NonNull
    public PlanUid uid;

    @lombok.Builder(builderClassName = "Builder")
    public QueryPlanExplainParam(OffsetDateTime startTime, OffsetDateTime endTime, Long clusterId, Long tenantId,
            String planUid) {
        this.interval = validateInterval(startTime, endTime);
        this.clusterId = clusterId;
        this.tenantId = tenantId;
        this.uid = validatePlanUid(planUid);
    }
}

