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

package com.oceanbase.ocp.executor.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
@Builder
public class Configuration {

    @Default
    private int httpThreadCorePoolSize = 10;

    @Default
    private int httpThreadMaximumPoolSize = 200;

    @Default
    private long httpThreadKeepAliveTime = 120000L;

    @Default
    private int httpSocketTimeout = 3000;

    @Default
    private int httpConnectTimeout = 5000;

    @Default
    private int httpReadTimeout = 5000;

    @Default
    private int httpConnectionMaxPoolSize = 100;

    @Default
    private int httpConnectionMaxPerRoute = 10;

    @Default
    private int connectorCacheMaxSize = 1000;

    @Default
    private int connectorCacheMaxIdleSeconds = 3600;

}
