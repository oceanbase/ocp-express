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

package com.oceanbase.ocp.obops.tenant.model;

import lombok.Getter;

public enum ObTenantStatus {

    NORMAL("TENANT_STATUS_NORMAL", "NORMAL"),

    CREATING("CREATING", "CREATING"),

    RESTORE("TENANT_STATUS_RESTORE", "RESTORE"),
    ;

    @Getter
    final String value;

    @Getter
    final String newValue;

    ObTenantStatus(String value, String newValue) {
        this.value = value;
        this.newValue = newValue;
    }

    public static ObTenantStatus fromValue(String value) {
        for (ObTenantStatus status : ObTenantStatus.values()) {
            if (String.valueOf(status.value).equals(value) || String.valueOf(status.newValue).equals(value)) {
                return status;
            }
        }
        return null;
    }
}
