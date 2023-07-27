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

import org.apache.commons.lang3.StringUtils;

public enum ObjectType {

    TABLE("TABLE"),
    VIEW("VIEW"),
    STORED_PROCEDURE("PROCEDURE");

    private final String value;

    ObjectType(String value) {
        this.value = value;
    }

    public static ObjectType fromValue(String value) {
        for (ObjectType b : ObjectType.values()) {
            if (StringUtils.equalsIgnoreCase(b.value, value)
                    || StringUtils.equalsIgnoreCase(b.name(), value)) {
                return b;
            }
        }
        return null;
    }
}
