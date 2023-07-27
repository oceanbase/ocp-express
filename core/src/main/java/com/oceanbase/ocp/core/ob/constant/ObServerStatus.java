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

public enum ObServerStatus {

    /**
     * Final state of OB Server.
     */
    RUNNING("RUNNING", false),
    UNAVAILABLE("UNAVAILABLE", false),
    SERVICE_STOPPED("SERVICE_STOPPED", false),
    PROCESS_STOPPED("PROCESS_STOPPED", false),
    DELETED("DELETED", false),
    /**
     * Operating intermediate state.
     */
    CREATING("CREATING", true),
    SERVICE_STOPPING("SERVICE_STOPPING", true),
    PROCESS_STOPPING("PROCESS_STOPPING", true),
    STARTING("STARTING", true),
    RESTARTING("RESTARTING", true),
    DELETING("DELETING", true);

    private final String value;

    private final boolean inOperation;

    ObServerStatus(String value, boolean inOperation) {
        this.value = value;
        this.inOperation = inOperation;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ObServerStatus fromValue(String text) {
        for (ObServerStatus b : ObServerStatus.values()) {
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
