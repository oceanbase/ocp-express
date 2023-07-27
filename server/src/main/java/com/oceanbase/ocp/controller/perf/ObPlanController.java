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

package com.oceanbase.ocp.controller.perf;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.perf.sql.PlanExplainService;
import com.oceanbase.ocp.perf.sql.PlanRawStatService;
import com.oceanbase.ocp.perf.sql.model.PlanExplain;
import com.oceanbase.ocp.perf.sql.model.PlanStatGroup;
import com.oceanbase.ocp.perf.sql.param.QueryPlanExplainParam;
import com.oceanbase.ocp.perf.sql.param.QueryTopPlanParam;

@RestController
@Validated
@RequestMapping("/api/v1/ob/tenants/{tenantId}")
public class ObPlanController {

    @Autowired
    PlanRawStatService planRawStatService;

    @Autowired
    private PlanExplainService planExplainService;

    @GetMapping(value = "/sqls/{sqlId}/topPlanGroup", produces = {"application/json"})
    public IterableResponse<PlanStatGroup> topPlanGroup(@PathVariable("tenantId") Long tenantId,
            @RequestParam(value = "dbName", required = false) String dbName,
            @PathVariable("sqlId") String sqlId,
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime") OffsetDateTime endTime) {
        QueryTopPlanParam param = QueryTopPlanParam.builder()
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(tenantId)
                .sqlId(sqlId)
                .dbName(dbName)
                .attachPlanExplain(true)
                .build();
        List<PlanStatGroup> res = planRawStatService.topPlanGroup(param);
        return ResponseBuilder.iterable(res);
    }

    @GetMapping(value = "/plans/{planUid}/explain", produces = {"application/json"})
    public SuccessResponse<PlanExplain> planExplain(
            @PathVariable("tenantId") Long tenantId,
            @PathVariable("planUid") String planUid,
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime") OffsetDateTime endTime) {
        return ResponseBuilder.single(planExplainService.getExplain(QueryPlanExplainParam.builder()
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(tenantId)
                .planUid(planUid)
                .build()));
    }
}
