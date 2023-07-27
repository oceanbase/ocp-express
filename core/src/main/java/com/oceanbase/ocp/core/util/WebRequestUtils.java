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
package com.oceanbase.ocp.core.util;

import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class WebRequestUtils {

    private static final Pattern IP_V4_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

    private static final String COMMA = ",";

    private static final String[] IP_HEADER_CANDIDATES = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};

    /**
     * Get request client address.
     */
    public static String getClientAddress(HttpServletRequest request) {
        if (IP_HEADER_CANDIDATES != null) {
            for (String header : IP_HEADER_CANDIDATES) {
                String remoteAddr = request.getHeader(header);
                String firstAddr = Optional.ofNullable(remoteAddr)
                        .filter(s -> s.length() > 0)
                        .filter(s -> s.contains(COMMA))
                        .map(s -> s.substring(0, s.indexOf(COMMA)))
                        .map(String::trim)
                        .orElse(remoteAddr);
                if (firstAddr != null && IP_V4_PATTERN.matcher(firstAddr).matches()) {
                    return firstAddr;
                }
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Determine whether request is basic authorization.
     */
    public static boolean isBasicAuth(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Basic ");
    }

    /**
     * Determine whether request is basic authorization.
     */
    public static boolean isBasicAuth() {
        HttpServletRequest request =
                ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String authHeader = request.getHeader("Authorization");
        return StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Basic ");
    }

}
