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

package com.oceanbase.ocp.obops.internal.cluster;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.constant.ObServerStatus;
import com.oceanbase.ocp.core.ob.constant.ObZoneInnerStatus;
import com.oceanbase.ocp.core.ob.constant.ObZoneStatus;
import com.oceanbase.ocp.core.ob.tenant.QueryTenantParam;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.cluster.ClusterService;
import com.oceanbase.ocp.obops.cluster.model.ClusterInfo;
import com.oceanbase.ocp.obops.cluster.model.PerformanceStats;
import com.oceanbase.ocp.obops.cluster.model.Server;
import com.oceanbase.ocp.obops.cluster.model.Zone;
import com.oceanbase.ocp.obops.resource.ClusterResourceStatService;
import com.oceanbase.ocp.obops.resource.model.ServerResourceStats;
import com.oceanbase.ocp.obops.tenant.TenantService;
import com.oceanbase.ocp.obops.tenant.model.TenantInfo;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerInnerStatus;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerKey;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnit;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ClusterResourceStatService clusterResourceStatService;

    @Autowired
    private ClusterMetricManager clusterMetricManager;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Override
    public ClusterInfo getClusterInfo() {
        BasicCluster basicCluster = managedCluster.getClusterInfo();
        ClusterInfo clusterInfo = ClusterInfo.fromBasicCluster(basicCluster);
        fixStatus(clusterInfo);
        Page<TenantInfo> tenantList = tenantService.listTenant(QueryTenantParam.builder().build(), Pageable.unpaged());
        clusterInfo.setTenants(tenantList.toList());
        attachServerResourceStats(clusterInfo);
        attachPerformance(clusterInfo);
        return clusterInfo;
    }

    @Override
    public Server getServerInfo(String ip, Integer svrPort) {
        ExceptionUtils.require(StringUtils.isNotEmpty(ip), ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "ip");
        ExceptionUtils.require(svrPort != null && svrPort > 0, ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "svrPort");
        BasicCluster basicCluster = managedCluster.getClusterInfo();
        Optional<ObServer> optional = basicCluster.getObServers().stream().filter(
                server -> ip.equals(server.getSvrIp()) && svrPort.equals(server.getSvrPort())).findFirst();
        ExceptionUtils.require(optional.isPresent(), ErrorCodes.COMMON_NOT_FOUND, Server.class,
                Arrays.asList(ip, svrPort));
        Server server = Server.fromObServer(optional.get());
        server.setArchitecture(basicCluster.getArch(ip, svrPort));
        server.setDataPath(basicCluster.getDataPath(ip, svrPort));
        server.setLogPath(basicCluster.getLogPath(ip, svrPort));
        return server;
    }

    private void attachServerResourceStats(ClusterInfo clusterInfo) {
        try {
            List<ServerResourceStats> serverResourceStatsList = clusterResourceStatService.serverStatsByCluster();
            Map<ObServerKey, ServerResourceStats> ip2stats = serverResourceStatsList.stream()
                    .collect(Collectors.toMap(stat -> new ObServerKey(stat.getIp(), stat.getPort()), stat -> stat));
            for (Zone zone : clusterInfo.getZones()) {
                for (Server server : zone.getServers()) {
                    server.setStats(ip2stats.get(new ObServerKey(server.getIp(), server.getPort())));
                }
            }
        } catch (Exception ex) {
            log.warn("Get server stats failed", ex);
        }
    }

    private void attachPerformance(ClusterInfo clusterInfo) {
        List<Zone> zones = clusterInfo.getZones();
        try {
            Map<String, PerformanceStats> server2PerfStatsMap = clusterMetricManager.serverPerformanceStats();
            fixUnitNum(server2PerfStatsMap);
            PerformanceStats clusterPerfStats = new PerformanceStats();
            for (Zone zone : zones) {
                PerformanceStats zonePerfStats = new PerformanceStats();
                for (Server server : zone.getServers()) {
                    PerformanceStats serverPerfStats = server2PerfStatsMap.get(server.getIp() + ":" + server.getPort());
                    if (Objects.nonNull(serverPerfStats)) {
                        server.setPerformanceStats(serverPerfStats);
                        zonePerfStats.addFrom(serverPerfStats);
                    }
                }
                zone.setPerformanceStats(zonePerfStats);
                clusterPerfStats.addFrom(zonePerfStats);
            }
            clusterInfo.setPerformanceStats(clusterPerfStats);
        } catch (Exception e) {
            log.warn("query performance failed, message={}", e.getMessage());
        }
    }

    private void fixUnitNum(Map<String, PerformanceStats> server2PerfStatsMap) {
        try {
            List<ObUnit> allUnits = obOperatorFactory.createResourceOperator().listAllUnits();
            Map<String, List<ObUnit>> svr2Units = allUnits.stream()
                    .collect(Collectors.groupingBy(obUnit -> obUnit.getSvrIp() + ":" + obUnit.getSvrPort()));
            Map<String, Integer> svr2UnitCount = svr2Units.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().size()));
            // data monitor API may miss some server, fill it first
            svr2UnitCount.forEach((svr, unitCount) -> {
                PerformanceStats performanceStats =
                        server2PerfStatsMap.computeIfAbsent(svr, t -> new PerformanceStats());
                performanceStats.setUnitNum(unitCount.doubleValue());
            });
            // it no value form __all_unit, then set unit_num as 0
            server2PerfStatsMap.forEach((key, value) -> {
                if (null == svr2UnitCount.get(key)) {
                    value.setUnitNum(0.0d);
                }
            });
        } catch (Exception ex) {
            log.warn("failed to fix unit_num, reason={}",
                    org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage(ex));
        }
    }

    private void fixStatus(ClusterInfo clusterInfo) {
        for (Zone zone : clusterInfo.getZones()) {
            fixZoneStatus(zone);
            for (Server server : zone.getServers()) {
                fixServerStatus(zone, server);
            }
        }
    }

    private void fixServerStatus(Zone zone, Server server) {
        ObServerInnerStatus innerStatus = server.getInnerStatus();
        if (ObServerInnerStatus.DELETING == innerStatus) {
            server.setStatus(ObServerStatus.DELETING);
            return;
        }
        if (ObServerInnerStatus.INACTIVE == innerStatus) {
            server.setStatus(ObServerStatus.UNAVAILABLE);
            return;
        }
        long stopTime = server.getStopTime().toEpochSecond();
        if (stopTime > 0) {
            server.setStatus(ObServerStatus.SERVICE_STOPPED);
            return;
        }
        if (zone.getStatus() == ObZoneStatus.SERVICE_STOPPED) {
            server.setStatus(ObServerStatus.SERVICE_STOPPED);
            return;
        }
        if (ObServerInnerStatus.ACTIVE == innerStatus) {
            server.setStatus(ObServerStatus.RUNNING);
        }
    }

    private void fixZoneStatus(Zone zone) {
        ObZoneInnerStatus innerStatus = zone.getInnerStatus();
        if (ObZoneInnerStatus.INACTIVE == innerStatus) {
            zone.setStatus(ObZoneStatus.SERVICE_STOPPED);
        }
        if (ObZoneInnerStatus.ACTIVE == innerStatus) {
            zone.setStatus(ObZoneStatus.RUNNING);
        }
    }
}
