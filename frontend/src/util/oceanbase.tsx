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

import { formatMessage } from '@/util/intl';
import { flatten, uniq, orderBy } from 'lodash';

// 将主集群中的备集群展开，以获取全部主备集群列表
export function getAllFromPrimaryClusterList(clusterList: API.ClusterInfo[]) {
  return flatten((clusterList || []).map(item => [item]));
}

export function getRegionCountByObCluster(clusterData: API.Cluster) {
  const regionNameList =
    (clusterData.zones && clusterData.zones.map(item => item.regionName)) || [];
  return uniq(regionNameList).length;
}

export function getIdcCountByObCluster(clusterData: API.Cluster) {
  const idcNameList = (clusterData.zones && clusterData.zones.map(item => item.idcName)) || [];
  return uniq(idcNameList).length;
}

export function getObServerCountByObCluster(clusterData: API.Cluster) {
  const zones = clusterData.zones || [];
  return (
    // reduce 方法要求数组长度大于 0
    (zones.length > 0 && zones.map(item => item.hostCount).reduce((a, b) => (a || 0) + (b || 0))) ||
    0
  );
}

export function getObServersByObCluster(clusterData: API.ClusterInfo) {
  const zones = clusterData.zones || [];
  return flatten(
    zones.map(item =>
      item.servers.map(server => ({
        ...server,
        zoneName: item.name,
      }))
    )
  );
}

export function validateUnitCount(rule, value, callback, zoneData: API.Zone) {
  const { servers = [] } = zoneData;
  if (value && value > servers.length) {
    callback(
      formatMessage({
        id: 'ocp-express.src.util.oceanbase.CannotExceedTheNumberOf',
        defaultMessage: '不能超过当前 Zone 的 OBServer 数量',
      })
    );
  }
  callback();
}
/***
 * 1. unit 数量不允许超过 zone 中的 observer 数量
 * 2. zone 中需要有足够的 observer 的剩余资源大于等于 unit 规格，满足条件的 observer 数量需要大于等于 unit 数量
 * */
export function validatorUnitResource(
  rule,
  unitSpec,
  callback,
  zoneData: API.Zone,
  currentUnitCount: number
) {
  const { maxCpuCoreCount, maxMemorySize } = unitSpec;
  // OBServer 剩余资源
  // 筛选出满足 剩余资源大于等于 unit 规格的 observer
  const checkFailedOBServersStats: any = [];
  const checkpassedOBServers = zoneData?.servers?.filter(server => {
    // 存在部分server 缺失 unit 资源信息，直接放过，允许创建，由后端处理
    if (!server?.stats) {
      callback();
    }
    const { cpuCoreTotal, cpuCoreAssigned, memoryInBytesTotal, memoryInBytesAssigned } =
      server?.stats;

    const idleCpuCoreTotal = cpuCoreTotal - cpuCoreAssigned;
    const idleMemoryInBytesTotal = (
      (memoryInBytesTotal - memoryInBytesAssigned) /
      (1024 * 1024 * 1024)
    ).toFixed(1);

    // 所选 unit 规格 要小于剩余资源需要
    if (maxCpuCoreCount <= idleCpuCoreTotal && maxMemorySize <= idleMemoryInBytesTotal) {
      return server;
    } else {
      checkFailedOBServersStats.push({
        idleCpuCoreTotal,
        idleMemoryInBytesTotal,
      });
    }
  });

  if (
    zoneData?.servers?.length >= currentUnitCount &&
    checkpassedOBServers?.length < currentUnitCount
  ) {
    const minOBServersStats =
      orderBy(
        checkFailedOBServersStats,
        ['idleCpuCoreTotal', 'idleMemoryInBytesTotal'],
        ['asc', 'asc']
      )?.[0] || {};
    callback(
      formatMessage(
        {
          id: 'ocp-express.src.util.oceanbase.ObserverOfTheResourcesInTheZoneAre',
          defaultMessage:
            'Zone 中的 OBServer 资源不足，请选择 {minOBServersStatsIdleCpuCoreTotal}C{minOBServersStatsIdleMemoryInBytesTotal}G 以下的 Unit 规格',
        },
        {
          minOBServersStatsIdleCpuCoreTotal: minOBServersStats.idleCpuCoreTotal,
          minOBServersStatsIdleMemoryInBytesTotal: minOBServersStats.idleMemoryInBytesTotal,
        }
      )
    );
  } else {
    callback();
  }
}
