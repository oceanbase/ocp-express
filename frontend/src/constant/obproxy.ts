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

export const SYSTEM_KPI_LIST = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.CpuUsagePercentage',
      defaultMessage: 'CPU 使用百分比',
    }),

    value: 'cpu',
    unit: '%',
    kpiList: [
      {
        value: 'cpu_idle',
        label: 'cpu_idle',
      },

      {
        value: 'cpu_iowait',
        label: 'cpu_iowait',
      },

      {
        value: 'cpu_irq',
        label: 'cpu_irq',
      },

      {
        value: 'cpu_nice',
        label: 'cpu_nice',
      },

      {
        value: 'cpu_softirq',
        label: 'cpu_softirq',
      },

      {
        value: 'cpu_steal',
        label: 'cpu_steal',
      },

      {
        value: 'cpu_system',
        label: 'cpu_system',
      },

      {
        value: 'cpu_user',
        label: 'cpu_user',
      },
    ],
  },

  {
    label: 'LOAD',
    value: 'load',
    unit: '',
    kpiList: [
      {
        value: 'load1',
        label: 'load1',
      },

      {
        value: 'load5',
        label: 'load5',
      },

      {
        value: 'load15',
        label: 'load15',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.MemoryUsagePercentage',
      defaultMessage: '内存使用百分比',
    }),

    value: 'memory_percent',
    unit: '%',
    kpiList: [
      {
        value: 'memory_percent',
        label: 'memory_percent',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.MemoryUsageSize',
      defaultMessage: '内存使用大小',
    }),

    value: 'memory',
    unit: formatMessage({ id: 'ocp-express.src.constant.obproxy.Bytes', defaultMessage: '字节' }),
    kpiList: [
      {
        value: 'memory_used',
        label: 'memory_used',
      },

      {
        value: 'memory_free',
        label: 'memory_free',
      },

      {
        value: 'memory_cached',
        label: 'memory_cached',
      },

      {
        value: 'memory_buffers',
        label: 'memory_buffers',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Network',
      defaultMessage: '网络',
    }),
    value: 'net',
    unit: formatMessage({ id: 'ocp-express.src.constant.obproxy.Bytes', defaultMessage: '字节' }),
    kpiList: [
      {
        value: 'net_recv',
        label: 'net_recv',
      },

      {
        value: 'net_send',
        label: 'net_send',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.DiskUsagePercentage',
      defaultMessage: '磁盘使用百分比',
    }),

    value: 'disk_percent',
    unit: '%',
    kpiList: [
      {
        value: 'disk_percent',
        label: 'disk_percent',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.IoTimes',
      defaultMessage: 'IO 次数',
    }),
    value: 'io',
    unit: formatMessage({ id: 'ocp-express.src.constant.obproxy.Times', defaultMessage: '次' }),
    kpiList: [
      {
        value: 'ioread',
        label: 'ioread',
      },

      {
        value: 'iowrite',
        label: 'iowrite',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.IoTimeConsumption',
      defaultMessage: 'IO 耗时',
    }),

    value: 'io_time',
    unit: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Milliseconds',
      defaultMessage: '毫秒',
    }),
    kpiList: [
      {
        value: 'ioread_time',
        label: 'ioread_time',
      },

      {
        value: 'iowrite_time',
        label: 'iowrite_time',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.IoSize',
      defaultMessage: 'IO 大小',
    }),
    value: 'io_byte',
    unit: formatMessage({ id: 'ocp-express.src.constant.obproxy.Bytes', defaultMessage: '字节' }),
    kpiList: [
      {
        value: 'ioread_byte',
        label: 'ioread_byte',
      },

      {
        value: 'iowrite_byte',
        label: 'iowrite_byte',
      },
    ],
  },
];

export const SERVICE_KPI_LIST = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.NumberOfTransactions',
      defaultMessage: '事务数',
    }),

    value: 'transaction_total',
    unit: '',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.NumberOfRequests',
      defaultMessage: '请求数',
    }),

    value: 'request_total',
    unit: '',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.SqlProcessingTime',
      defaultMessage: 'SQL 处理耗时',
    }),

    value: 'request_cost',
    unit: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Microseconds',
      defaultMessage: '微秒',
    }),
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.ErrorNumberOfResponsePackages',
      defaultMessage: 'ERROR 响应包数',
    }),

    value: 'error_response',
    unit: '',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.NumberOfLinks',
      defaultMessage: '链接数',
    }),

    value: 'current_session',
    unit: '',
  },
];

