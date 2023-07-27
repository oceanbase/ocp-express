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

package com.oceanbase.ocp.obsdk.operator.parameter.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SetObParameter {

    String name;

    Object value;

    String zone;

    String server;

    String tenant;

    Boolean allTenant;

    public static SetObParameter plain(String name, Object value) {
        return SetObParameter.builder()
                .name(name)
                .value(value)
                .build();
    }

    public static SetObParameter clusterScope(String name, Object value) {
        return plain(name, value);
    }

    public static SetObParameter zoneScope(String zone, String name, Object value) {
        return SetObParameter.builder()
                .name(name)
                .value(value)
                .zone(zone)
                .build();
    }

    public static SetObParameter serverScope(String server, String name, Object value) {
        return SetObParameter.builder()
                .name(name)
                .value(value)
                .server(server)
                .build();
    }

    public static SetObParameter tenantScope(String tenantName, String name, Object value) {
        return SetObParameter.builder()
                .name(name)
                .value(value)
                .tenant(tenantName)
                .build();
    }

    public static SetObParameter allTenantScope(String name, Object value) {
        return SetObParameter.builder()
                .name(name)
                .value(value)
                .allTenant(true)
                .build();
    }
}
