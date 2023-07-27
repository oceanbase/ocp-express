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

/**
 * 根据备份文件的解析和计算结果，分类获取备份恢复相关的时间点数据
 * */
export function getBackupTimeData(recoverableItems: API.ObRecoverableSectionItem[]) {
  // 数据备份时间点
  const dataList = recoverableItems.filter(
    item =>
      // 数据备份: 全量
      item.obRecoverableSectionItemType === 'FULL_DATA_BACKUP' ||
      // 数据备份: 增量
      item.obRecoverableSectionItemType === 'INCREMENTAL_DATA_BACKUP'
  );
  // 日志备份时间区间
  const logList = recoverableItems.filter(
    item => item.obRecoverableSectionItemType === 'LOG_BACKUP'
  );
  // 可恢复时间区间: 对数据备份时间点和日志备份时间区间做了聚合和计算，真实可恢复
  const recoverableList = recoverableItems.filter(
    item => item.obRecoverableSectionItemType === 'RECOVERABLE'
  );
  return {
    dataList,
    logList,
    recoverableList,
  };
}
