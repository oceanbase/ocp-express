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

export const OCP_AGENT_PROCESS_STATUS_LIST = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.compute.Running',
      defaultMessage: '运行中',
    }),
    value: 'RUNNING',
    badgeStatus: 'processing',
  },
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.compute.Unknown',
      defaultMessage: '未知',
    }),
    value: 'UNKNOWN',
    badgeStatus: 'warning',
  },
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.compute.Starting',
      defaultMessage: '启动中',
    }),
    value: 'STARTING',
    badgeStatus: 'success',
  },
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.compute.Stopped',
      defaultMessage: '停止中',
    }),
    value: 'STOPPING',
    badgeStatus: 'error',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.compute.NotRunning',
      defaultMessage: '未运行',
    }),
    value: 'STOPPED',
    badgeStatus: 'default',
  },
];
