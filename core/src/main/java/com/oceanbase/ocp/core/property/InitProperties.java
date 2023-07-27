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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ocp.init")
public class InitProperties {

    private ClusterProperties cluster;

    private String agentUsername;

    private String agentPassword;

    @Override
    public String toString() {
        return "InitProperties{" +
                "cluster=" + cluster +
                ", agentUsername=" + agentUsername +
                ", agentPassword=***}";
    }

    @Getter
    @Setter
    public static class ClusterProperties {

        private String name;

        private Long obClusterId;

        private String rootSysPassword;

        private List<ServerAddressInfo> serverAddresses;

        @Override
        public String toString() {
            return "ClusterProperties{" +
                    "name='" + name + '\'' +
                    ", obClusterId=" + obClusterId +
                    ", rootSysPassword='******'" +
                    ", rootServerAddresses=" + serverAddresses +
                    '}';
        }

        @Getter
        @Setter
        public static class ServerAddressInfo {

            private String address;

            private Integer svrPort;

            private Integer sqlPort;

            private boolean withRootServer;

            private int agentMgrPort;

            private int agentMonPort;

            @Override
            public String toString() {
                return "ServerAddressInfo{" +
                        "address='" + address + '\'' +
                        ", svrPort=" + svrPort +
                        ", sqlPort=" + sqlPort +
                        ", withRootServer=" + withRootServer +
                        ", agentMgrPort=" + agentMgrPort +
                        ", agentMonPort=" + agentMonPort +
                        '}';
            }

        }
    }

}
