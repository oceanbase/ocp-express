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

package com.oceanbase.ocp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obops.tenant.TenantCompactionService;
import com.oceanbase.ocp.obops.tenant.model.TenantCompaction;
import com.oceanbase.ocp.obops.tenant.model.TenantCompactionHistory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ObTenantCompactionController {

    @Autowired
    private TenantCompactionService compactionService;

    @GetMapping("/ob/tenants/{tenantId:[\\d]+}/compaction")
    public SuccessResponse<TenantCompaction> getTenantCompaction(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.single(compactionService.getTenantCompaction(tenantId));
    }

    @PostMapping("/ob/tenants/{tenantId:[\\d]+}/triggerCompaction")
    public NoDataResponse triggerTenantCompaction(@PathVariable("tenantId") Long tenantId) {
        compactionService.triggerTenantCompaction(tenantId);
        return ResponseBuilder.noData();
    }

    @PostMapping("/ob/tenants/{tenantId:[\\d]+}/clearCompactionError")
    public NoDataResponse clearCompactionError(@PathVariable("tenantId") Long tenantId) {
        compactionService.clearCompactionError(tenantId);
        return ResponseBuilder.noData();
    }

    @GetMapping("/ob/tenants/topCompactions")
    public IterableResponse<TenantCompactionHistory> topCompactions(@RequestParam("top") Integer top,
            @RequestParam("times") Integer times) {
        return ResponseBuilder.iterable(compactionService.topCompactions(top, times));
    }
}
