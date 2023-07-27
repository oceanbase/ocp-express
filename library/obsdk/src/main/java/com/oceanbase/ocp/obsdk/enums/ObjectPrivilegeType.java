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

package com.oceanbase.ocp.obsdk.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

public enum ObjectPrivilegeType {

    SELECT("SELECT", SupportedObjectTypes.TABLE_AND_VIEW),
    UPDATE("UPDATE", SupportedObjectTypes.TABLE_AND_VIEW),
    INSERT("INSERT", SupportedObjectTypes.TABLE_AND_VIEW),
    DELETE("DELETE", SupportedObjectTypes.TABLE_AND_VIEW),
    ALTER("ALTER", SupportedObjectTypes.TABLE),
    INDEX("INDEX", SupportedObjectTypes.TABLE),
    REFERENCES("REFERENCES", SupportedObjectTypes.TABLE),
    EXECUTE("EXECUTE", SupportedObjectTypes.STORED_PROCEDURE),


    ;

    private static class SupportedObjectTypes {

        private static final List<ObjectType> TABLE_AND_VIEW = Arrays.asList(ObjectType.TABLE, ObjectType.VIEW);
        private static final List<ObjectType> TABLE = Collections.singletonList(ObjectType.TABLE);
        private static final List<ObjectType> STORED_PROCEDURE = Collections.singletonList(ObjectType.STORED_PROCEDURE);
        private static final List<ObjectType> NONE = Collections.emptyList();
    }

    @Getter
    private final String value;

    @Getter
    private final List<ObjectType> supportedObjectTypes;

    ObjectPrivilegeType(String value, List<ObjectType> supportedObjectTypes) {
        this.value = value;
        this.supportedObjectTypes = supportedObjectTypes;
    }

    public boolean roleSupported() {
        return this != INDEX && this != REFERENCES;
    }

    public static ObjectPrivilegeType fromValue(String value) {
        for (ObjectPrivilegeType b : ObjectPrivilegeType.values()) {
            if (StringUtils.equals(b.value, value) || StringUtils.equals(b.name(), value)) {
                return b;
            }
        }
        return null;
    }
}
