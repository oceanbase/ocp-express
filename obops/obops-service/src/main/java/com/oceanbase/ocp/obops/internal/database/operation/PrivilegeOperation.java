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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.oceanbase.ocp.obops.database.model.Grantee;
import com.oceanbase.ocp.obsdk.accessor.UserAccessor;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString
public abstract class PrivilegeOperation {

    public enum PrivilegeOperationType {
        GRANT,
        REVOKE,
        ;
    }

    protected Long tenantId;

    protected Grantee grantee;

    protected PrivilegeOperationType operationType;

    public abstract boolean hasPrivileges();

    public abstract void operate(UserAccessor userAccessor);

    @JsonProperty
    public String getDescription() {
        String opWord = operationType.name().toLowerCase();
        String opPrep = operationType == PrivilegeOperationType.GRANT ? "to" : "from";
        String privWord;
        if (this instanceof GlobalPrivilegeOperation) {
            privWord = "global privilege";
        } else if (this instanceof RolePrivilegeOperation) {
            privWord = "role privilege";
        } else if (this instanceof DbPrivilegeOperation) {
            privWord = "database privilege";
        } else if (this instanceof ObjectPrivilegeOperation) {
            privWord = "object privilege";
        } else {
            privWord = "privilege";
        }
        String granteeWord = grantee.getType().name().toLowerCase() + " " + grantee.getName();
        return String.format("%s %s %s %s %s", opWord, privWord, getPrivilegeString(), opPrep, granteeWord);
    }

    @JsonIgnore
    public abstract String getPrivilegeString();
}
