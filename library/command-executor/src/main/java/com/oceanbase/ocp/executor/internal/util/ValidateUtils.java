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

import org.apache.commons.lang3.Validate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateUtils {

    public static void requireNotNull(Object arg, String argName) {
        Validate.notNull(arg, "argument '%s' can't be null!", argName);
    }

    public static void requireNotBlank(String arg, String argName) {
        Validate.notBlank(arg, "argument '%s' can't be blank!", argName);
    }

    public static void requirePositive(Integer arg, String argName) {
        Validate.isTrue(arg != null && arg > 0, "argument '%s' must be positive!", argName);
    }

}
