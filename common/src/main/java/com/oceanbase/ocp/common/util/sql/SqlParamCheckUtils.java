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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class SqlParamCheckUtils {

    private static final Pattern SQL_PARAM_PATTERN = Pattern.compile("^[A-Za-z0-9\\u4e00-\\u9fa5\\-_,\\.]*$");
    private static final Pattern UNI_CODE_PATTERN = Pattern.compile("\\\\u[0-9a-f]{4}");

    public static boolean check(String param, String customRule) {
        if (param == null) {
            return true;
        }
        Matcher allowed;
        if (StringUtils.isNotEmpty(customRule)) {
            allowed = Pattern.compile(customRule).matcher(param);
        } else {
            allowed = SQL_PARAM_PATTERN.matcher(param);
        }
        if (allowed.find()) {
            return unicodeCheck(param);
        }
        return false;
    }

    private static boolean unicodeCheck(String param) {
        int start = 0;
        Matcher uniCodeMatcher = UNI_CODE_PATTERN.matcher(param);
        int valueInteger;
        do {
            String keyword;
            do {
                do {
                    if (!uniCodeMatcher.find(start)) {
                        return true;
                    }
                    start = uniCodeMatcher.end();
                    keyword = uniCodeMatcher.group(0);
                } while (!StringUtils.isNotBlank(keyword));
            } while (!keyword.startsWith("\\u"));
            String hexString = StringUtils.substring(keyword, 2);
            valueInteger = Integer.parseInt(hexString, 16);
        } while (valueInteger >= 19968 && valueInteger <= 40869);
        return false;
    }

}
