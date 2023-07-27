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

export const REPORT_STATUS_LIST: Global.StatusItem[] = [
  {
    value: 'SUCCESSFUL',
    badgeStatus: 'success',
    label: formatMessage({ id: 'ocp-express.Detail.Report.Success', defaultMessage: '成功' }),
    operations: [
      {
        value: 'download',
        label: formatMessage({
          id: 'ocp-express.src.constant.report.Download',
          defaultMessage: '下载',
        }),
      },

      {
        value: 'view',
        label: formatMessage({ id: 'ocp-express.src.constant.report.See', defaultMessage: '查看' }),
      },
    ],
  },

  {
    value: 'FAILED',
    badgeStatus: 'error',
    label: formatMessage({ id: 'ocp-express.Detail.Report.Failed', defaultMessage: '失败' }),
    operations: [
      {
        value: 'viewTask',
        label: formatMessage({
          id: 'ocp-express.Detail.Report.ViewTasks',
          defaultMessage: '查看任务',
        }),
      },
    ],
  },

  {
    value: 'CREATING',
    badgeStatus: 'processing',
    label: formatMessage({ id: 'ocp-express.Detail.Report.Generating', defaultMessage: '生成中' }),
    operations: [
      {
        value: 'viewTask',
        label: formatMessage({
          id: 'ocp-express.Detail.Report.ViewTasks',
          defaultMessage: '查看任务',
        }),
      },
    ],
  },
];

