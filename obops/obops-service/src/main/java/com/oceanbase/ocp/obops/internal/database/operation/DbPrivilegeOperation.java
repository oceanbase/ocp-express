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

package com.oceanbase.ocp.obops.internal.database.operation;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.obsdk.accessor.UserAccessor;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString(callSuper = true)
public class DbPrivilegeOperation extends PrivilegeOperation {

    private final String dbName;


    private final List<DbPrivType> dbPrivileges;

    @Override
    public boolean hasPrivileges() {
        return CollectionUtils.isNotEmpty(dbPrivileges);
    }

    @Override
    public void operate(UserAccessor userAccessor) {
        if (operationType == PrivilegeOperationType.GRANT) {
            userAccessor.grantDbPrivilege(grantee.getName(), dbName, dbPrivileges);
        } else {
            userAccessor.revokeDbPrivilege(grantee.getName(), dbName, dbPrivileges);
        }
    }

    @Override
    public String getPrivilegeString() {
        return MoreObjects.toStringHelper("")
                .add("db", dbName)
                .add("privileges", dbPrivileges)
                .toString();
    }
}
