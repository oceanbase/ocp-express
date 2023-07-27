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
package com.oceanbase.ocp.security.util;

import java.util.regex.Pattern;

public class RegExChecker {

    /**
     * length: 8~32，at least contains 2 digits, 2 lower case letter, 2 upper case
     * letters, tow special characters.
     *
     * <ul>
     * <li>(?=(.*\\d){2,}) : at least two digits</li>
     * <li>(?=(.*[a-z]){2,}) : at least two lower case letters</li>
     * <li>(?=(.*[A-Z]){2,}) : at least two upper case letters</li>
     * <li>(?=(.*[~!@#%^&*_\\-+=|(){}\\[\\]:;,.?/]){2,}) : at least two special
     * characters</li>
     * </ul>
     */
    private static final String PASSWORD_PATTERN_EXPRESSION = "(" +
            "(?=(.*\\d){2,})" +
            "(?=(.*[a-z]){2,})" +
            "(?=(.*[A-Z]){2,})" +
            "(?=(.*[~!@#%^&*_\\-+=|(){}\\[\\]:;,.?/]){2,})" +
            "[0-9a-zA-Z~!@#%^&*_\\-+=|(){}\\[\\]:;,.?/]{8,32})";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_PATTERN_EXPRESSION);

    public static boolean checkPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

}