// 用于生成左侧目录和右侧内容。
export const SQL_GROUPS_TYPE_LIST = [
  {
    value: 'BY_ELAPSED_TIME',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByTotal',
      defaultMessage: '按照总响应时间排序的 SQL',
    }),

    summary: 'totalElapsedTime',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.TotalResponseTime',
      defaultMessage: '总响应时间',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalResponseTimeS',
          defaultMessage: '总响应时间（s）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheOverallResponseTimeOf',
          defaultMessage: 'SQL 总响应时间（s）。总响应时间 = SQL 执行次数 x SQL 平均响应时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageResponseTimeMs',
          defaultMessage: '平均响应时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageSqlResponseTime',
          defaultMessage: 'SQL 平均响应时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.ProportionOfResponseTime',
          defaultMessage: '响应时间占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalResponseTimeOfThe',
          defaultMessage: '该 SQL 的总响应时间 / 全部 SQL 的总响应时间 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumResponseTimeMs',
          defaultMessage: '最长响应时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheMaximumResponseTimeOf',
          defaultMessage: 'SQL 最长响应时间（ms）',
        }),
      },
    ],
  },

  {
    value: 'BY_CPU_TIME',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByCpu',
      defaultMessage: '按照 CPU 时间排序的 SQL',
    }),

    summary: 'totalCpuTime',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.CpuTime',
      defaultMessage: ' CPU 时间',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.CpuTimeS',
          defaultMessage: 'CPU 时间（s）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheTotalCpuTimeOf',
          defaultMessage:
            'SQL 整体 CPU 时间（s）。整体 CPU 时间 = SQL 执行次数 x SQL 平均 CPU 时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageCpuTimeMs',
          defaultMessage: '平均 CPU 时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageSqlCpuTime',
          defaultMessage: 'SQL 平均 CPU 时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.CpuTimeRatio',
          defaultMessage: 'CPU 时间占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalCpuTimeOfThe',
          defaultMessage: '该 SQL 的总 CPU 时间 / 全部 SQL 的总 CPU 时间 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumCpuTimeMs',
          defaultMessage: '最长 CPU 时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheMaximumCpuTimeOf',
          defaultMessage: 'SQL 最长 CPU 时间（ms）',
        }),
      },
    ],
  },

  {
    value: 'BY_IO_WAIT_TIME',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByI',
      defaultMessage: '按照 IO 等待时间排序的 SQL',
    }),

    summary: 'totalUserIoWaitTime',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.IOWaitTime',
      defaultMessage: ' IO 等待时间',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.IOWaitTimeS',
          defaultMessage: 'IO 等待时间（s）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheOverallIOWait',
          defaultMessage:
            'SQL 整体 IO 等待时间（s）。整体 IO 等待时间 = SQL执行次数 x SQL平均 IO 等待时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageIOWaitTime',
          defaultMessage: '平均 IO 等待时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlAverageIOWait',
          defaultMessage: 'SQL 平均 IO 等待时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.RatioOfIOWaiting',
          defaultMessage: 'IO 等待时间占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalIOWaitTime',
          defaultMessage: '该 SQL 的总 IO 等待时间 / 全部 SQL 的总 IO 等待时间 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumIOWaitTime',
          defaultMessage: '最长 IO 等待时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheMaximumIOWait',
          defaultMessage: 'SQL 最长 IO 等待时间（ms）',
        }),
      },
    ],
  },

  {
    value: 'BY_NET_WAIT_TIME',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByNetwork',
      defaultMessage: '按照网络等待时间排序的 SQL',
    }),

    summary: 'totalNetWaitTime',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.NetworkWaitingTime',
      defaultMessage: '网络等待时间',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NetworkLatencyS',
          defaultMessage: '网络等待时间（s）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheTotalNetworkLatencyOf',
          defaultMessage:
            'SQL 整体网络等待时间（s）。整体网络等待时间 = SQL 执行次数 x SQL 平均网络等待时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNetworkLatencyMs',
          defaultMessage: '平均网络等待时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNetworkLatencyOfSql',
          defaultMessage: 'SQL 平均网络等待时间',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.ProportionOfNetworkWaitingTime',
          defaultMessage: '网络等待时间占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalNetworkLatencyOfThe',
          defaultMessage: '该 SQL 的总网络等待时间 / 全部 SQL 的总网络等待时间 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNetworkLatencyMs',
          defaultMessage: '最长网络等待时间（ms）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheMaximumNetworkLatencyOf',
          defaultMessage: 'SQL 最长网络等待时间（ms）',
        }),
      },
    ],
  },

  {
    value: 'BY_LOGICAL_READS',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByNumber',
      defaultMessage: '按照逻辑读次数排序的 SQL',
    }),

    summary: 'totalLogicalReads',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.LogicalReadCount',
      defaultMessage: '逻辑读次数',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.LogicalReadCount',
          defaultMessage: '逻辑读次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheTotalNumberOfLogical',
          defaultMessage:
            'SQL 整体逻辑读次数，采用的 V$SYSSTAT 中的 SSSTORE_READ_ROW_COUNT，整体逻辑读次数 = SQL 执行次数 x SQL 平均逻辑读次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfLogicalReads',
          defaultMessage: '平均逻辑读次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfLogicalReads.1',
          defaultMessage: 'SQL 平均逻辑读次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.LogicalReadCount.1',
          defaultMessage: '逻辑读次数占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalNumberOfLogicalReads',
          defaultMessage: '该 SQL 的总逻辑读次数 / 全部 SQL 的总逻辑读次数 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNumberOfLogicalReads',
          defaultMessage: '最大逻辑读次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNumberOfLogicalReads.1',
          defaultMessage: 'SQL 最大逻辑读次数',
        }),
      },
    ],
  },

  {
    value: 'BY_PHYSICAL_READS',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByNumber.1',
      defaultMessage: '按照物理读次数排序的 SQL',
    }),

    summary: 'totalPhysicalReads',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.NumberOfPhysicalReads',
      defaultMessage: '物理读次数',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfPhysicalReads',
          defaultMessage: '物理读次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheTotalNumberOfPhysical',
          defaultMessage:
            'SQL 整体物理读次数，采用的 V$SYSSTAT 中的 STORAGE_READ_ROW_COUNT，整体物理读次数 = SQL 执行次数 x SQL 平均物理读次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfPhysicalReads',
          defaultMessage: '平均物理读次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfPhysicalReads.1',
          defaultMessage: 'SQL 平均物理读次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.ProportionOfPhysicalReads',
          defaultMessage: '物理读次数占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalNumberOfPhysicalReads',
          defaultMessage: '该 SQL 的总物理读次数 / 全部 SQL 的总物理读次数 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNumberOfPhysicalReads',
          defaultMessage: '最大物理读次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNumberOfPhysicalReads.1',
          defaultMessage: 'SQL 最大物理读次数',
        }),
      },
    ],
  },

  {
    value: 'BY_EXECUTIONS',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByNumber.2',
      defaultMessage: '按照执行次数排序的 SQL',
    }),

    summary: 'totalExecutions',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
      defaultMessage: '执行次数',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalNumberOfSqlExecutions',
          defaultMessage: 'SQL 总执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: '',
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfExecutions',
          defaultMessage: '平均执行次数',
        }),

        description: '',
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.PercentageOfExecutions',
          defaultMessage: '执行次数占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalNumberOfExecutionsOf',
          defaultMessage: '该 SQL 的总执行次数 / 全部 SQL 的总执行次数 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNumberOfExecutions',
          defaultMessage: '最多执行次数',
        }),

        description: '',
      },
    ],
  },

  {
    value: 'BY_REMOTE_EXECUTIONS',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByThe',
      defaultMessage: '按照远程计划执行次数排序的 SQL',
    }),

    summary: 'totalRemoteExecutions',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.RemotePlanExecutionTimes',
      defaultMessage: '远程计划执行次数',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.RemotePlanExecutionTimes',
          defaultMessage: '远程计划执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlRemotePlanExecutionTimes',
          defaultMessage: 'SQL 远程计划执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfRemotePlan',
          defaultMessage: '平均远程计划执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheAverageNumberOfExecution',
          defaultMessage: 'SQL 平均远程计划执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.PercentageOfRemotePlanExecutions',
          defaultMessage: '远程计划执行次数占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfRemoteScheduledExecutions',
          defaultMessage: '该 SQL 的远程计划执行次数 / 全部 SQL 的总远程计划执行次数 x 100%',
        }),
      },
    ],
  },

  {
    value: 'BY_DISTRIBUTE_EXECUTIONS',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByDistributedPlanExecutions',
      defaultMessage: '按照分布式计划执行次数排序的 SQL',
    }),

    summary: 'totalDistributeExecutions',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.NumberOfDistributedPlanExecutions',
      defaultMessage: '分布式计划执行次数',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfDistributedPlanExecutions',
          defaultMessage: '分布式计划执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlDistributedPlanExecutionTimes',
          defaultMessage: 'SQL 分布式计划执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfDistributedPlan',
          defaultMessage: '平均分布式计划执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfSqlDistributed',
          defaultMessage: 'SQL 平均分布式计划执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.PercentageOfDistributedExecutions',
          defaultMessage: '分布式执行次数占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfDistributedScheduledExecutions',
          defaultMessage: '该 SQL 的分布式计划执行次数 / 全部 SQL 的总分布式计划执行次数 x 100%',
        }),
      },
    ],
  },

  {
    value: 'BY_AFFECTED_ROWS',
    label: formatMessage({
      id: 'ocp-performance-report.src.constants.report.SqlStatementsSortedByAffected',
      defaultMessage: '按照影响行数排序的 SQL',
    }),

    summary: 'totalAffectedRows',
    description: formatMessage({
      id: 'ocp-performance-report.src.constants.report.AffectedRows',
      defaultMessage: '影响行数',
    }),

    tableDescription: [
      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AffectedRows',
          defaultMessage: '影响行数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TheTotalNumberOfAffected',
          defaultMessage: 'SQL 总影响行数，总影响行数 = SQL 执行次数 x SQL 平均影响行数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.NumberOfExecutions',
          defaultMessage: '执行次数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.SqlExecutionTimes',
          defaultMessage: 'SQL 执行次数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfAffectedRows',
          defaultMessage: '平均影响行数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.AverageNumberOfAffectedSql',
          defaultMessage: 'SQL 平均影响行数',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.ProportionOfAffectedRows',
          defaultMessage: '影响行数占比（%）',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.TotalNumberOfAffectedRows',
          defaultMessage: '该 SQL 的总影响行数 / 全部 SQL 的总影响行数 x 100%',
        }),
      },

      {
        label: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNumberOfAffectedRows',
          defaultMessage: '最大影响行数',
        }),

        description: formatMessage({
          id: 'ocp-performance-report.src.constants.report.MaximumNumberOfAffectedSql',
          defaultMessage: 'SQL 最大影响行数',
        }),
      },
    ],
  },
];

