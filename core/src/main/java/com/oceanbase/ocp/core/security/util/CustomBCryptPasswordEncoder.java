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
package com.oceanbase.ocp.core.security.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.oceanbase.ocp.core.util.WebRequestUtils;

public class CustomBCryptPasswordEncoder extends BCryptPasswordEncoder {

    private final String privateKey;

    public CustomBCryptPasswordEncoder(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String decryptedPassword;
        if (!WebRequestUtils.isBasicAuth() && StringUtils.isNotEmpty(privateKey)) {
            decryptedPassword = OcpRSAUtils.decryptByPrivate(privateKey, rawPassword.toString());
        } else {
            decryptedPassword = rawPassword.toString();
        }
        if (StringUtils.isEmpty(decryptedPassword)) {
            return false;
        }
        return super.matches(decryptedPassword, encodedPassword);
    }

}
