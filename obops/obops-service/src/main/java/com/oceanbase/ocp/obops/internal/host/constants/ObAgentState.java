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

package com.oceanbase.ocp.obops.internal.host.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ObAgentState {

    /**
     * Agent State.
     */
    AVAILABLE("AVAILABLE"),
    RESTARTING("RESTARTING"),
    OFFLINE("OFFLINE");

    private final String value;

    ObAgentState(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ObAgentState fromValue(String text) {
        for (ObAgentState b : ObAgentState.values()) {
            if (b.toString().equals(text)) {
                return b;
            }
        }
        return null;
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }
}
