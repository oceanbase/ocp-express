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
package com.oceanbase.ocp.common.util.sql;

import org.apache.commons.lang3.StringUtils;

public abstract class SqlUtils {

    private SqlUtils() {}

    /**
     * Escape value for `like` statement.
     *
     * @param value value in query condition
     * @return escaped value
     */
    public static String escapeWildcards(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        value = StringUtils.replace(value, "%", "\\%");
        return StringUtils.replace(value, "_", "\\_");
    }

    public static String escapeStringValue(String value) {
        if (value == null) {
            return null;
        }
        return StringUtils.replace(value, "'", "''");
    }

}
