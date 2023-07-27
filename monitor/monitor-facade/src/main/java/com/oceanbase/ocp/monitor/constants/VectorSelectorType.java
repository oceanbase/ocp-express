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
package com.oceanbase.ocp.monitor.constants;

import lombok.Getter;

@Getter
public enum VectorSelectorType {

    /**
     * Math symbol.
     */
    MATCH_EQ("="),
    MATCH_NEQ("!="),
    MATCH_REGEX("=~"),
    MATCH_NOT_REGEX("!~");

    private final String value;

    VectorSelectorType(String matchSymbol) {
        this.value = matchSymbol;
    }

    public static VectorSelectorType fromValue(String value) {
        for (VectorSelectorType labelMatchEnums : values()) {
            if (String.valueOf(labelMatchEnums.value).equals(value)) {
                return labelMatchEnums;
            }
        }
        return MATCH_EQ;
    }
}
