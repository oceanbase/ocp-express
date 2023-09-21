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

import { toNumber } from 'lodash';

/* 获取 4.0 以下集群实际的合并状态
 */
export function getCompactionStatus(
  clusterCompaction?: API.ClusterCompaction | API.TenantCompaction
) {
  // IDLE 状态也可能是等待合并调度中，此处前端扩展出 WAIT_MERGE 状态
  if (
    clusterCompaction?.status === 'IDLE' &&
    (clusterCompaction?.broadcastVersion || 0) < (clusterCompaction?.freezeVersion || 0)
  ) {
    return 'WAIT_MERGE';
  }
  return clusterCompaction?.status;
}
/* 获取集群实际的合并状态
IDLE -> 空闲
COMPACTING | VERIFYING -> 合并中
error = true -> 合并失败
 */
export function getCompactionStatusV4(tenantCompactionsList: API.TenantCompaction[]) {
  // 合并出错
  const compactionsError = tenantCompactionsList?.filter(item => item?.error).length > 0;
  // 合并中
  const isCompactionsMerging =
    tenantCompactionsList.filter(
      item => item?.status === 'VERIFYING' || item?.status === 'COMPACTING'
    ).length > 0;
  // IDLE 状态也可能是等待合并调度中，此处前端扩展出 WAIT_MERGE 状态
  const isCompactionsWaitMerge =
    tenantCompactionsList.filter(
      item => item?.status === 'IDLE' && (item?.broadcastScn || 0) !== (item?.frozenScn || 0)
    ).length === tenantCompactionsList.length;
  // 空闲
  const isCompactionsIdle =
    tenantCompactionsList.filter(item => item?.status === 'IDLE').length ===
    tenantCompactionsList.length;

  if (compactionsError) {
    return 'ERROR';
  } else if (isCompactionsMerging) {
    return 'COMPACTING';
  } else if (isCompactionsWaitMerge) {
    return 'WAIT_MERGE';
  } else if (isCompactionsIdle) {
    return 'IDLE';
  }
}

/* 获取 Zone 实际的合并状态 */
export function getZoneCompactionStatus(
  zoneCompaction?: API.ZoneCompaction,
  clusterCompaction?: API.ClusterCompaction
) {
  // IDLE 状态也可能是等待合并调度中，此处前端扩展出 WAIT_MERGE 状态
  if (
    zoneCompaction?.status === 'IDLE' &&
    (zoneCompaction?.version || 0) < (clusterCompaction?.freezeVersion || 0)
  ) {
    return 'WAIT_MERGE';
  }
  return zoneCompaction?.status;
}

/* 判断集群下所有租户的合并策略是否一致 */
export function diffClusterAllTenantMergeStrategy(
  compactionSettingsList: API.TenantCompactionSetting[]
) {
  // 取第一个配置作为标准，与数组里的进行比较
  const standardCompactionSettings = compactionSettingsList[0];
  const otherCompactionSettings = compactionSettingsList.filter(
    item =>
      standardCompactionSettings.majorFreezeDutyTime === item.majorFreezeDutyTime &&
      standardCompactionSettings.majorCompactionThreadScore === item.majorCompactionThreadScore
  );

  if (
    otherCompactionSettings.length === compactionSettingsList.length &&
    compactionSettingsList.length !== 0
  ) {
    return true;
  } else {
    return false;
  }
}

/* 根据 tenantCompaction，获取租户合并结果，仅适用 OB 4.0 及以上版本 */
export function getCompactionResult(tenantCompaction?: API.TenantCompaction) {
  // 合并出错
  return tenantCompaction?.error
    ? 'FAIL'
    : // 合并中
    tenantCompaction?.status === 'COMPACTING'
      ? 'COMPACTING'
      : // 合并成功
      'SUCCESS';
}

/* 判断集群下所有租户的转储策略是否一致 */
export function diffClusterAllTenantDumpStrategy(
  compactionSettingsList: API.TenantCompactionSetting[]
) {
  // 取第一个配置作为标准，与数组里的进行比较
  const standardCompactionSettings = compactionSettingsList[0];
  const otherCompactionSettings = compactionSettingsList.filter(
    item =>
      standardCompactionSettings.majorCompactTrigger === item.majorCompactTrigger &&
      standardCompactionSettings.minorCompactTrigger === item.minorCompactTrigger &&
      standardCompactionSettings.miniCompactionThreadScore === item.miniCompactionThreadScore &&
      standardCompactionSettings.freezeTriggerPercentage === item.freezeTriggerPercentage
  );

  if (
    otherCompactionSettings.length === compactionSettingsList.length &&
    compactionSettingsList.length !== 0
  ) {
    return true;
  } else {
    return false;
  }
}

/***
 * 1. unit 数量不允许超过 zone 中的 observer 数量
 * */
export function getUnitSpecLimit(
  zoneStats: API.ComOceanbaseOcpObopsCommonModelServerResourceStats
) {
  const { cpuCoreTotal, cpuCoreAssigned, memoryInBytesTotal, memoryInBytesAssigned } = zoneStats;
  let idleCpuCoreTotal, idleMemoryInBytesTotal;
  if (cpuCoreTotal && cpuCoreAssigned && memoryInBytesTotal && memoryInBytesAssigned) {
    // OBServer 剩余资源
    idleCpuCoreTotal = cpuCoreTotal - cpuCoreAssigned;
    idleMemoryInBytesTotal = Math.floor(toNumber(
      ((memoryInBytesTotal - memoryInBytesAssigned) / (1024 * 1024 * 1024)))
    );
  }

  return { idleCpuCoreTotal, idleMemoryInBytesTotal };
}

/***
 * 判断OBServer 剩余资源是否充足，满足 4核8GB 时展示默认值  默认值 4核8GB
 * */
export function getResourcesLimit(idleUnitSpec) {
  const { idleCpuCore, idleMemoryInBytes } = idleUnitSpec;
  return idleCpuCore > 4 && idleMemoryInBytes > 8;
}
