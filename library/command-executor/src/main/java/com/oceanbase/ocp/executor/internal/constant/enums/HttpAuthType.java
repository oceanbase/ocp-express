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

package com.oceanbase.ocp.executor.internal.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

public enum HttpAuthType {

    /**
     * Authentication type.
     */
    BASIC("basic"),

    DIGEST("digest"),

    /**
     * Ocp self-defined encryption.
     */
    OCP_DIGEST("ocp_digest"),

    SSL("ssl");

    @Getter
    private final String value;

    HttpAuthType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static HttpAuthType fromValue(String value) {
        for (HttpAuthType type : HttpAuthType.values()) {
            if (String.valueOf(type.value).equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
