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

package com.oceanbase.ocp.perf.sql.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.perf.sql.util.SqlStatUtils;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PlanStatGroup {

    private String planHash;

    private String planUnionHash;

    private List<PlanStatDetail> plans;

    public Long mergedVersion;

    public OffsetDateTime firstLoadTime;

    public String planType;

    public Double hitPercentage;

    public Double avgCpuTime;

    public PlanExplain planExplain;

    public Long executions;

    public Long timeoutCount;

    public String querySql;

    public static PlanStatGroup of(List<PlanStatDetail> plans) {
        Validate.notEmpty(plans, "Plan list can't be empty");
        PlanStatGroupBuilder builder = PlanStatGroup.builder();
        builder.plans(plans);
        builder.planHash(plans.get(0).planHash);
        builder.planUnionHash(plans.get(0).planUnionHash);
        builder.planType(plans.get(0).getPlanType());
        builder.mergedVersion(0L);
        builder.firstLoadTime(plans.stream().map(PlanStatDetail::getFirstLoadTime).max(OffsetDateTime::compareTo)
                .orElse(OffsetDateTime.now()));
        Long totalCount = plans.stream().mapToLong(PlanStatBase::getExecutions).sum();
        Double totalCpuTime = plans.stream().mapToDouble(plan -> plan.getExecutions() * plan.cpuTime.longValue()).sum();
        builder.avgCpuTime(SqlStatUtils.avg2(totalCpuTime.longValue(), totalCount));
        builder.executions(totalCount);
        Long totalTimeoutCount = plans.stream().mapToLong(PlanStatBase::getTimeoutCount).sum();
        builder.timeoutCount(totalTimeoutCount);
        return builder.build();
    }
}
