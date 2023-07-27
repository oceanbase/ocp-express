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
package com.oceanbase.ocp.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class LogContentUtils {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("password=[^\\s]*", Pattern.CASE_INSENSITIVE);
    private static final String PASSWORD_REPLACE_TO = "password=xxx";

    private static final Pattern BASIC_AUTH_PATTERN = Pattern.compile("--user [^\\s]*", Pattern.CASE_INSENSITIVE);
    private static final String BASIC_AUTH_REPLACE_TO = "--user xxx:xxx";

    private static final Pattern ACCESS_KEY_PATTERN =
            Pattern.compile("(access_id|access_key)=[\\w\\d]*");
    private static final String ACCESS_KEY_REPLACE_TO = "$1=xxx";

    private static final Pattern COMMAND_LINE_PATTERN = Pattern.compile("(mysql|obclient)(.*?) -p[^\\s]*");
    private static final String COMMAND_LINE_REPLACE_TO = "$1$2 -pxxx";

    private static final Pattern IDENTIFIED_BY_ARGS_PATTERN =
            Pattern.compile("identified by [^\\s]*", Pattern.CASE_INSENSITIVE);
    private static final String IDENTIFIED_BY_ARGS_REPLACE_TO = "identified by xxx";

    private static final Pattern PROXYRO_PASSWD_PATTERN = Pattern.compile("(observer_sys_password1?) = '[^\\s]*'");

    private static final Pattern PROXYSYS_PASSWD_PATTERN = Pattern.compile("(obproxy_sys_password) = '[^\\s]*'");

    private static final String PROXYRO_PASSWD_REPLACE_TO = "$1 = '***'";

    private static final String PROXYSYS_PASSWD_REPLACE_TO = "$1 = '***'";

    private static final Pattern IDENTIFIED_BY_PATTERN = Pattern.compile("identified by \\?", Pattern.CASE_INSENSITIVE);

    public static String maskProxysysPasswd(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher matcher = PROXYSYS_PASSWD_PATTERN.matcher(text);
        return matcher.replaceAll(PROXYSYS_PASSWD_REPLACE_TO);
    }

    public static String maskProxyroPasswd(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher matcher = PROXYRO_PASSWD_PATTERN.matcher(text);
        return matcher.replaceAll(PROXYRO_PASSWD_REPLACE_TO);
    }

    public static String maskPasswordValue(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = PASSWORD_PATTERN.matcher(text);
        return match.replaceAll(PASSWORD_REPLACE_TO);
    }

    public static String maskBasicAuth(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = BASIC_AUTH_PATTERN.matcher(text);
        return match.replaceAll(BASIC_AUTH_REPLACE_TO);
    }

    public static String maskAccessKey(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = ACCESS_KEY_PATTERN.matcher(text);
        return match.replaceAll(ACCESS_KEY_REPLACE_TO);
    }

    public static String maskCommandLine(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = COMMAND_LINE_PATTERN.matcher(text);
        return match.replaceAll(COMMAND_LINE_REPLACE_TO);
    }

    public static String maskSql(String sql) {
        String replacedSql = "";
        if (StringUtils.isNotEmpty(sql)) {
            Matcher match = IDENTIFIED_BY_ARGS_PATTERN.matcher(sql);
            replacedSql = match.replaceAll(IDENTIFIED_BY_ARGS_REPLACE_TO);
            match = ACCESS_KEY_PATTERN.matcher(replacedSql);
            replacedSql = match.replaceAll(ACCESS_KEY_REPLACE_TO);
        }
        return replacedSql;
    }

    private static List<Function<String, String>> maskFunctions = Arrays.asList(
            LogContentUtils::maskPasswordValue,
            LogContentUtils::maskBasicAuth,
            LogContentUtils::maskAccessKey,
            LogContentUtils::maskCommandLine,
            LogContentUtils::maskSql,
            LogContentUtils::maskProxyroPasswd,
            LogContentUtils::maskProxysysPasswd);

    public static String mask(String text) {
        String result = text;
        for (Function<String, String> maskFunction : maskFunctions) {
            result = maskFunction.apply(result);
        }
        return result;
    }

    public static Object[] maskSqlArgs(String sql, Object[] args) {
        Object[] replaceArgs = args.clone();
        Matcher match = IDENTIFIED_BY_PATTERN.matcher(sql);
        if (match.find()) {
            int pos = match.start();
            String subSql = sql.substring(0, pos);
            int paramIdx = 0;
            for (int i = 0; i < subSql.length(); i++) {
                if (subSql.charAt(i) == '?') {
                    paramIdx += 1;
                }
            }
            replaceArgs[paramIdx] = "xxx";
        }
        if (StringUtils.containsIgnoreCase(sql, "set backup_dest")
                || StringUtils.containsIgnoreCase(sql, "set backup_backup_dest")
                || StringUtils.containsIgnoreCase(sql, "set data_backup_dest")
                || StringUtils.containsIgnoreCase(sql, "set log_archive_dest")
                || StringUtils.containsIgnoreCase(sql, "alter system backup")
                || StringUtils.containsIgnoreCase(sql, "alter system restore")) {
            if (args.length >= 1 && args[0] instanceof String) {
                replaceArgs[0] = maskAccessKey((String) args[0]);
            }
        }
        return replaceArgs;
    }
}