export const SYS_STA_TYPE_MAP = {
  db_cpu: formatMessage({
    id: 'ocp-performance-report.src.constants.report.DatabaseCpuConsumptionS',
    defaultMessage: '数据库 CPU 耗时（s）',
  }),

  db_time: formatMessage({
    id: 'ocp-performance-report.src.constants.report.DatabaseTimeS',
    defaultMessage: '数据库耗时（s）',
  }),

  rollbacks: formatMessage({
    id: 'ocp-performance-report.src.constants.report.TransactionRollback',
    defaultMessage: '事务回滚数',
  }),

  rollback_rt: formatMessage({
    id: 'ocp-performance-report.src.constants.report.TransactionRollbackTimeMs',
    defaultMessage: '事务回滚耗时（ms）',
  }),

  rpc_packet_in: formatMessage({
    id: 'ocp-performance-report.src.constants.report.NumberOfBytesOfInternal',
    defaultMessage: '收到的内部 RPC 请求字节数',
  }),

  rpc_packet_in_rt: formatMessage({
    id: 'ocp-performance-report.src.constants.report.InternalRpcRequestTimeMs',
    defaultMessage: '内部 RPC 请求耗时（ms）',
  }),

  rpc_packet_out: formatMessage({
    id: 'ocp-performance-report.src.constants.report.NumberOfRpcRequestBytes',
    defaultMessage: '发送出去的 RPC 请求字节数',
  }),

  sql_distributed_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.DistributedSqlExecutionTimes',
    defaultMessage: '分布式 SQL 执行次数',
  }),

  sql_local_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.NumberOfLocalSqlExecutions',
    defaultMessage: '本地 SQL 执行次数',
  }),

  sql_remote_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.NumberOfRemoteSqlStatements',
    defaultMessage: '远程 SQL 执行次数',
  }),

  sql_insert_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.InsertStatementExecutionTimes',
    defaultMessage: 'Insert 语句执行次数',
  }),

  sql_delete_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.DeleteStatementExecutionTimes',
    defaultMessage: 'Delete 语句执行次数',
  }),

  sql_update_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.UpdateStatementExecutionTimes',
    defaultMessage: 'Update 语句执行次数',
  }),

  sql_select_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.SelectStatementExecutionTimes',
    defaultMessage: 'Select 语句执行次数',
  }),

  // TODO：tps -> transaction_count，V332 后端 tps 指标被占用，暂时只能使用 transaction_count 代替
  // V4.0 需要更正回去，前端暂时先做适配
  transaction_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.Transactions',
    defaultMessage: '事务数',
  }),

  // TODO：tps_rt -> transaction_rt，V332 后端 tps 指标被占用，暂时只能使用 transaction_count 代替
  // V4.0 需要更正回去，前端暂时先做适配
  transaction_rt: formatMessage({
    id: 'ocp-performance-report.src.constants.report.TransactionTimeMs',
    defaultMessage: '事务耗时（ms）',
  }),

  logical_read_row_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.LogicalReadRows',
    defaultMessage: '逻辑读行数',
  }),

  physical_read_row_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.NumberOfPhysicalReadRows',
    defaultMessage: '物理读行数',
  }),

  io_read_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.AverageNumberOfReadsPerSecond',
    defaultMessage: '平均每秒读次数',
  }),
  io_write_count: formatMessage({
    id: 'ocp-performance-report.src.constants.report.AverageNumberOfWritesPerSecond',
    defaultMessage: '平均每秒写次数',
  }),
  io_read_rt: formatMessage({
    id: 'ocp-performance-report.src.constants.report.ElapsedTimePerIORead',
    defaultMessage: '平均每次 IO 读取耗时（us）',
  }),
  io_write_rt: formatMessage({
    id: 'ocp-performance-report.src.constants.report.ElapsedTimePerIOWrite',
    defaultMessage: '平均每次 IO 写入耗时（us）',
  }),
  io_read_size: formatMessage({
    id: 'ocp-performance-report.src.constants.report.AmountOfDataReadEachTime',
    defaultMessage: '每次读取数据量（Byte）',
  }),
  io_write_size: formatMessage({
    id: 'ocp-performance-report.src.constants.report.AmountOfDataWrittenEachTime',
    defaultMessage: '每次写入数据量（Byte）',
  }),
};
