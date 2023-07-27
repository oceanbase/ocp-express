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

package com.oceanbase.ocp.obops.parameter.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Sets;

import com.oceanbase.ocp.core.ob.tenant.TenantMode;

public enum CompatibleType {

    ALL("ALL"),

    ORACLE("ORACLE"),

    MYSQL("MYSQL"),
    ;

    private static final Set<CompatibleType> ALL_SET = new HashSet<>(Arrays.asList(ALL, ORACLE, MYSQL));

    private String value;

    CompatibleType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static CompatibleType fromValue(String text) {
        for (CompatibleType b : CompatibleType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public boolean compatibleWith(TenantMode tenantMode) {
        return !incompatibleWith(tenantMode);
    }

    public boolean incompatibleWith(TenantMode tenantMode) {
        return incompatibles(tenantMode).contains(this);
    }

    public static Set<CompatibleType> compatibles(TenantMode tenantMode) {
        return Sets.difference(ALL_SET, incompatibles(tenantMode));
    }

    public static Set<CompatibleType> incompatibles(TenantMode tenantMode) {
        if (tenantMode == TenantMode.MYSQL) {
            return Collections.singleton(ORACLE);
        } else if (tenantMode == TenantMode.ORACLE) {
            return Collections.singleton(MYSQL);
        } else {
            return Collections.emptySet();
        }
    }
}
