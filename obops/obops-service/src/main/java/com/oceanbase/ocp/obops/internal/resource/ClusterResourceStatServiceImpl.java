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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.util.ByteSizeUtils;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.resource.ClusterResourceStatService;
import com.oceanbase.ocp.obops.resource.model.ClusterResourceStats;
import com.oceanbase.ocp.obops.resource.model.ResourceStats;
import com.oceanbase.ocp.obops.resource.model.ServerResourceStats;
import com.oceanbase.ocp.obops.resource.model.ZoneResourceStats;
import com.oceanbase.ocp.obsdk.operator.StatsOperator;
import com.oceanbase.ocp.obsdk.operator.stats.model.ObServerStats;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClusterResourceStatServiceImpl implements ClusterResourceStatService {

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Override
    public ClusterResourceStats clusterStats() {
        try {
            return this.internalClusterStats();
        } catch (Exception ex) {
            log.info("call failed, message={}", ex.getMessage());
        }
        return new ClusterResourceStats();
    }

    private ClusterResourceStats internalClusterStats() {
        List<ObServerStats> allServerStats = allServerStats();
        ObServerStats clusterStats = sumServerStats(allServerStats);
        ServerResourceStats serverResourceStats = mapToModel(clusterStats);
        return setValues(new ClusterResourceStats(), serverResourceStats);
    }

    private ObServerStats sumServerStats(List<ObServerStats> serverStats) {
        return serverStats.stream().reduce((current, next) -> {
            ObServerStats stats = new ObServerStats();
            stats.setLeaderCount(current.getLeaderCount() + next.getLeaderCount());
            stats.setUnitNum(current.getUnitNum() + next.getUnitNum());
            stats.setCpuCapacity(current.getCpuCapacity() + next.getCpuCapacity());
            stats.setCpuCapacityMax(current.getCpuCapacityMax() + next.getCpuCapacityMax());
            stats.setCpuAssigned(current.getCpuAssigned() + next.getCpuAssigned());
            stats.setCpuAssignedMax(current.getCpuAssignedMax() + next.getCpuAssignedMax());
            stats.setMemCapacity(current.getMemCapacity() + next.getMemCapacity());
            stats.setMemCapacityMax(current.getMemCapacityMax() + next.getMemCapacityMax());
            stats.setMemAssigned(current.getMemAssigned() + next.getMemAssigned());
            stats.setMemAssignedMax(current.getMemAssignedMax() + next.getMemAssignedMax());
            stats.setDiskCapacity(current.getDiskCapacity() + next.getDiskCapacity());
            stats.setDiskInUse(current.getDiskInUse() + next.getDiskInUse());
            return stats;
        }).orElseGet(ObServerStats::new);
    }

    @Override
    public List<ZoneResourceStats> zoneStatsByCluster() {
        List<ObServerStats> allServerStats = allServerStats();

        Map<String, List<ObServerStats>> zoneServerStatsGroup = allServerStats.stream()
                .collect(Collectors.groupingBy(ObServerStats::getZone));

        return zoneServerStatsGroup.entrySet().stream()
                .map(e -> {
                    String zoneName = e.getKey();
                    List<ObServerStats> zoneServerStats = e.getValue();
                    ZoneResourceStats zoneResourceStats = zoneStats(zoneServerStats);
                    zoneResourceStats.setName(zoneName);
                    return zoneResourceStats;
                })
                .collect(Collectors.toList());
    }

    private ZoneResourceStats zoneStats(List<ObServerStats> zoneServerStats) {
        ObServerStats zoneStats = sumServerStats(zoneServerStats);
        ServerResourceStats serverResourceStats = mapToModel(zoneStats);
        return setValues(new ZoneResourceStats(), serverResourceStats);
    }

    @Override
    public List<ServerResourceStats> serverStatsByCluster() {
        List<ObServerStats> allServerStats = allServerStats();
        return allServerStats.stream()
                .map(this::mapToModel)
                .collect(Collectors.toList());
    }

    private List<ObServerStats> allServerStats() {
        StatsOperator statOperator = obOperatorFactory.createObOperator().stats();
        return statOperator.allServerStats();
    }


    private static double safeCalcPercent(Long divisor, Long dividend) {
        return dividend > 0 ? divisor * 100.0d / dividend : 0.0d;
    }

    private static double safeCalcPercent(Double divisor, Double dividend) {
        return dividend > 0.1d ? divisor * 100.0d / dividend : 0.0d;
    }

    private <T extends ResourceStats> T setValues(T c, ServerResourceStats serverResourceStats) {
        c.setDiskUsed(serverResourceStats.getDiskUsed());
        c.setDiskFree(serverResourceStats.getDiskFree());
        c.setDiskInBytesUsed(serverResourceStats.getDiskInBytesUsed());
        c.setDiskInBytesFree(serverResourceStats.getDiskInBytesFree());
        c.setDiskUsedPercent(serverResourceStats.getDiskUsedPercent());
        c.setUnitCount(serverResourceStats.getUnitCount());
        c.setCpuCoreTotal(serverResourceStats.getCpuCoreTotal());
        c.setCpuCoreAssigned(serverResourceStats.getCpuCoreAssigned());
        c.setCpuCoreAssignedPercent(serverResourceStats.getCpuCoreAssignedPercent());
        c.setMemoryTotal(serverResourceStats.getMemoryTotal());
        c.setMemoryAssigned(serverResourceStats.getMemoryAssigned());
        c.setMemoryInBytesTotal(serverResourceStats.getMemoryInBytesTotal());
        c.setMemoryInBytesAssigned(serverResourceStats.getMemoryInBytesAssigned());
        c.setMemoryAssignedPercent(serverResourceStats.getMemoryAssignedPercent());
        c.setDiskTotal(serverResourceStats.getDiskTotal());
        c.setDiskAssigned(serverResourceStats.getDiskAssigned());
        c.setDiskInBytesTotal(serverResourceStats.getDiskInBytesTotal());
        c.setDiskInBytesAssigned(serverResourceStats.getDiskInBytesAssigned());
        c.setDiskAssignedPercent(serverResourceStats.getDiskAssignedPercent());
        return c;
    }

    private ServerResourceStats mapToModel(ObServerStats source) {
        ServerResourceStats target = new ServerResourceStats();
        target.setIp(source.getSvrIp());
        target.setPort(source.getSvrPort());
        target.setZone(source.getZone());
        target.setPartitionCount(source.getLeaderCount());
        target.setUnitCount(source.getUnitNum());

        Double cpuCapacityMax = source.getCpuCapacityMax();
        Double cpuAssignedMax = source.getCpuAssignedMax();
        target.setCpuCoreTotal(cpuCapacityMax);
        target.setCpuCoreAssigned(cpuAssignedMax);
        target.setCpuCoreAssignedPercent(safeCalcPercent(cpuAssignedMax, cpuCapacityMax));

        Long memCapacityMax = source.getMemCapacityMax();
        Long memAssignedMax = source.getMemAssignedMax();
        target.setMemoryInBytesTotal(memCapacityMax);
        target.setMemoryInBytesAssigned(memAssignedMax);
        target.setMemoryAssignedPercent(safeCalcPercent(memAssignedMax, memCapacityMax));
        target.setMemoryTotal(ByteSizeUtils.readableByteSize(memCapacityMax));
        target.setMemoryAssigned(ByteSizeUtils.readableByteSize(memAssignedMax));

        Long diskCapacity = source.getDiskCapacity();
        Long diskInUse = source.getDiskInUse();
        Long diskFree = diskCapacity - diskInUse;
        target.setDiskInBytesTotal(diskCapacity);
        target.setDiskInBytesUsed(diskInUse);
        target.setDiskInBytesFree(diskFree);
        target.setDiskUsedPercent(safeCalcPercent(diskInUse, diskCapacity));
        target.setDiskTotal(ByteSizeUtils.readableByteSize(diskCapacity));
        target.setDiskUsed(ByteSizeUtils.readableByteSize(diskInUse));
        target.setDiskFree(ByteSizeUtils.readableByteSize(diskFree));

        return target;
    }

}
