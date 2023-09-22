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

import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.util.WebRequestUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Enables CSRF protection with whitelists of headers, http methods, and url
 * patterns.
 */
@RefreshScope
@Slf4j
@Data
@Configuration
@ConditionalOnProperty(name = "ocp.iam.csrf.enabled", havingValue = "true")
public class CsrfRequestMatcher implements RequestMatcher {

    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

    @Value("${ocp.iam.csrf.url.excluded:/}")
    private String[] excludedUrls;

    @Override
    public boolean matches(HttpServletRequest request) {

        // Skip CSRF protection - allowed methods
        if (allowedMethods.matcher(request.getMethod()).matches()) {
            return false;
        }

        if (WebRequestUtils.isBasicAuth(request)) {
            return false;
        }

        // Skip CSRF protection - excluded urls
        if (urlMatches(request, excludedUrls)) {
            return false;
        }

        // CSRF for everything else
        log.info("CSRF token required for API [{} {}, client {}, traceId {}]", request.getMethod(),
                request.getRequestURI(), request.getRemoteAddr(), getTraceId());
        return true;
    }

    /**
     * check if request contains any of the excluded headers.
     */
    private boolean headerMatches(HttpServletRequest request, String[] headers) {
        if (headers.length == 0) {
            return false;
        }
        return Arrays.stream(headers).anyMatch(key -> StringUtils.isNotEmpty(request.getHeader(key)));
    }

    /**
     * check if the request matches any of the excluded urls.
     **/
    private boolean urlMatches(HttpServletRequest request, String[] urls) {
        if (urls.length == 0) {
            return false;
        }
        return Arrays.stream(urls).anyMatch(url -> (new RegexRequestMatcher(url, null, true)).matches(request));
    }

    /**
     * Get trace id from tracer.
     */
    private String getTraceId() {
        return TraceUtils.getTraceId();
    }

}
