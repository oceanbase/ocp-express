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

public enum IndexStatus {

    VALID,

    /**
     * Unique checking
     */
    CHECKING,

    /**
     * Unique inelegible
     */
    INELEGIBLE,

    /**
     * index_error and restore_index_error
     */
    ERROR,

    /**
     * UNUSABLE and unavaiable
     */
    UNUSABLE,

    UNKNOWN;

    public static IndexStatus of(String status) {
        IndexStatus[] values = IndexStatus.values();
        for (IndexStatus s : values) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        return UNKNOWN;
    }

}
