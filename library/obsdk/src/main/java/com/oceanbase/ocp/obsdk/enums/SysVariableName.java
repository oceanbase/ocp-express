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

public enum SysVariableName {

    CHARACTER_SET_SERVER("character_set_server"),
    CHARACTER_SET_DATABASE("character_set_database"),
    OB_COMPATIBILITY_MODE("ob_compatibility_mode"),
    OB_TCP_INVITED_NODES("ob_tcp_invited_nodes"),
    READ_ONLY("read_only"),
    ;

    private String value;

    SysVariableName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static SysVariableName fromValue(String text) {
        for (SysVariableName b : SysVariableName.values()) {
            if (b.value.equals(text) || b.name().equals(text)) {
                return b;
            }
        }
        return null;
    }
}
