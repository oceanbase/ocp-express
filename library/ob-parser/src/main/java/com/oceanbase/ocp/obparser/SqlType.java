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

package com.oceanbase.ocp.obparser;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public enum SqlType {

    /**
     * SQL statement type.
     */
    SELECT,

    SELECT_FOR_UPDATE,

    INSERT,

    UPDATE,

    DELETE,

    REPLACE,

    MERGE,

    EXPLAIN,

    COMMIT,

    ROLLBACK,

    USE,

    SET,

    OTHER,

    SHOW,

    PL,
    ;

    public static SqlType fromValue(String value) {
        for (SqlType b : SqlType.values()) {
            if (StringUtils.equalsIgnoreCase(b.name(), value)) {
                return b;
            }
        }
        return OTHER;
    }

    private static final Pattern FOR_UPDATE_PATTERN = Pattern.compile(
            "for[\\s\\p{Zs}]+update[\\s\\p{Zs}]*(wait[\\s\\p{Zs}]+.*|nowait)?[\\s\\p{Zs}]*(order[\\s\\p{Zs}]+(siblings[\\s\\p{Zs}]+)?by[\\s\\p{Zs}]+.*)?$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Parse SQL type by input sql statement.
     *
     * <p>
     * Can't recognize SELECT or SELECT_FOR_UPDATE.
     * </p>
     *
     * @param text Sql
     * @return {@link SqlType}
     */
    public static SqlType quickParse(String text) {
        if (text == null) {
            return OTHER;
        } else if (StringUtils.isBlank(text)) {
            return OTHER;
        }
        String summary = ltrimFirstNCharToLowerCase(text, 6);
        if (summary == null) {
            return OTHER;
        } else if ("select".equals(summary)) {
            if (FOR_UPDATE_PATTERN.matcher(text).find()) {
                return SELECT_FOR_UPDATE;
            }
            return SELECT;
        } else if ("insert".equals(summary)) {
            return INSERT;
        } else if ("delete".equals(summary)) {
            return DELETE;
        } else if ("update".equals(summary)) {
            return UPDATE;
        } else if ("replac".equals(summary)) {
            return REPLACE;
        } else if ("commit".equals(summary)) {
            return COMMIT;
        } else if ("rollba".equals(summary)) {
            return ROLLBACK;
        } else if ("explai".equals(summary)) {
            return EXPLAIN;
        } else if (summary.startsWith("use ")) {
            return USE;
        } else if (summary.startsWith("set ")) {
            return SET;
        } else if (summary.startsWith("show ")) {
            return SHOW;
        } else if (summary.startsWith("begin ")) {
            String str = text.substring(StringUtils.indexOfIgnoreCase(text, "begin") + 5);
            if (!StringUtils.startsWithIgnoreCase(str.trim(), "transaction")) {
                return PL;
            }
        }
        return OTHER;
    }

    private static String ltrimFirstNCharToLowerCase(String text, int n) {
        if (text == null) {
            return null;
        }
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                continue;
            }
            return text.substring(i, Math.min(text.length(), i + n)).toLowerCase();
        }
        return null;
    }

}
