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

package com.oceanbase.ocp.obops.cluster.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.obops.tenant.model.TenantInfo;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObClusterStatus;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterInfo {

    private String clusterName;

    private Long obClusterId;

    @Default
    private ObClusterStatus status = ObClusterStatus.RUNNING;

    private String obVersion;

    private PerformanceStats performanceStats;

    private Boolean communityEdition;

    private List<Zone> zones;

    private List<TenantInfo> tenants;

    public static ClusterInfo fromBasicCluster(BasicCluster cluster) {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setObClusterId(cluster.getObClusterId());
        clusterInfo.setClusterName(cluster.getClusterName());
        clusterInfo.setStatus(cluster.getStatus());
        clusterInfo.setObVersion(cluster.getObVersion());
        clusterInfo.setObVersion(cluster.getObVersion());
        clusterInfo.setCommunityEdition(cluster.getCommunityEdition());
        List<ObServer> obServers = cluster.getObServers();
        Map<String, List<ObServer>> zoneToServer = obServers.stream().collect(Collectors.groupingBy(ObServer::getZone));
        List<RootService> rootServices = cluster.getRootServices();
        Map<String, RootService> zoneToRootService =
                rootServices.stream().collect(Collectors.toMap(RootService::getZone, r -> r));

        clusterInfo.setZones(cluster.getObZones().stream().map(obZone -> {
            Zone zone = Zone.fromObZone(obZone);
            if (zoneToRootService.get(zone.getName()) != null) {
                zone.setRootServer(RootServer.fromRootService(zoneToRootService.get(zone.getName())));
            }
            Map<String, String> serverArchs = cluster.getServerArchs();
            Map<String, String> serverDataPaths = cluster.getServerDataPaths();
            Map<String, String> serverLogPaths = cluster.getServerLogPaths();
            if (zoneToServer.containsKey(zone.getName())) {
                zone.setServers(zoneToServer.get(zone.getName()).stream().map(obServer -> {
                    Server server = Server.fromObServer(obServer);
                    String serverId = server.getIp() + ":" + server.getPort();
                    server.setArchitecture(serverArchs.get(serverId));
                    server.setDataPath(serverDataPaths.get(serverId));
                    server.setLogPath(serverLogPaths.get(serverId));
                    return server;
                }).collect(Collectors.toList()));
            }
            return zone;
        }).collect(Collectors.toList()));
        return clusterInfo;
    }
}
