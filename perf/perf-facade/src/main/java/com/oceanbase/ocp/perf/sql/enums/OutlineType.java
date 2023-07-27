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

package com.oceanbase.ocp.perf.sql.enums;

import com.oceanbase.ocp.obsdk.accessor.sql.tuning.model.ObOutline;
import com.oceanbase.ocp.perf.sql.util.SqlDiagnoseUtils;

/**
 * Type of outline, including plan-binding, index-binding, and rate-limit
 */
public enum OutlineType {

    /**
     * Index binding
     */
    INDEX,

    /**
     * Plan binding
     */
    PLAN,

    /**
     * Concurrent rate limit
     */
    CONCURRENT_LIMIT;

    public static OutlineType from(ObOutline otn) {
        if (otn.getConcurrentNum() != null || otn.getOutlineContent() == null) {
            return CONCURRENT_LIMIT;
        }
        return SqlDiagnoseUtils.isSingleIndexHint(otn.getOutlineContent()) ? INDEX : PLAN;
    }
}
