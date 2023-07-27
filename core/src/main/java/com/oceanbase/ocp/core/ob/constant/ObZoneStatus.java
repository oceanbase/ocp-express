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

public enum ObZoneStatus {

    /**
     * OB Zone final state.
     */
    RUNNING("RUNNING", false),
    UNAVAILABLE("UNAVAILABLE", false),
    SERVICE_STOPPED("SERVICE_STOPPED", false),
    STOPPED("STOPPED", false),
    DELETED("DELETED", false),
    /**
     * OB Zone operating intermediate state.
     */
    CREATING("CREATING", true),
    STOPPING("STOPPING", true),
    STARTING("STARTING", true),
    RESTARTING("RESTARTING", true),
    DELETING("DELETING", true),
    OPERATING("OPERATING", true);

    private final String value;

    private final boolean inOperation;

    ObZoneStatus(String value, boolean inOperation) {
        this.value = value;
        this.inOperation = inOperation;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ObZoneStatus fromValue(String text) {
        for (ObZoneStatus b : ObZoneStatus.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public boolean isInOperation() {
        return this.inOperation;
    }
}
