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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.perf.sql.OutlineService;
import com.oceanbase.ocp.perf.sql.model.BatchConcurrentLimitResult;
import com.oceanbase.ocp.perf.sql.model.BatchDropOutlineResult;
import com.oceanbase.ocp.perf.sql.model.Outline;
import com.oceanbase.ocp.perf.sql.param.BatchConcurrentLimitRequest;
import com.oceanbase.ocp.perf.sql.param.BatchDropOutlineRequest;

@RestController
@Validated
@RequestMapping("/api/v1/ob/tenants/{tenantId}")
public class ObOutlineController {

    @Autowired
    private OutlineService outlineService;

    @GetMapping("/outlines")
    public IterableResponse<Outline> getSqlOutline(@PathVariable("tenantId") Long tenantId,
            @RequestParam(value = "dbName", required = false) String dbName,
            @RequestParam(value = "sqlId", required = false) String sqlId,
            @RequestParam(value = "startTime", required = false) OffsetDateTime startTime,
            @RequestParam(value = "endTime", required = false) OffsetDateTime endTime,
            @RequestParam(value = "attachPerfData", required = false) Boolean attachPerfData) {
        return ResponseBuilder
                .iterable(outlineService.getOutline(tenantId, dbName, sqlId, startTime, endTime, attachPerfData));
    }

    @PostMapping("/outlines")
    public SuccessResponse<BatchConcurrentLimitResult> batchCreateOutline(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody BatchConcurrentLimitRequest param) {
        return ResponseBuilder.single(outlineService.batchConcurrentLimit(tenantId, param));
    }

    @PostMapping("/outlines/batchDrop")
    public SuccessResponse<BatchDropOutlineResult> batchDropSqlOutline(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody BatchDropOutlineRequest param) {
        return ResponseBuilder.single(outlineService.batchDropOutline(tenantId, param));
    }

}
