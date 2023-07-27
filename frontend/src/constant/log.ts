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

import moment from 'moment';
import { formatMessage } from '@/util/intl';

// 获取适用于 ob-ui Ranger 组件的 selects，该格式与 antd 的不同
export function getSelects() {
  return [
    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearFenZhong',
        defaultMessage: '近 5 分钟',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 5 Minutes',
        },
      ],

      range: () => [moment().subtract(5, 'minute'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearFenZhong.1',
        defaultMessage: '近 10 分钟',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 10 Minutes',
        },
      ],

      range: () => [moment().subtract(10, 'minute'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearMins',
        defaultMessage: '近 15 分钟',
      }),
      locale: [
        {
          lang: 'en-US',
          name: 'Last 15 Minutes',
        },
      ],

      range: () => [moment().subtract(15, 'minute'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearFenZhong.2',
        defaultMessage: '近 30 分钟',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 30 Minutes',
        },
      ],

      range: () => [moment().subtract(30, 'minute'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearlyHour',
        defaultMessage: '近 1 小时',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 1 Hour',
        },
      ],

      range: () => [moment().subtract(1, 'hours'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearlyHours',
        defaultMessage: '近 3 小时',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 3 Hours',
        },
      ],

      range: () => [moment().subtract(3, 'hours'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearlyHours.1',
        defaultMessage: '近 6 小时',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 6 Hours',
        },
      ],

      range: () => [moment().subtract(6, 'hours'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearlyHours.2',
        defaultMessage: '近 12 小时',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 12 Hours',
        },
      ],

      range: () => [moment().subtract(12, 'hours'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.log.NearHours',
        defaultMessage: '近 24 小时',
      }),

      locale: [
        {
          lang: 'en-US',
          name: 'Last 24 Hours',
        },
      ],

      range: () => [moment().subtract(24, 'hours'), moment()],
    },
  ];
}

export const LOG_TYPE_LIST = [
  {
    value: 'CLUSTER',
    label: formatMessage({
      id: 'ocp-express.src.constant.compute.ObserverLog',
      defaultMessage: 'OBServer 日志',
    }),
    types: ['observer', 'rootservice', 'election'],
  },

  {
    value: 'OBAGENT',
    label: formatMessage({
      id: 'ocp-express.src.constant.log.ObagentLogs',
      defaultMessage: 'OBAgent 日志',
    }),
    types: ['mgragent', 'monagent', 'agentctl', 'agentd'],
  },
];

export const LOG_LEVEL = ['ERROR', 'WARN', 'INFO', 'EDIAG', 'WDIAG', 'TRACE', 'DEBUG'];
