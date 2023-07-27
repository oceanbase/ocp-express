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

package com.oceanbase.ocp.obsdk.connector;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class ObConnectorKey {

    private ConnectionMode connectionMode;

    private String clusterName;

    private Long obClusterId;

    private String tenantName;

    private String username;

    @ToString.Exclude
    private String password;

    private String address;

    private Integer port;

    private String database;

    public static ObConnectorKey of(ConnectProperties connectProperties) {
        return ObConnectorKey.builder()
                .connectionMode(connectProperties.getConnectionMode())
                .clusterName(connectProperties.getClusterName())
                .obClusterId(connectProperties.getObClusterId())
                .tenantName(connectProperties.getTenantName())
                .username(connectProperties.getUsername())
                .password(connectProperties.getPassword())
                .address(connectProperties.getAddress())
                .port(connectProperties.getPort())
                .database(connectProperties.getDatabase())
                .build();
    }
}
