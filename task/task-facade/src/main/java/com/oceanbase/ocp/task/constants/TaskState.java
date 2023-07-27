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
package com.oceanbase.ocp.task.constants;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum TaskState {

    /**
     * Task is running.
     */
    RUNNING("RUNNING"),

    /**
     * Task run failed or rollback failed.
     */
    FAILED("FAILED"),
    /**
     * Task finished with success.
     */
    SUCCESSFUL("SUCCESSFUL");

    @Getter
    private final String value;

    TaskState(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TaskState fromValue(String text) {
        return Arrays.stream(TaskState.values())
                .filter(b -> b.value.equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }

}
