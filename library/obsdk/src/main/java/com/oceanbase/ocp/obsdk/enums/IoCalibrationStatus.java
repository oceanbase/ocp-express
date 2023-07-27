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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IoCalibrationStatus {

    IN_PROCESS("IN PROGRESS"),

    READY("READY"),

    NOT_AVAILABLE("NOT AVAILABLE"),

    UNKNOWN("UNKNOWN");

    private final String value;

    IoCalibrationStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static IoCalibrationStatus fromValue(String text) {
        for (IoCalibrationStatus b : IoCalibrationStatus.values()) {
            if (b.value.equals(text)) {
                return b;
            }
        }
        return UNKNOWN;
    }

    public boolean isSuccess() {
        return this.equals(READY);
    }

    public boolean isFailed() {
        return this.equals(NOT_AVAILABLE);
    }

    public boolean isInProcess() {
        return this.equals(IN_PROCESS);
    }
}
