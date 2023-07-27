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

package com.oceanbase.ocp.obsdk.operator.sql.model;

public enum PlanType {

    LOCAL(1), REMOTE(2), DIST(3), UNKNOWN(0);

    final int key;

    PlanType(int key) {
        this.key = key;
    }

    public static PlanType of(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (PlanType e : values()) {
            if (e == UNKNOWN) {
                continue;
            }
            if (e.key == value) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
