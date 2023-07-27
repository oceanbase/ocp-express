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

package com.oceanbase.ocp.perf.sql.util;

import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlDiagnoseUtils {

    private static final Pattern SINGLE_INDEX_HINT_PATTERN =
            Pattern.compile("/\\*\\+\\s?(index)\\s*(\\(\\s*\\w*\\s+(.*?)\\))\\s?\\*/", Pattern.CASE_INSENSITIVE);

    public static boolean isSingleIndexHint(String hint) {
        return SINGLE_INDEX_HINT_PATTERN.matcher(hint).find();
    }
}
