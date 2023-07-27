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

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.obops.parameter.TenantParameterService;
import com.oceanbase.ocp.obops.parameter.model.TenantParameter;
import com.oceanbase.ocp.obops.parameter.model.TenantParameterInfo;
import com.oceanbase.ocp.obops.parameter.param.TenantParameterParam;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/v1")
public class ObTenantParameterController {

    @Autowired
    private TenantParameterService tenantParameterService;

    @GetMapping(value = "/ob/tenants/parameterInfo", produces = {"application/json"})
    public IterableResponse<TenantParameterInfo> listTenantParameterInfo() {
        return ResponseBuilder.iterable(tenantParameterService.listParameterInfo());
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/parameters", produces = {"application/json"})
    public IterableResponse<TenantParameter> listTenantParameters(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.iterable(tenantParameterService.listParameters(tenantId));
    }

    @PatchMapping(value = "/ob/tenants/{tenantId:[\\d]+}/parameters", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse updateTenantParameter(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody List<TenantParameterParam> paramList) {
        tenantParameterService.updateParameters(tenantId, paramList);
        return ResponseBuilder.noData();
    }

}
