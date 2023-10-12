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

package com.oceanbase.ocp.executor.internal.auth.http;

import com.oceanbase.ocp.executor.internal.constant.enums.HttpAuthType;
import com.oceanbase.ocp.executor.internal.util.ValidateUtils;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class HttpAuthentication {

    private HttpAuthType authType;

    private BasicAuthConfig basicAuthConfig;

    private DigestAuthConfig digestAuthConfig;

    private HttpAuthentication() {}

    public static HttpAuthentication basic(String username, String password) {
        ValidateUtils.requireNotBlank(username, "username");
        HttpAuthentication authentication = new HttpAuthentication();
        authentication.setAuthType(HttpAuthType.BASIC);
        authentication.setBasicAuthConfig(BasicAuthConfig.builder().username(username).password(password).build());
        return authentication;
    }

    public static HttpAuthentication ocpDigest(String username, String password) {
        ValidateUtils.requireNotBlank(username, "username");
        HttpAuthentication authentication = new HttpAuthentication();
        authentication.setAuthType(HttpAuthType.OCP_DIGEST);
        authentication.setDigestAuthConfig(DigestAuthConfig.builder().username(username).password(password).build());
        return authentication;
    }

}
