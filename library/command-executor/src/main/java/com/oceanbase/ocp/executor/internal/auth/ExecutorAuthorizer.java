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

package com.oceanbase.ocp.executor.internal.auth;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.oceanbase.ocp.executor.exception.NoSuchAuthTypeException;
import com.oceanbase.ocp.executor.internal.auth.http.BasicAuthConfig;
import com.oceanbase.ocp.executor.internal.auth.http.DigestAuthConfig;
import com.oceanbase.ocp.executor.internal.auth.http.HttpAuthentication;
import com.oceanbase.ocp.executor.internal.constant.enums.HttpAuthType;

import jakarta.ws.rs.client.Client;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutorAuthorizer {

    public static void authorize(Client client, HttpAuthentication authentication) {
        HttpAuthType authType = authentication.getAuthType();
        switch (authType) {
            case BASIC:
                basicAuthorize(client, authentication.getBasicAuthConfig());
                break;
            case DIGEST:
                digestAuthorize(client, authentication.getDigestAuthConfig());
                break;
            default:
                throw new NoSuchAuthTypeException("[ExecutorAuthorizer]:no such auth type:" + authType);
        }
    }

    private static void basicAuthorize(Client client, BasicAuthConfig basicAuthConfig) {
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(basicAuthConfig.getUsername(), basicAuthConfig.getPassword());
        client.register(feature);
    }

    private static void digestAuthorize(Client client, DigestAuthConfig digestAuthConfig) {
        HttpAuthenticationFeature feature;
        if (digestAuthConfig == null) {
            feature = HttpAuthenticationFeature.digest();
        } else {
            feature = HttpAuthenticationFeature.digest(digestAuthConfig.getUsername(), digestAuthConfig.getPassword());
        }
        client.register(feature);
    }

}
