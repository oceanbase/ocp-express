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

package com.oceanbase.ocp.executor.internal.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.common.util.file.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogMaskUtils {

    private static final Set<String> SENSITIVE_WORDS = loadSensitiveWords();

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("password=[^\\s]*", Pattern.CASE_INSENSITIVE);

    private static final String PASSWORD_REPLACE_TO = "password=xxx";

    private static final Pattern BASIC_AUTH_PATTERN = Pattern.compile("--user [^\\s]*", Pattern.CASE_INSENSITIVE);
    private static final String BASIC_AUTH_REPLACE_TO = "--user xxx:xxx";

    private static final Pattern ACCESS_KEY_PATTERN = Pattern.compile("(access_id|access_key)=[\\w\\d]*");
    private static final String ACCESS_KEY_REPLACE_TO = "$1=xxx";

    private static final Pattern COMMAND_LINE_PATTERN = Pattern.compile("(mysql|obclient)(.*?) -p[^\\s]*");
    private static final String COMMAND_LINE_REPLACE_TO = "$1$2 -pxxx";

    private static final Pattern IDENTIFIED_BY_ARGS_PATTERN =
            Pattern.compile("identified by [^\\s]*", Pattern.CASE_INSENSITIVE);
    private static final String IDENTIFIED_BY_ARGS_REPLACE_TO = "identified by xxx";

    private static final List<Function<String, String>> MASK_FUNCTIONS = Arrays.asList(
            LogMaskUtils::maskPasswordValue,
            LogMaskUtils::maskBasicAuth,
            LogMaskUtils::maskAccessKey,
            LogMaskUtils::maskCommandLine,
            LogMaskUtils::maskSql);

    public static String mask(String text) {
        String result = text;
        for (Function<String, String> maskFunction : MASK_FUNCTIONS) {
            result = maskFunction.apply(result);
        }
        return result;
    }

    public static boolean containsSensitiveWords(String plainText) {
        return SENSITIVE_WORDS.stream().anyMatch(word -> StringUtils.containsIgnoreCase(plainText, word));
    }

    private static String maskPasswordValue(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = PASSWORD_PATTERN.matcher(text);
        return match.replaceAll(PASSWORD_REPLACE_TO);
    }

    private static String maskBasicAuth(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = BASIC_AUTH_PATTERN.matcher(text);
        return match.replaceAll(BASIC_AUTH_REPLACE_TO);
    }

    private static String maskAccessKey(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = ACCESS_KEY_PATTERN.matcher(text);
        return match.replaceAll(ACCESS_KEY_REPLACE_TO);
    }

    private static String maskCommandLine(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        Matcher match = COMMAND_LINE_PATTERN.matcher(text);
        return match.replaceAll(COMMAND_LINE_REPLACE_TO);
    }

    private static String maskSql(String sql) {
        String replacedSql = "";
        if (StringUtils.isNotEmpty(sql)) {
            Matcher match = IDENTIFIED_BY_ARGS_PATTERN.matcher(sql);
            replacedSql = match.replaceAll(IDENTIFIED_BY_ARGS_REPLACE_TO);
        }
        return replacedSql;
    }

    private static Set<String> loadSensitiveWords() {
        try (InputStream inputStream = FileUtils.readAsInputStream("security/sensitive_words.txt")) {
            List<String> words = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            return words.stream().map(StringUtils::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        } catch (Exception ex) {
            log.error("failed to load sensitive words config file, errMsg:{},cause:{}", ex.getMessage(), ex.getCause());
        }
        return new HashSet<>();
    }

}
