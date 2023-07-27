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
package com.oceanbase.ocp.core.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RefreshScope
@Slf4j
@Component
@Getter
@Setter
public class PropertyManager {

    @Value("${ocp.log.download.http.connect.timeout:5000}")
    private Integer ocpLogDownloadHttpConnectTimeout;

    @Value("${ocp.log.download.http.read.timeout:30000}")
    private Integer ocpLogDownloadHttpReadTimeout;

    @Value("${ocp.iam.login.lockout-minutes:30}")
    private int userLoginLockoutMinutes;

    @Value("${ocp.iam.login.max-attempts:5}")
    private int userLoginMaxAttempts;

    @Value("${ocp.metric.collect.interval.second:15}")
    private int metricCollectSecondInterval;

    @Value("${ocp.sql.validate.pattern:}")
    private String sqlParamPattern;

}