export const OB_PROXY_STATUS_LIST = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Running',
      defaultMessage: '运行中',
    }),
    value: 'RUNNING',
    badgeStatus: 'success',
    operations: [
      {
        value: 'upgrade',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.Upgrade',
          defaultMessage: '升级',
        }),
      },

      {
        value: 'restart',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.Restart',
          defaultMessage: '重启',
        }),
      },

      {
        value: 'refresh',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.RefreshConfigurations',
          defaultMessage: '刷新配置',
        }),
      },

      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.Delete',
          defaultMessage: '删除',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Restarting',
      defaultMessage: '重启中',
    }),

    value: 'RESTARTING',
    badgeStatus: 'warning',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Upgrade.1',
      defaultMessage: '升级中',
    }),
    value: 'UPGRADING',
    badgeStatus: 'warning',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.TakingOver',
      defaultMessage: '接管中',
    }),
    value: 'TAKINGOVER',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.RefreshingConfiguration',
      defaultMessage: '刷新配置中',
    }),

    value: 'REFRESHING',
    badgeStatus: 'warning',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Deleting',
      defaultMessage: '删除中',
    }),
    value: 'DELETING',
    badgeStatus: 'warning',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Creating',
      defaultMessage: '创建中',
    }),
    value: 'CREATING',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.Unavailable',
      defaultMessage: '不可用',
    }),

    value: 'UNAVAILABLE',
    badgeStatus: 'error',
    operations: [
      {
        value: 'restart',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.Restart',
          defaultMessage: '重启',
        }),
      },

      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.Delete',
          defaultMessage: '删除',
        }),
      },
    ],
  },
  {
    value: 'OPERATING',
    label: formatMessage({ id: 'ocp-express.src.constant.obproxy.OM', defaultMessage: '运维中' }),
    badgeStatus: 'warning',
  },

  {
    value: 'NODE_CRASH',
    label: formatMessage({
      id: 'ocp-express.src.constant.obproxy.NodeException',
      defaultMessage: '节点异常',
    }),
    badgeStatus: 'warning',
  },
];

export const OPERATING = ['OPERATING', 'UPDATE', 'DELETE'];
export const ABNORMAL = ['ABNORMAL'];

export const OB_PROXY_BATCH_STATUS_LIST = [
  {
    value: 'NORMAL',
    parameters: ['NORMAL'],
    operations: [
      {
        value: 'upgrade',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchUpgrade',
          defaultMessage: '批量升级',
        }),
      },

      {
        value: 'restart',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchRestart',
          defaultMessage: '批量重启',
        }),
      },

      {
        value: 'refresh',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchRefresh',
          defaultMessage: '批量刷新',
        }),
      },

      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchDownloadLogs',
          defaultMessage: '批量下载日志',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchDelete',
          defaultMessage: '批量删除',
        }),

        buttonProps: {
          danger: true,
        },
      },
    ],
  },

  {
    value: 'OPERATING',
    parameters: ['OPERATING', 'UPDATE', 'DELETE'],
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchDownloadLogs',
          defaultMessage: '批量下载日志',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchDelete',
          defaultMessage: '批量删除',
        }),

        buttonProps: {
          danger: true,
        },
      },
    ],
  },

  {
    value: 'ABNORMAL',
    parameters: ['ABNORMAL'],
    operations: [
      {
        value: 'restart',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchRestart',
          defaultMessage: '批量重启',
        }),
      },

      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchDownloadLogs',
          defaultMessage: '批量下载日志',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.obproxy.BatchDelete',
          defaultMessage: '批量删除',
        }),

        buttonProps: {
          danger: true,
        },
      },
    ],
  },
];

export const SYSTEM_LIST_ATTR = [
  'cpu_system',
  'cpu_user',
  'load1',
  'load5',
  'memory_used',
  'memory_percent',
  'disk_percent',
  'net_recv',
];

export const WORK_MODE_LIST = [
  {
    label: 'ConfigUrl',
    value: 'CONFIG_URL',
  },

  {
    label: 'RsList',
    value: 'RS_LIST',
  },
];
