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

package com.oceanbase.ocp.obops.cluster.param;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cluster init params, expected to be passed from OBD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterInitParam {

    private String clusterName;

    private Long obClusterId;

    private String rootSysPassword;

    private List<ObServerAddr> serverList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObServerAddr {

        private String address;

        private Integer sqlPort;

        private boolean withRootServer;

        @Override
        public String toString() {
            return "ObServerAddr{" +
                    "address='" + address + '\'' +
                    ", sqlPort=" + sqlPort +
                    ", withRootServer=" + withRootServer +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ClusterInitParam{" +
                "clusterName='" + clusterName + '\'' +
                ", obClusterId=" + obClusterId +
                ", rootSysPassword='***" + '\'' +
                ", serverList=" + serverList +
                '}';
    }

}
