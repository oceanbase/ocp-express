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

package com.oceanbase.ocp.obsdk.operator.cluster.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ObServerInnerStatus {

    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    DELETING("DELETING"),
    TAKEOVER_BY_RS("TAKEOVER_BY_RS"),
    ;

    private String value;

    ObServerInnerStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ObServerInnerStatus fromValue(String text) {
        String upperCaseText = StringUtils.upperCase(text);
        for (ObServerInnerStatus b : ObServerInnerStatus.values()) {
            if (String.valueOf(b.value).equals(upperCaseText)) {
                return b;
            }
        }
        return null;
    }
}
