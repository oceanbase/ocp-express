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

import lombok.Getter;

public enum PartitionRole {

    LEADER("LEADER", 1),
    FOLLOWER("FOLLOWER", 2),
    UNKNOWN("UNKNOWN", -1);

    @Getter
    private final String value;

    @Getter
    private final Integer code;

    PartitionRole(String value, Integer code) {
        this.value = value;
        this.code = code;
    }

    public static PartitionRole fromValue(String value) {
        for (PartitionRole role : PartitionRole.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return UNKNOWN;
    }

    public static PartitionRole fromCode(Integer code) {
        for (PartitionRole role : PartitionRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return UNKNOWN;
    }
}
