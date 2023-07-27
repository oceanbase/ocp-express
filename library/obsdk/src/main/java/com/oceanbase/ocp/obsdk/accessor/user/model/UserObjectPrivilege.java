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

package com.oceanbase.ocp.obsdk.accessor.user.model;

import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;
import com.oceanbase.ocp.obsdk.enums.ObjectType;

import lombok.Data;

@Data
public class UserObjectPrivilege {

    private String user;
    private String schemaName;
    private ObjectType objectType;
    private String objectName;
    private ObjectPrivilegeType objectPrivilege;

    public DbObject getDbObject() {
        DbObject dbObject = new DbObject();
        dbObject.setObjectType(objectType);
        dbObject.setObjectName(objectName);
        dbObject.setSchemaName(schemaName);
        return dbObject;
    }
}
