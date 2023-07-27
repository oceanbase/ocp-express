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

package com.oceanbase.ocp.obops.internal.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import com.oceanbase.ocp.common.util.MultimapUtils;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.resource.ClusterResourceStatService;
import com.oceanbase.ocp.obops.resource.ClusterResourceViewService;
import com.oceanbase.ocp.obops.resource.model.ClusterUnitView;
import com.oceanbase.ocp.obops.resource.model.ClusterUnitViewOfRegion;
import com.oceanbase.ocp.obops.resource.model.ClusterUnitViewOfServer;
import com.oceanbase.ocp.obops.resource.model.ClusterUnitViewOfTenant;
import com.oceanbase.ocp.obops.resource.model.ClusterUnitViewOfUnit;
import com.oceanbase.ocp.obops.resource.model.ClusterUnitViewOfZone;
import com.oceanbase.ocp.obops.resource.model.ServerResourceStats;
import com.oceanbase.ocp.obops.resource.model.ZoneResourceStats;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerKey;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnit;
import com.oceanbase.ocp.obsdk.operator.stats.model.ObUnitStats;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClusterResourceViewServiceImpl implements ClusterResourceViewService {

    @Autowired
    private ClusterResourceStatService clusterResourceStatService;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private TenantDaoManager tenantDaoManager;

    private List<ClusterUnitViewOfTenant> getTenantInfo() {
        List<ObTenantEntity> tenants = tenantDaoManager.queryAllTenant();

        return tenants.stream()
                .filter(tenant -> tenant.getStatus() != TenantStatus.CREATING)
                .map(tenant -> ClusterUnitViewOfTenant.builder()
                        .tenantName(tenant.getName())
                        .obTenantId(tenant.getObTenantId())
                        .locality(tenant.getLocality())
                        .primaryZone(tenant.getPrimaryZone())
                        .status(tenant.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ClusterUnitView getClusterUnitView() {
        ObOperator obOperator = obOperatorFactory.createObOperator();

        UnitViewBuildContext context = new UnitViewBuildContext();
        context.tenants = tenantDaoManager.queryAllTenant();
        context.servers = managedCluster.servers();

        context.tenantInfos = getTenantInfo();

        context.obZoneList = obOperator.cluster().listZones();

        context.obUnitList = obOperator.resource().listAllUnits();

        context.obUnitStatsList = obOperator.stats().allUnitStats();

        context.serverResourceStatsList = clusterResourceStatService.serverStatsByCluster();
        context.zoneResourceStatsList = clusterResourceStatService.zoneStatsByCluster();

        context.unusedUnits = obOperator.resource().listUnusedUnit();

        UnitViewBuilder unitViewBuilder = new UnitViewBuilder(context);
        return unitViewBuilder.buildUnitView();
    }

    static class UnitViewBuildContext {

        // processed data
        List<ClusterUnitViewOfTenant> tenantInfos;

        // raw data
        List<ObTenantEntity> tenants;
        List<ObServer> servers;
        List<ObZone> obZoneList;
        List<ObUnit> obUnitList;
        List<ObUnitStats> obUnitStatsList; // unit resource stats
        List<ServerResourceStats> serverResourceStatsList; // server resource stats
        List<ZoneResourceStats> zoneResourceStatsList; // zone resource stats
        List<ObUnit> unusedUnits;

        // intermediate data
        Map<String, String> zone2RegionMap; // zone -> region map

        ListMultimap<Long, ObUnitStats> obUnitStatsMap; // unit resource stats index, key = unit_id
        Map<ObServerKey, ServerResourceStats> serverResourceStatsMap; // observer resource stats index
        Map<String, ZoneResourceStats> zoneResourceStatsMap; // zone resource stats index, key = zone_name
    }

    static class UnitViewBuilder {

        private final UnitViewBuildContext context;

        public UnitViewBuilder(UnitViewBuildContext context) {
            this.context = context;
        }

        public ClusterUnitView buildUnitView() {
            context.zone2RegionMap = context.obZoneList.stream()
                    .collect(Collectors.toMap(ObZone::getZone, ObZone::getRegion));

            context.obUnitStatsMap = Multimaps.index(context.obUnitStatsList, ObUnitStats::getUnitId);
            context.serverResourceStatsMap = context.serverResourceStatsList.stream()
                    .collect(Collectors.toMap(ServerResourceStats::getKey, Function.identity()));
            context.zoneResourceStatsMap = context.zoneResourceStatsList.stream()
                    .collect(Collectors.toMap(ZoneResourceStats::getName, Function.identity()));

            List<ClusterUnitViewOfRegion> regionInfos = buildRegionInfos();

            return ClusterUnitView.builder()
                    .tenantInfos(context.tenantInfos)
                    .regionInfos(regionInfos)
                    .build();
        }

        private List<ClusterUnitViewOfRegion> buildRegionInfos() {
            List<ClusterUnitViewOfZone> zoneInfos = buildZoneInfos();
            ListMultimap<String, ClusterUnitViewOfZone> regionZoneInfoMap = zoneInfos.stream()
                    .filter(zoneInfo -> context.zone2RegionMap.containsKey(zoneInfo.getObZoneName()))
                    .collect(MultimapUtils.groupingBy(zone -> context.zone2RegionMap.get(zone.getObZoneName())));

            return regionZoneInfoMap.keySet().stream()
                    .map(region -> buildRegionInfo(region, regionZoneInfoMap.get(region)))
                    .collect(Collectors.toList());
        }

        private ClusterUnitViewOfRegion buildRegionInfo(String regionName, List<ClusterUnitViewOfZone> zoneInfos) {
            return ClusterUnitViewOfRegion.builder()
                    .obRegionName(regionName)
                    .zoneInfos(zoneInfos)
                    .build();
        }

        private List<ClusterUnitViewOfZone> buildZoneInfos() {
            List<ClusterUnitViewOfServer> serverInfos = buildServerInfos();
            ListMultimap<String, ClusterUnitViewOfServer> zoneServerInfoMap =
                    Multimaps.index(serverInfos, ClusterUnitViewOfServer::getZone);

            return zoneServerInfoMap.keySet().stream()
                    .map(zone -> {
                        ZoneResourceStats zoneResourceStats =
                                context.zoneResourceStatsMap.getOrDefault(zone, new ZoneResourceStats());
                        List<ClusterUnitViewOfServer> zoneServerInfos = zoneServerInfoMap.get(zone);
                        return buildZoneInfo(zone, zoneResourceStats, zoneServerInfos);
                    })
                    .collect(Collectors.toList());
        }

        private ClusterUnitViewOfZone buildZoneInfo(String zoneName, ZoneResourceStats resourceStats,
                List<ClusterUnitViewOfServer> serverInfos) {
            return ClusterUnitViewOfZone.builder()
                    .obZoneName(zoneName)
                    .cpuCountAssigned(resourceStats.getCpuCoreAssigned())
                    .memorySizeAssignedByte(resourceStats.getMemoryInBytesAssigned())
                    .diskSizeUsedByte(resourceStats.getDiskInBytesUsed())
                    .unitCount(resourceStats.getUnitCount())
                    .serverInfos(serverInfos)
                    .build();
        }

        private List<ClusterUnitViewOfServer> buildServerInfos() {
            List<ClusterUnitViewOfUnit> unitInfos = buildUnitInfos();
            ListMultimap<ObServerKey, ClusterUnitViewOfUnit> serverUnitInfoMap = Multimaps.index(unitInfos,
                    unitInfo -> new ObServerKey(unitInfo.getServerIp(), unitInfo.getServerPort()));

            return context.servers.stream()
                    .map(server -> {
                        ServerResourceStats serverResourceStats =
                                context.serverResourceStatsMap.getOrDefault(server.getKey(),
                                        new ServerResourceStats());
                        List<ClusterUnitViewOfUnit> serverUnitInfos = serverUnitInfoMap.get(server.getKey());
                        return buildServerInfo(server, serverResourceStats, serverUnitInfos);
                    })
                    .collect(Collectors.toList());
        }

        private ClusterUnitViewOfServer buildServerInfo(ObServer server,
                ServerResourceStats resourceStats,
                List<ClusterUnitViewOfUnit> unitInfos) {
            String zone = server.getZone();
            String region = context.zone2RegionMap.get(zone);
            Long unusedUnitCount = context.unusedUnits.stream()
                    .filter(unit -> Objects.equals(new ObServerKey(unit.getSvrIp(), unit.getSvrPort()),
                            server.getKey()))
                    .count();
            Long totalUnitCount = unitInfos.size() + unusedUnitCount;

            return ClusterUnitViewOfServer.builder()
                    .serverIp(server.getSvrIp())
                    .serverPort(server.getSvrPort())
                    .zone(zone)
                    .region(region)
                    .cpuAssignedPercent(resourceStats.getCpuCoreAssignedPercent())
                    .cpuCountAssigned(resourceStats.getCpuCoreAssigned())
                    .diskUsedByte(resourceStats.getDiskInBytesUsed())
                    .diskUsedPercent(resourceStats.getDiskUsedPercent())
                    .memoryAssignedPercent(resourceStats.getMemoryAssignedPercent())
                    .memorySizeAssignedByte(resourceStats.getMemoryInBytesAssigned())
                    .totalCpuCount(resourceStats.getCpuCoreTotal())
                    .totalDiskSizeByte(resourceStats.getDiskInBytesTotal())
                    .totalMemorySizeByte(resourceStats.getMemoryInBytesTotal())
                    .unitCount(totalUnitCount)
                    .unusedUnitCount(unusedUnitCount)
                    .unitInfos(unitInfos)
                    .build();
        }

        private List<ClusterUnitViewOfUnit> buildUnitInfos() {
            if (CollectionUtils.isEmpty(context.obUnitList)) {
                return Collections.emptyList();
            }

            List<ClusterUnitViewOfUnit> unitInfos = new ArrayList<>();

            for (ObUnit obUnit : context.obUnitList) {
                if (obUnit.unused()) {
                    continue;
                }
                Optional<ObUnitStats> obUnitStats = getObUnitStats(obUnit);
                if (!obUnitStats.isPresent()) {
                    continue;
                }
                ClusterUnitViewOfUnit unitInfo = buildUnitInfo(obUnit, obUnitStats.get());
                unitInfos.add(unitInfo);
            }
            return unitInfos;
        }

        // A migrating unit may have multiple GV$OB_UNITS records. We only take the one
        // on migration destination observer by comparing to the DBA_OB_UNITS record.
        private Optional<ObUnitStats> getObUnitStats(ObUnit obUnit) {
            List<ObUnitStats> obUnitStatsList = context.obUnitStatsMap.get(obUnit.getUnitId());
            return obUnitStatsList.stream()
                    .filter(stats -> Objects.equals(stats.getTenantId(), obUnit.getTenantId()))
                    .filter(stats -> Objects.equals(stats.getSvrIp(), obUnit.getSvrIp()))
                    .filter(stats -> Objects.equals(stats.getSvrPort(), obUnit.getSvrPort()))
                    .findFirst();
        }

        private ClusterUnitViewOfUnit buildUnitInfo(ObUnit unit, ObUnitStats stats) {
            return ClusterUnitViewOfUnit.builder()
                    .obUnitId(unit.getUnitId())
                    .obTenantId(unit.getTenantId())
                    .tenantName(unit.getTenantName())
                    .serverIp(unit.getSvrIp())
                    .serverPort((int) (long) unit.getSvrPort())
                    .zone(unit.getZone())
                    .region(context.zone2RegionMap.get(unit.getZone()))
                    .maxCpuAssignedCount(stats.getMaxCpu())
                    .maxMemoryAssignedByte((double) stats.getMaxMemory())
                    .minCpuAssignedCount(stats.getMinCpu())
                    .minMemoryAssignedByte((double) stats.getMinMemory())
                    .diskUsedByte(stats.getDataDiskInUse())
                    .resourcePoolName(unit.getResourcePoolName())
                    .unitConfig(stats.getUnitConfigAliasName())
                    .unitConfigAliasName(stats.getUnitConfigAliasName())
                    .build();
        }
    }
}
