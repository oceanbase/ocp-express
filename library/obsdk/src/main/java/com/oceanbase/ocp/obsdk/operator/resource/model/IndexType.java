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

public enum IndexType {

    PRIMARY,

    NORMAL_LOCAL,

    NORMAL_GLOBAL,

    UNIQUE_LOCAL,

    UNIQUE_GLOBAL,

    OTHER;

    public static IndexType from(boolean isPrimary, boolean isUnique, boolean global) {
        if (isPrimary) {
            return PRIMARY;
        }
        if (global) {
            if (isUnique) {
                return UNIQUE_GLOBAL;
            }
            return NORMAL_GLOBAL;
        } else if (isUnique) {
            return UNIQUE_LOCAL;
        }
        return NORMAL_LOCAL;
    }
}
