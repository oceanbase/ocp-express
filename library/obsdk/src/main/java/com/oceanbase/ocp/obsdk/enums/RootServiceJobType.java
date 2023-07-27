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

import lombok.Getter;

public enum RootServiceJobType {

    ALTER_TENANT_LOCALITY(RootServiceJobTypeGroup.TENANT, "ALTER_TENANT_LOCALITY"),
    ROLLBACK_ALTER_TENANT_LOCALITY(RootServiceJobTypeGroup.TENANT, "ROLLBACK_ALTER_TENANT_LOCALITY"),
    SHRINK_RESOURCE_POOL_UNIT_NUM(RootServiceJobTypeGroup.TENANT, "SHRINK_RESOURCE_POOL_UNIT_NUM"),
    UNKNOWN("UNKNOWN");

    @Getter
    private final RootServiceJobTypeGroup group;
    @Getter
    private final String value;

    RootServiceJobType(String value) {
        this(null, value);
    }

    RootServiceJobType(RootServiceJobTypeGroup group, String value) {
        this.group = group;
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static RootServiceJobType fromValue(String text) {
        for (RootServiceJobType b : RootServiceJobType.values()) {
            if (b.value.equals(text) || b.name().equals(text)) {
                return b;
            }
        }
        return UNKNOWN;
    }
}
