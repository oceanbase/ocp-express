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

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Getter
@With
@Builder
@ToString(exclude = {"password"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ConnectProperties {

    @Builder.Default
    private ConnectionMode connectionMode = ConnectionMode.DIRECT;

    private String address;

    private Integer port;

    private List<ObServerAddr> obsAddrList;

    private String username;

    private String password;

    @Builder.Default
    private String tenantName = "sys";

    private String clusterName;

    private Long obClusterId;

    private JdbcSocksProxy proxy;

    @Builder.Default
    private CompatibilityMode compatibilityMode = CompatibilityMode.MYSQL;

    @Builder.Default
    private String database = "oceanbase";

    public String getDatabase() {
        return this.compatibilityMode == CompatibilityMode.ORACLE ? this.username : this.database;
    }

    public String getFullUsername() {
        if (ConnectionMode.PLAIN.equals(connectionMode)) {
            return username;
        }
        String fullName = this.username + "@" + this.tenantName;
        if (ConnectionMode.PROXY == connectionMode) {
            fullName += ("#" + this.clusterName);
            if (Objects.nonNull(this.obClusterId)) {
                fullName += (":" + this.obClusterId);
            }
        }
        return fullName;
    }

    public void validate() {
        Validate.isTrue(connectionMode != null, "The input ConnectProperties.connectionMode cannot be null.");
        Validate.isTrue(compatibilityMode != null, "The input ConnectProperties.compatibilityMode cannot be null.");

        if (ConnectionMode.PROXY.equals(connectionMode) || ConnectionMode.PLAIN.equals(connectionMode)) {
            Validate.isTrue(StringUtils.isNotBlank(address), "The input ConnectProperties.address:%s is not valid.",
                    address);
            Validate.isTrue(port != null, "The input ConnectProperties.port cannot be null.");
        }
        if (ConnectionMode.DIRECT.equals(connectionMode)) {
            boolean addressInvalid = StringUtils.isBlank(address) || port == null;
            if (addressInvalid) {
                Validate.isTrue(CollectionUtils.isNotEmpty(obsAddrList),
                        "The input ConnectProperties.obsAddrList can not be empty when connection mode is 'direct'.");
            }
        }
        Validate.isTrue(StringUtils.isNotBlank(username), "The input ConnectProperties.username:%s is not valid.",
                username);
        if (ConnectionMode.PROXY == connectionMode) {
            Validate.isTrue(StringUtils.isNotEmpty(clusterName),
                    "The input ConnectProperties.clusterName can not be empty when connection mode is proxy.");
        }
    }
}
