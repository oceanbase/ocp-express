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

package com.oceanbase.ocp.core.ob.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.obsdk.operator.compaction.model.ObTenantCompaction;

public enum TenantCompactionResult {

    /**
     * Tenant compacting state.
     */
    SUCCESS("SUCCESS"),

    FAIL("FAIL"),

    COMPACTING("COMPACTING");

    private final String value;

    TenantCompactionResult(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TenantCompactionResult fromValue(String text) {
        for (TenantCompactionResult b : TenantCompactionResult.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static TenantCompactionResult fromCompaction(ObTenantCompaction compaction) {
        if (compaction.getError()) {
            return FAIL;
        }
        switch (compaction.getStatus()) {
            case IDLE:
                return SUCCESS;
            case VERIFYING:
            case COMPACTING:
                return COMPACTING;
            default:
                throw new IllegalArgumentException(ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "status");
        }
    }

}
