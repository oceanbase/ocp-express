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

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obops.database.DbObjectService;
import com.oceanbase.ocp.obops.database.DbRoleService;
import com.oceanbase.ocp.obops.database.DbUserService;
import com.oceanbase.ocp.obops.database.model.DbRole;
import com.oceanbase.ocp.obops.database.model.DbUser;
import com.oceanbase.ocp.obops.database.param.CreateDbRoleParam;
import com.oceanbase.ocp.obops.database.param.CreateDbUserParam;
import com.oceanbase.ocp.obops.database.param.GrantObjectPrivilegeParam;
import com.oceanbase.ocp.obops.database.param.GrantRoleParam;
import com.oceanbase.ocp.obops.database.param.ModifyDbPrivilegeParam;
import com.oceanbase.ocp.obops.database.param.ModifyDbUserPasswordParam;
import com.oceanbase.ocp.obops.database.param.ModifyGlobalPrivilegeParam;
import com.oceanbase.ocp.obops.database.param.ModifyObjectPrivilegeParam;
import com.oceanbase.ocp.obops.database.param.ModifyRoleParam;
import com.oceanbase.ocp.obops.database.param.RevokeObjectPrivilegeParam;
import com.oceanbase.ocp.obops.database.param.RevokeRoleParam;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;
import com.oceanbase.ocp.security.annotation.SensitiveApi;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/v1")
public class ObUserController {

    @Resource
    private DbUserService dbUserService;

    @Resource
    private DbRoleService dbRoleService;

    @Resource
    private DbObjectService dbObjectService;

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users", produces = {"application/json"})
    public IterableResponse<DbUser> listDbUsers(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.iterable(dbUserService.listUsers(tenantId));
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}", produces = {"application/json"})
    public SuccessResponse<DbUser> getDbUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username) {
        return ResponseBuilder.single(dbUserService.getUser(tenantId, username));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users", produces = {"application/json"},
            consumes = {"application/json"})
    @SensitiveApi
    public SuccessResponse<DbUser> createDbUser(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody CreateDbUserParam param) {
        return ResponseBuilder.single(dbUserService.createUser(tenantId, param));
    }

    @DeleteMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}", produces = {"application/json"})
    public NoDataResponse deleteDbUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username) {
        dbUserService.deleteUser(tenantId, username);
        return ResponseBuilder.noData();
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles", produces = {"application/json"})
    public IterableResponse<DbRole> listDbRoles(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.iterable(dbRoleService.listRoles(tenantId));
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}", produces = {"application/json"})
    public SuccessResponse<DbRole> getDbRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName) {
        return ResponseBuilder.single(dbRoleService.getRole(tenantId, roleName));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles", produces = {"application/json"},
            consumes = {"application/json"})
    public SuccessResponse<DbRole> createDbRole(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody CreateDbRoleParam param) {
        return ResponseBuilder.single(dbRoleService.createRole(tenantId, param));
    }

    @DeleteMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}", produces = {"application/json"})
    public NoDataResponse deleteDbRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName) {
        dbRoleService.deleteRole(tenantId, roleName);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/modifyGlobalPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse modifyGlobalPrivilege(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody ModifyGlobalPrivilegeParam param) {
        dbUserService.modifyGlobalPrivilege(tenantId, username, param.getGlobalPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}/modifyGlobalPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse modifyGlobalPrivilegeFromRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName, @Valid @RequestBody ModifyGlobalPrivilegeParam param) {
        dbRoleService.modifyGlobalPrivilege(tenantId, roleName, param.getGlobalPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/grantRole", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse grantRoleToUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody GrantRoleParam param) {
        dbUserService.grantRole(tenantId, username, param.getRoles());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/revokeRole", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse revokeRoleFromUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody RevokeRoleParam param) {
        dbUserService.revokeRole(tenantId, username, param.getRoles());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/modifyRole", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse modifyRoleFromUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody ModifyRoleParam param) {
        dbUserService.modifyRole(tenantId, username, param.getRoles());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}/grantRole", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse grantRoleToRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName, @Valid @RequestBody GrantRoleParam param) {
        dbRoleService.grantRole(tenantId, roleName, param.getRoles());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}/revokeRole", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse revokeRoleFromRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName, @Valid @RequestBody RevokeRoleParam param) {
        dbRoleService.revokeRole(tenantId, roleName, param.getRoles());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}/modifyRole", produces = {"application/json"},
            consumes = {"application/json"})
    public NoDataResponse modifyRoleFromRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName, @Valid @RequestBody ModifyRoleParam param) {
        dbRoleService.modifyRole(tenantId, roleName, param.getRoles());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/modifyDbPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse modifyDbPrivilege(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody ModifyDbPrivilegeParam param) {
        dbUserService.modifyDbPrivilege(tenantId, username, param.getDbPrivileges());
        return ResponseBuilder.noData();
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/objects", produces = {"application/json"})
    public IterableResponse<DbObject> listDbObjects(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.iterable(dbObjectService.listObjects(tenantId));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/grantObjectPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse grantObjectPrivilegeToUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody GrantObjectPrivilegeParam param) {
        dbUserService.grantObjectPrivilege(tenantId, username, param.getObjectPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/revokeObjectPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse revokeObjectPrivilegeFromUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody RevokeObjectPrivilegeParam param) {
        dbUserService.revokeObjectPrivilege(tenantId, username, param.getObjectPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/modifyObjectPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse modifyObjectPrivilegeFromUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody ModifyObjectPrivilegeParam param) {
        dbUserService.modifyObjectPrivilege(tenantId, username, param.getObjectPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}/grantObjectPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse grantObjectPrivilegeToRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName, @Valid @RequestBody GrantObjectPrivilegeParam param) {
        dbRoleService.grantObjectPrivilege(tenantId, roleName, param.getObjectPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}/revokeObjectPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse revokeObjectPrivilegeFromRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName, @Valid @RequestBody RevokeObjectPrivilegeParam param) {
        dbRoleService.revokeObjectPrivilege(tenantId, roleName, param.getObjectPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/roles/{roleName}/modifyObjectPrivilege",
            produces = {"application/json"}, consumes = {"application/json"})
    public NoDataResponse modifyObjectPrivilegeFromRole(@PathVariable("tenantId") Long tenantId,
            @PathVariable("roleName") String roleName, @Valid @RequestBody ModifyObjectPrivilegeParam param) {
        dbRoleService.modifyObjectPrivilege(tenantId, roleName, param.getObjectPrivileges());
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/changePassword",
            produces = {"application/json"}, consumes = {"application/json"})
    @SensitiveApi
    public NoDataResponse changeDbUserPassword(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username, @Valid @RequestBody ModifyDbUserPasswordParam param) {
        dbUserService.modifyPassword(tenantId, username, param);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/lock", produces = {"application/json"})
    public NoDataResponse lockDbUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username) {
        dbUserService.lockUser(tenantId, username);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/users/{username}/unlock", produces = {"application/json"})
    public NoDataResponse unlockDbUser(@PathVariable("tenantId") Long tenantId,
            @PathVariable("username") String username) {
        dbUserService.unlockUser(tenantId, username);
        return ResponseBuilder.noData();
    }

}
