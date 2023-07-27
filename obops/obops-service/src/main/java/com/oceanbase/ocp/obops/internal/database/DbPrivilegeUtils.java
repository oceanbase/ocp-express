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

package com.oceanbase.ocp.obops.internal.database;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.MysqlGlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;
import com.oceanbase.ocp.obsdk.enums.ObjectType;
import com.oceanbase.ocp.obsdk.enums.OracleSystemPrivilege;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbPrivilegeUtils {

    public static void checkObjectPrivileges(List<ObjectPrivilege> objectPrivileges) {
        for (ObjectPrivilege objectPrivilege : objectPrivileges) {
            checkObjectPrivilege(objectPrivilege);
        }
    }

    private static void checkObjectPrivilege(ObjectPrivilege objectPrivilege) {
        DbObject object = objectPrivilege.getObject();
        ObjectType objectType = object.getObjectType();
        for (ObjectPrivilegeType privilege : objectPrivilege.getPrivileges()) {
            ExceptionUtils.illegalArgs(privilege.getSupportedObjectTypes().contains(objectType),
                    ErrorCodes.OB_USER_OBJECT_PRIVILEGE_INVALID, object.getFullName(), privilege);
        }
    }

    /**
     * Parse global privileges or system privileges according to tenant mode.
     *
     * @param privilegeStrings privilege strings
     * @param tenantMode tenant mode
     * @return list of global privileges or system privileges
     */
    public static List<GlobalPrivilege> parseGlobalPrivileges(List<String> privilegeStrings, TenantMode tenantMode) {
        Function<String, GlobalPrivilege> parseFunc = tenantMode == TenantMode.MYSQL
                ? MysqlGlobalPrivilege::fromValue
                : OracleSystemPrivilege::nullSafeFromValue;
        return privilegeStrings.stream().map(parseFunc).collect(Collectors.toList());
    }
}
