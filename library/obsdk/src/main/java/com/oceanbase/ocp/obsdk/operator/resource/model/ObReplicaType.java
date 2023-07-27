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

package com.oceanbase.ocp.obsdk.operator.resource.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ObReplicaType {

    FULL("FULL", "F"), LOGONLY("LOGONLY", "L"), READONLY("READONLY", "R");

    private String value;

    private String shorthand;

    ObReplicaType(String value, String shorthand) {
        this.value = value;
        this.shorthand = shorthand;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public String getShorthand() {
        return shorthand;
    }

    @JsonCreator
    public static ObReplicaType fromValue(String text) {
        for (ObReplicaType ele : ObReplicaType.values()) {
            if (ele.value.equals(text) || ele.shorthand.equals(text)) {
                return ele;
            }
        }
        return null;
    }
}
