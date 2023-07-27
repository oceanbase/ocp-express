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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.oceanbase.ocp.common.sql.Hint;

public class SqlHintUtils {

    private static final Pattern SELECT_WITH_HINT =
            Pattern.compile("^\\s*SELECT /[*][+]\\s*(.+?)\\s*[*]/", Pattern.CASE_INSENSITIVE);
    private static final Pattern SELECT_WITHOUT_HINT = Pattern.compile("^\\s*SELECT", Pattern.CASE_INSENSITIVE);

    /**
     * Add hints to sqls.
     * <p>
     * attentions: Only support SELECT statement.
     *
     * @param sql raw SQL
     * @param hints list of hit
     */
    public static String addHints(String sql, List<Hint> hints) {
        String hintText = hints.stream().map(Hint::text).collect(Collectors.joining(" "));

        if (SELECT_WITH_HINT.matcher(sql).find()) {
            String replacement = String.format("SELECT /*+ $1 %s */", hintText);
            return SELECT_WITH_HINT.matcher(sql).replaceFirst(replacement);
        }
        if (SELECT_WITHOUT_HINT.matcher(sql).find()) {
            String replacement = String.format("SELECT /*+ %s */", hintText);
            return SELECT_WITHOUT_HINT.matcher(sql).replaceFirst(replacement);
        }
        return sql;
    }

}
