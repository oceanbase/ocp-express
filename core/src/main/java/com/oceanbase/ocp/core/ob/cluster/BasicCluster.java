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

package com.oceanbase.ocp.core.ob.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oceanbase.ocp.obsdk.operator.cluster.model.ObClusterStatus;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicCluster {

    private String clusterName;

    private Long obClusterId;

    @Default
    private ObClusterStatus status = ObClusterStatus.RUNNING;

    private String obVersion;

    private Boolean communityEdition;

    private List<ObZone> obZones;

    private List<ObServer> obServers;

    private List<RootService> rootServices;

    /**
     * serverIp:svrPort to architecture.
     */
    @Default
    private Map<String, String> serverArchs = new HashMap<>();

    /**
     * serverIp:svrPort to data path.
     */
    @Default
    private Map<String, String> serverDataPaths = new HashMap<>();

    /**
     * serverIp:svrPort to log path
     */
    @Default
    private Map<String, String> serverLogPaths = new HashMap<>();

    public String getArch(String ip, Integer svrPort) {
        return serverArchs.get(ip + ":" + svrPort);
    }

    public String getDataPath(String ip, Integer svrPort) {
        return serverDataPaths.get(ip + ":" + svrPort);
    }

    public String getLogPath(String ip, Integer svrPort) {
        return serverLogPaths.get(ip + ":" + svrPort);
    }
}
