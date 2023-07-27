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

import { HddFilled } from '@ant-design/icons';

// 链路节点类型列表
export const NODE_TYPE_LIST = [
  {
    value: 'CLIENT',
    label: 'Client',
    icon: <img src="/assets/icon/client.svg" width={14} height={14} />,
  },
  {
    value: 'OBPROXY',
    label: 'OBProxy',
    // 由于 DeploymentUnitOutlined 不存在对应的实底 icon，因此使用 SVG 图片代替
    icon: <img src="/assets/icon/obproxy.svg" width={14} height={14} />,
  },
  {
    value: 'OBSERVER',
    label: 'OBServer',
    icon: (
      <HddFilled
        style={{
          fontSize: 14,
          color: '#B37FEB',
        }}
      />
    ),
  },
];
