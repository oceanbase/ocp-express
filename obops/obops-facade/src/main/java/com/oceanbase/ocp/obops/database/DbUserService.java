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

import com.oceanbase.ocp.obops.database.model.DbUser;
import com.oceanbase.ocp.obops.database.param.CreateDbUserParam;
import com.oceanbase.ocp.obops.database.param.DbPrivilegeParam;
import com.oceanbase.ocp.obops.database.param.ModifyDbUserPasswordParam;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;

public interface DbUserService {

    List<DbUser> listUsers(Long obTenantId);

    DbUser getUser(Long obTenantId, String username);

    DbUser createUser(Long obTenantId, CreateDbUserParam param);

    void deleteUser(Long obTenantId, String username);

    void grantGlobalPrivilege(Long obTenantId, String username, List<String> privilegeStrings);

    void revokeGlobalPrivilege(Long obTenantId, String username, List<String> privilegeStrings);

    void modifyGlobalPrivilege(Long obTenantId, String username, List<String> privilegeStrings);

    void grantDbPrivilege(Long obTenantId, String username, List<DbPrivilegeParam> dbPrivileges);

    void revokeDbPrivilege(Long obTenantId, String username, List<DbPrivilegeParam> dbPrivileges);

    void modifyDbPrivilege(Long obTenantId, String username, List<DbPrivilegeParam> dbPrivileges);

    void grantRole(Long obTenantId, String username, List<String> roles);

    void revokeRole(Long obTenantId, String username, List<String> roles);

    void modifyRole(Long obTenantId, String username, List<String> roles);

    void grantObjectPrivilege(Long obTenantId, String username, List<ObjectPrivilege> objectPrivileges);

    void revokeObjectPrivilege(Long obTenantId, String username, List<ObjectPrivilege> objectPrivileges);

    void modifyObjectPrivilege(Long obTenantId, String username, List<ObjectPrivilege> objectPrivileges);

    void modifyPassword(Long obTenantId, String username, ModifyDbUserPasswordParam param);

    void lockUser(Long obTenantId, String username);

    void unlockUser(Long obTenantId, String username);
}
