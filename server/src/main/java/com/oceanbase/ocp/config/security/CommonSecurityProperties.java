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
package com.oceanbase.ocp.config.security;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
public class CommonSecurityProperties {

    private static final String[] BUILD_IN_AUTH_WHITELIST =
            new String[] {"/api/v1/status.*", "/api/v1/loginKey", "/api/v1/time"};

    @Getter
    @Value("${ocp.iam.auth.basic.enabled:true}")
    private boolean basicAuthEnabled;

    @Getter
    @Value("${ocp.iam.csrf.enabled:false}")
    private boolean csrfEnabled;

    public String[] getAuthWhitelist() {
        return ArrayUtils.addAll(BUILD_IN_AUTH_WHITELIST);
    }
}
