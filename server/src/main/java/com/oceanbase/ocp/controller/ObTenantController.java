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
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.ob.tenant.QueryTenantParam;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.PaginatedResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obops.tenant.TenantOperationService;
import com.oceanbase.ocp.obops.tenant.TenantReplicaService;
import com.oceanbase.ocp.obops.tenant.TenantService;
import com.oceanbase.ocp.obops.tenant.TenantWhitelistService;
import com.oceanbase.ocp.obops.tenant.model.CheckTenantPasswordResult;
import com.oceanbase.ocp.obops.tenant.model.TenantInfo;
import com.oceanbase.ocp.obops.tenant.model.TenantPreCheckResult;
import com.oceanbase.ocp.obops.tenant.param.AddReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.CreatePasswordInVaultParam;
import com.oceanbase.ocp.obops.tenant.param.CreateTenantParam;
import com.oceanbase.ocp.obops.tenant.param.DeleteReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.ModifyPrimaryZoneParam;
import com.oceanbase.ocp.obops.tenant.param.ModifyReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.ModifyTenantDescriptionParam;
import com.oceanbase.ocp.obops.tenant.param.ModifyWhitelistParam;
import com.oceanbase.ocp.obops.tenant.param.TenantChangePasswordParam;
import com.oceanbase.ocp.security.annotation.SensitiveApi;
import com.oceanbase.ocp.task.model.TaskInstance;

import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ObTenantController {

    @Autowired
    private TenantService tenantService;
    @Autowired
    private TenantOperationService tenantOperationService;
    @Autowired
    private TenantReplicaService tenantReplicaService;
    @Autowired
    private TenantWhitelistService tenantWhitelistService;

    @PostMapping(value = "/ob/tenants", produces = {"application/json"}, consumes = {"application/json"})
    @SensitiveApi
    public SuccessResponse<TaskInstance> createTenant(@Valid @RequestBody CreateTenantParam param) {
        return ResponseBuilder.single(tenantOperationService.createTenant(param));
    }

    @DeleteMapping(value = "/ob/tenants/{tenantId:[\\d]+}", produces = {"application/json"})
    public NoDataResponse deleteTenant(@PathVariable("tenantId") Long tenantId) {
        tenantOperationService.deleteTenant(tenantId);
        return ResponseBuilder.noData();
    }

    @GetMapping(value = "/ob/tenants", produces = {"application/json"})
    public PaginatedResponse<TenantInfo> listTenants(
            @PageableDefault(size = Integer.MAX_VALUE, sort = {"id"}, direction = Direction.DESC) Pageable pageable,
            @Valid @RequestParam(value = "name", required = false) String name,
            @Valid @RequestParam(value = "mode", required = false) List<TenantMode> modeList,
            @Valid @RequestParam(value = "locked", required = false) Boolean locked,
            @Valid @RequestParam(value = "readonly", required = false) Boolean readonly,
            @Valid @RequestParam(value = "status", required = false) List<TenantStatus> statusList) {
        QueryTenantParam param = QueryTenantParam.builder().name(name).modeList(modeList).locked(locked)
                .readonly(readonly).statusList(statusList).build();
        return ResponseBuilder.paginated(tenantService.listTenant(param, pageable));
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}", produces = {"application/json"})
    public SuccessResponse<TenantInfo> getTenant(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.single(tenantService.getTenantInfo(tenantId));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/lock", produces = {"application/json"})
    public NoDataResponse lockTenant(@PathVariable("tenantId") Long tenantId) {
        tenantOperationService.lockTenant(tenantId);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/unlock", produces = {"application/json"})
    public NoDataResponse unlockTenant(@PathVariable("tenantId") Long tenantId) {
        tenantOperationService.unlockTenant(tenantId);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/changePassword", produces = {"application/json"})
    @SensitiveApi
    public NoDataResponse changePasswordForTenant(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody TenantChangePasswordParam param) {
        tenantOperationService.changePassword(tenantId, param);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/createOrReplacePassword", produces = {"application/json"})
    @SensitiveApi
    public NoDataResponse createOrReplacePassword(@Valid @RequestBody CreatePasswordInVaultParam param) {
        tenantOperationService.createOrReplacePassword(param);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/checkTenantPassword", produces = {"application/json"})
    @SensitiveApi
    public SuccessResponse<CheckTenantPasswordResult> checkTenantPassword(
            @Valid @RequestBody CreatePasswordInVaultParam param) {
        return ResponseBuilder.single(tenantOperationService.checkTenantPassword(param));
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/preCheck", produces = {"application/json"})
    public SuccessResponse<TenantPreCheckResult> tenantPreCheck(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.single(tenantService.tenantPreCheck(tenantId));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/modifyPrimaryZone", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse modifyPrimaryZone(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody ModifyPrimaryZoneParam param) {
        tenantOperationService.modifyPrimaryZone(tenantId, param.getPrimaryZone());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/modifyWhitelist", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse modifyWhitelist(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody ModifyWhitelistParam param) {
        tenantWhitelistService.modifyWhitelist(tenantId, param.getWhitelist());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/modifyDescription", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse modifyTenantDescription(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody ModifyTenantDescriptionParam param) {
        tenantService.modifyDescription(tenantId, param.getDescription());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/addReplica", produces = {"application/json"},
            consumes = {"application/json"})
    public SuccessResponse<TaskInstance> addReplica(@PathVariable("tenantId") Long tenantId,
            @NotEmpty @Valid @RequestBody List<AddReplicaParam> paramList) {
        return ResponseBuilder.single(tenantReplicaService.addReplica(tenantId, paramList));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/deleteReplica", produces = {"application/json"},
            consumes = {"application/json"})
    public SuccessResponse<TaskInstance> deleteReplica(@PathVariable("tenantId") Long tenantId,
            @NotEmpty @Valid @RequestBody List<DeleteReplicaParam> paramList) {
        return ResponseBuilder.single(tenantReplicaService.deleteReplica(tenantId, paramList));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/modifyReplica", produces = {"application/json"},
            consumes = {"application/json"})
    public SuccessResponse<TaskInstance> modifyReplica(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody List<ModifyReplicaParam> paramList) {
        return ResponseBuilder.single(tenantReplicaService.modifyReplica(tenantId, paramList));
    }

}
