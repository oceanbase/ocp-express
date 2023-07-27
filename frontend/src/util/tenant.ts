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
import { min } from 'lodash';

// 获取集群各个 Zone 中 OBServer 数量的最小值，用于限制 OB 4.0 版本中可设置 Unit 的最大数量
export function getMinServerCount(zones: API.Zone[]) {
  if (zones?.length > 0) {
    return min(zones.map(zone => zone?.servers?.length || 0));
  }
  return 0;
}

// 根据  unitSpecLimit 说明不推荐使用的原因
export function getUnitSpecLimitText(unitSpecLimit: API.UnitSpecUnitSpecLimit) {
  const { memoryLowerLimit, cpuLowerLimit } = unitSpecLimit;
  if (memoryLowerLimit) {
    return formatMessage(
      {
        id: 'ocp-express.src.util.tenant.OnlyUnitSpecificationsWithAMemoryOfNo',
        defaultMessage: '仅可选择内存不小于 {memoryLowerLimit}G 的 Unit 规格',
      },
      { memoryLowerLimit: memoryLowerLimit }
    );
  }
  if (cpuLowerLimit) {
    return formatMessage(
      {
        id: 'ocp-express.src.util.tenant.OnlyUnitSpecificationsWithCpuNoLessThan',
        defaultMessage: '仅可选择 CPU 不小于 {cpuLowerLimit}C 的 Unit 规格',
      },
      { cpuLowerLimit: cpuLowerLimit }
    );
  }
}
