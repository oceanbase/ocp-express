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

package com.oceanbase.ocp.obops.host.model;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LogLevel {
    /**
     * trace
     */
    TRACE("TRACE"),

    /**
     * debug
     */
    DEBUG("DEBUG"),

    /**
     * info
     */
    INFO("INFO"),

    /**
     * warn
     */
    WARN("WARN"),

    /**
     * error
     */
    ERROR("ERROR"),

    /**
     * fatal
     */
    FATAL("FATAL"),

    /**
     * ediag
     */
    EDIAG("EDIAG"),

    /**
     * wdiag
     */
    WDIAG("WDIAG");

    private String value;

    LogLevel(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static LogLevel fromValue(String text) {
        text = text.toUpperCase(Locale.getDefault());
        for (LogLevel level : LogLevel.values()) {
            if (level.value.equals(text)) {
                return level;
            }
        }
        return null;
    }
}
