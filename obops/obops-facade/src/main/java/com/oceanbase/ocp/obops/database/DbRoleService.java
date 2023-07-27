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

package com.oceanbase.ocp.obops.database;

import java.util.List;

import com.oceanbase.ocp.obops.database.model.DbRole;
import com.oceanbase.ocp.obops.database.param.CreateDbRoleParam;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;

public interface DbRoleService {

    List<DbRole> listRoles(Long obTenantId);

    DbRole getRole(Long obTenantId, String roleName);

    DbRole createRole(Long obTenantId, CreateDbRoleParam param);

    void deleteRole(Long obTenantId, String roleName);

    void grantGlobalPrivilege(Long obTenantId, String roleName, List<String> privilegeStrings);

    void revokeGlobalPrivilege(Long obTenantId, String roleName, List<String> privilegeStrings);

    void modifyGlobalPrivilege(Long obTenantId, String roleName, List<String> privilegeStrings);

    void grantRole(Long obTenantId, String roleName, List<String> roles);

    void revokeRole(Long obTenantId, String roleName, List<String> roles);

    void modifyRole(Long obTenantId, String roleName, List<String> roles);

    void grantObjectPrivilege(Long obTenantId, String roleName, List<ObjectPrivilege> objectPrivileges);

    void revokeObjectPrivilege(Long obTenantId, String roleName, List<ObjectPrivilege> objectPrivileges);

    void modifyObjectPrivilege(Long obTenantId, String roleName, List<ObjectPrivilege> objectPrivileges);
}
