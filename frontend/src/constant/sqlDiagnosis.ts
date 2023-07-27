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

import { NEAR_30_MINUTES } from '@/component/OCPRangePicker/constant';
import { formatMessage } from '@/util/intl';

// 采集启动时需要的默认时间范围
export const DEFAULT_RANGE = {
  key: NEAR_30_MINUTES.name,
  // 实时更新 range 值
  get range() {
    return NEAR_30_MINUTES.range();
  },
};

// SQL 类型: TopSQL、SlowSQL
export const SQL_TYPE_LIST = [
  {
    value: 'topSql',
    label: 'TopSQL',
    defaultFieldList: [
      // SQL 文本
      'sqlTextShort',
      // 数据库
      'dbName',
      // 执行次数
      'executions',
      // 总响应时间
      'sumElapsedTime',
      // 平均响应时间
      'avgElapsedTime',
      //  报错次数
      'failCount',
      // 计划生成时间
      'avgGetPlanTime',
    ],
  },

  {
    value: 'slowSql',
    label: 'SlowSQL',
    defaultFieldList: [
      // SQL 文本
      'sqlTextShort',
      // 数据库
      'dbName',
      // 执行次数
      'executions',
      // 总响应时间
      'sumElapsedTime',
      // 最大响应时间
      'maxElapsedTime',
      // 最大返回行数
      'maxReturnRows',
      // 最大应用等待
      'maxApplicationWaitTime',
      // 最大物理读
      'maxDiskReads',
    ],
  },
];

export const ATTRIBUTE_GROUPS = [
  {
    name: 'FAVORITE',
    title: formatMessage({
      id: 'ocp-express.Detail.SQLDiagnosis.constant.CommonMetrics',
      defaultMessage: '常用指标',
    }),
  },

  {
    name: 'BASIC',
    title: formatMessage({
      id: 'ocp-express.Detail.SQLDiagnosis.constant.BasicInformation',
      defaultMessage: '基础信息',
    }),
  },

  {
    name: 'TIME_STAT',
    title: formatMessage({
      id: 'ocp-express.Detail.SQLDiagnosis.constant.TimeModel',
      defaultMessage: '时间模型',
    }),
  },

  {
    name: 'PLAN_STAT',
    title: formatMessage({
      id: 'ocp-express.Detail.SQLDiagnosis.constant.ExecutionPlan',
      defaultMessage: '执行计划',
    }),
  },
];

export const OUTLINE_STATUS_LIST: Global.StatusItem[] = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.TakingEffect',
      defaultMessage: '生效中',
    }),

    value: 'VALID',
    badgeStatus: 'processing',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.Canceled',
      defaultMessage: '已取消',
    }),

    value: 'CANCELED',
    badgeStatus: 'default',
  },
];

export const OUTLINE_TYPE_LIST: Global.StatusItem[] = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.IndexBinding',
      defaultMessage: '索引绑定',
    }),

    value: 'INDEX',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.PlanBinding',
      defaultMessage: '计划绑定',
    }),

    value: 'PLAN',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.SqlThrottling',
      defaultMessage: 'SQL 限流',
    }),

    value: 'CONCURRENT_LIMIT',
  },
];

// 执行计划变化趋势相关的指标
export const PLAN_ATTRIBUTE_LIST = [
  {
    dataType: 'INTEGER',
    name: 'executions',
    displayByDefault: true,
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.NumberOfExecutions',
      defaultMessage: '执行次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheTotalNumberOfScheduled',
      defaultMessage: '指定时间段内执行计划的总执行次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'execPs',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.NumberOfExecutionsPerSecond',
      defaultMessage: '每秒执行次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageNumberOfScheduled',
      defaultMessage: '指定时间段内执行计划的平均每秒执行次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgElapsedTime',
    displayByDefault: true,
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.AverageResponseTime',
      defaultMessage: '平均响应时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageResponseTimeOf',
      defaultMessage: '指定时间段内执行计划的平均响应时间',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgCpuTime',
    displayByDefault: true,
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.CpuTime',
      defaultMessage: 'CPU 时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageCpuTimeIn',
      defaultMessage: '指定时间段内执行计划的平均 CPU 时间',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgRowProcessed',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.NumberOfProcessedRows',
      defaultMessage: '处理行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageNumberOfRows',
      defaultMessage: '指定时间段内执行计划的平均处理行数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgDiskReads',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.PhysicalRead',
      defaultMessage: '物理读',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageNumberOfPhysical',
      defaultMessage: '指定时间段内执行计划的平均物理读次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgDiskWrites',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.PhysicalWrite',
      defaultMessage: '物理写',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageNumberOfPhysical.1',
      defaultMessage: '指定时间段内执行计划的平均物理写次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgBufferGets',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.CacheRead',
      defaultMessage: '缓存读',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageNumberOfCached',
      defaultMessage: '指定时间段内执行计划的平均缓存读次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgApplicationWaitTime',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ApplicationWaitingTime',
      defaultMessage: '应用等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageApplicationEventWaiting',
      defaultMessage: '指定时间段内执行计划的平均 Application 事件等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgConcurrencyWaitTime',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ConcurrentWaitTime',
      defaultMessage: '并发等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageConcurrencyEventWaiting',
      defaultMessage: '指定时间段内执行计划的 Concurrency 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgUserIoWaitTime',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.IOWaitTime',
      defaultMessage: 'IO 等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.TheAverageWaitTimeIn',
      defaultMessage: '指定时间段内执行计划的平均 UserIO 事件等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'largeQueryPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfLargeQueries',
      defaultMessage: '大查询占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.PercentageOfLargeQueriesScheduled',
      defaultMessage: '指定时间段内执行计划的大查询占比（%）',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'delayedLargeQueryPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfLargeLatencyQueries',
      defaultMessage: '延迟大查询占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.planAttributes.ThePercentageOfLargeQueries',
      defaultMessage: '指定时间段内执行计划的被置入队列的大查询的百分比',
    }),

    unit: '%',
  },
];

// 历史趋势相关的指标
export const TREND_ATTRIBUTE_LIST = [
  {
    dataType: 'INTEGER',
    name: 'executions',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NumberOfExecutions',
      defaultMessage: '执行次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfSql',
      defaultMessage: '指定时间段内 SQL 的总执行次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'execPs',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NumberOfExecutionsPerSecond',
      defaultMessage: '每秒执行次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNumberOfSql',
      defaultMessage: '指定时间段内 SQL 的平均每秒执行次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'sumElapsedTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TotalResponseTime',
      defaultMessage: '总响应时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalResponseTimeIn',
      defaultMessage: '指定时间段内 SQL 的总响应 时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    displayByDefault: true,
    name: 'avgElapsedTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.AverageResponseTime',
      defaultMessage: '平均响应时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageResponseTimeIn',
      defaultMessage:
        '指定时间段内 SQL 的平均响应时间（ms）。响应时间指客户端从发送 SQL 请求，到获取 SQL 结果的总时间。',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'maxElapsedTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.MaximumResponseTime',
      defaultMessage: '最大响应时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheMaximumResponseTimeIn',
      defaultMessage: '指定时间段内 SQL 的最大响应时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    displayByDefault: true,
    name: 'avgCpuTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.CpuTime',
      defaultMessage: 'CPU 时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageCpuTimeIn',
      defaultMessage: '执行计划在指定时间段内 SQL 平均占用 CPU 的时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'maxCpuTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.MaximumCpuTime',
      defaultMessage: '最大 CPU 时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheMaximumCpuTimeIn',
      defaultMessage: '指定时间段内 SQL 最大占用 CPU 时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgNetTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NetworkTransmissionTime',
      defaultMessage: '网络传输时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNetworkTransmissionTime',
      defaultMessage: '指定时间段内 SQL 的平均网络传输时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgNetWaitTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NetworkJoiningTime',
      defaultMessage: '网络入队时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNetworkQueuingTime',
      defaultMessage: '指定时间段内 SQL 的平均网络入队时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgQueueTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.QueuingTime',
      defaultMessage: '排队时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageQueuingTimeIn',
      defaultMessage: '指定时间段内 SQL 的平均排队时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgDecodeTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.ResolutionTime',
      defaultMessage: '解析时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageSyntaxParsingTime',
      defaultMessage: '指定时间段内 SQL 的平均语法解析时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    displayByDefault: true,
    name: 'avgExecuteTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.ExecutionTime',
      defaultMessage: '执行时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageScheduledExecutionTime',
      defaultMessage: '指定时间段内 SQL 计划执行的平均时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgAffectedRows',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.UpdatedRows',
      defaultMessage: '更新行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.AverageNumberOfSqlUpdate',
      defaultMessage: '指定时间段内 SQL 平均更新表记录行数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgReturnRows',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NumberOfReturnedRows',
      defaultMessage: '返回行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNumberOfRows',
      defaultMessage: '指定时间段内 SQL 的平均返回行数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgPartitionCount',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NumberOfAccessPartitions',
      defaultMessage: '访问分区数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.AverageNumberOfPartitionsAccessed',
      defaultMessage: '指定时间段内 SQL 平均访问的分区数',
    }),
  },

  {
    dataType: 'INTEGER',
    name: 'failCount',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NumberOfErrors',
      defaultMessage: '错误次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfSql.1',
      defaultMessage: '指定时间段内 SQL 的总错误次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'failPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ErrorPercentage',
      defaultMessage: '错误占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.ThePercentageOfSqlErrors',
      defaultMessage: '指定时间段内 SQL 的错误占比（%）',
    }),

    unit: '%',
  },

  {
    dataType: 'INTEGER',
    name: 'retCode4012Count',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TimeoutError',
      defaultMessage: '超时错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfTimes',
      defaultMessage: '指定时间段内 SQL 执行发生超时错误（4012）的总次数',
    }),
  },

  {
    dataType: 'INTEGER',
    name: 'retCode4013Count',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.MemoryShortageError',
      defaultMessage: '内存不足错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfMemory',
      defaultMessage: '指定时间段内 SQL 执行发生内存不足错误（4013）的总次数',
    }),
  },

  {
    dataType: 'INTEGER',
    name: 'retCode5001Count',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.SyntaxError',
      defaultMessage: '语法错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfSyntax',
      defaultMessage: '指定时间段内 SQL 执行发生语法解析错误（5001）的总次数',
    }),
  },

  {
    dataType: 'INTEGER',
    name: 'retCode5024Count',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.KeyValueConflictError',
      defaultMessage: '键值冲突错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfKey',
      defaultMessage: '指定时间段内 SQL 执行发生键值冲突错误（5024）的总次数',
    }),
  },

  {
    dataType: 'INTEGER',
    name: 'retCode5167Count',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.DataOverrunError',
      defaultMessage: '数据超长错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfTimes.1',
      defaultMessage: '指定时间段内 SQL 执行发生数据超长错误（5167）的总次数',
    }),
  },

  {
    dataType: 'INTEGER',
    name: 'retCode5217Count',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.UnknownColumnError',
      defaultMessage: '未知列错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheTotalNumberOfUnknown',
      defaultMessage: '指定时间段内 SQL 执行发生未知列错误（5217）的总次数',
    }),
  },

  {
    dataType: 'INTEGER',
    name: 'retCode6002Count',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TransactionRollbackError',
      defaultMessage: '事务回滚错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TotalNumberOfTransactionRollback',
      defaultMessage: '指定时间段内 SQL 执行发生事务回滚错误（6002）的总次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgWaitTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.WaitingTime',
      defaultMessage: '等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageWaitTimeIn',
      defaultMessage: '指定时间段内 SQL 平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgWaitCount',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.WaitingTimes',
      defaultMessage: '等待次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.AverageNumberOfSqlWaits',
      defaultMessage: '指定时间段内 SQL 的平均等待次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgRpcCount',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.RpcCount',
      defaultMessage: 'RPC 次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNumberOfRpc',
      defaultMessage: '指定时间段内 SQL 的平均发送 RPC 次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'localPlanPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfLocalPlans',
      defaultMessage: '本地计划占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.PercentageOfLocalSqlPlans',
      defaultMessage: '指定时间段内 SQL 的本地计划占比（%）',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'remotePlanPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.RemotePlanProportion',
      defaultMessage: '远程计划占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.PercentageOfRemoteSqlPlans',
      defaultMessage: '指定时间段内 SQL 的远程计划占比（%）',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'distPlanPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfDistributedPlans',
      defaultMessage: '分布式计划占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.PercentageOfSqlDistributedPlans',
      defaultMessage: '指定时间段内 SQL 的分布式计划占比（%）',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'avgExecutorRpcCount',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.RpcRequest',
      defaultMessage: 'RPC 请求',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.AverageNumberOfRpcRequests',
      defaultMessage: '指定时间段内 SQL 平均执行 RPC 请求次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'missPlanPercentage',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.MissPlan',
      defaultMessage: '未命中计划',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.ThePlannedMissRateOf',
      defaultMessage: '指定时间段内 SQL 计划未命中率',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'avgApplicationWaitTime',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ApplicationWaitingTime',
      defaultMessage: '应用等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageApplicationEventWait',
      defaultMessage: '指定时间段内 Application 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgConcurrencyWaitTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.ConcurrentWaiting',
      defaultMessage: '并发等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageConcurrencyEventWait',
      defaultMessage: '指定时间段内执行计划的 Concurrency 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgUserIoWaitTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.IOWaiting',
      defaultMessage: 'IO 等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageWaitTimeIn.1',
      defaultMessage: '指定时间段内 UserIO 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgScheduleTime',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.SchedulingWait',
      defaultMessage: '调度等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageScheduleEventWait',
      defaultMessage: '指定时间段内 Schedule 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    dataType: 'FLOAT',
    name: 'avgRowCacheHit',
    title: 'Row Cache Hit',
    tooltip: 'Row Cache Hit',
  },

  {
    dataType: 'FLOAT',
    name: 'avgBloomFilterCacheHit',
    title: 'Bloom filter Cache Hit',
    tooltip: 'Bloom filter Cache Hit',
  },

  {
    dataType: 'FLOAT',
    name: 'avgBlockCacheHit',
    title: 'Block Cache Hit',
    tooltip: 'Block Cache Hit',
  },

  {
    dataType: 'FLOAT',
    name: 'avgBlockIndexCacheHit',
    title: 'Block Index Cache Hit',
    tooltip: 'Block Index Cache Hit',
  },

  {
    dataType: 'FLOAT',
    name: 'avgDiskReads',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.PhysicalRead',
      defaultMessage: '物理读',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNumberOfPhysical',
      defaultMessage: '指定时间段内 SQL 的平均物理读次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'retryCount',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.Retries',
      defaultMessage: '重试次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TotalNumberOfSqlRetries',
      defaultMessage: '指定时间段内 SQL 的总重试次数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'tableScanPercentage',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TableScan',
      defaultMessage: '表扫描',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.AverageNumberOfSqlTable',
      defaultMessage: '指定时间段内 SQL 的平均表扫描次数',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'strongConsistencyPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfStronglyConsistentTransactions',
      defaultMessage: '强一致性事务占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.PercentageOfStronglyConsistentSql',
      defaultMessage: '指定时间段内 SQL 强一致性事务占比（%）',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'weakConsistencyPercentage',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfTransactionsWithWeak',
      defaultMessage: '弱一致性事务占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.ThePercentageOfSqlTransactions',
      defaultMessage: '指定时间段内 SQL 的弱一致性事务占比（%）',
    }),

    unit: '%',
  },

  {
    dataType: 'FLOAT',
    name: 'avgMemstoreReadRows',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.MemoryReadRows',
      defaultMessage: '内存读行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNumberOfMemstore',
      defaultMessage: '指定时间段内 SQL 的平均 Memstore 读行数',
    }),
  },

  {
    dataType: 'FLOAT',
    name: 'avgSsstoreReadRows',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.NumberOfPhysicalReadRows',
      defaultMessage: '物理读行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.TheAverageNumberOfSsstore',
      defaultMessage: '指定时间段内 SQL 平均读 Ssstore 行数',
    }),
  },
];

// SQL 诊断相关的指标
export const SQL_ATTRIBUTE_LIST = [
  {
    allowSearch: false,
    displayAlways: true,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'sqlTextShort',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.SqlText',
      defaultMessage: 'SQL 文本',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.ShortTextOfSql',
      defaultMessage: 'SQL 的短文本',
    }),
  },

  {
    allowSearch: true,
    displayByDefault: true,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'sqlId',
    title: 'SQL ID',
    tooltip: 'SQL ID',
  },

  {
    allowSearch: false,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'server',
    operation: 'FILTER',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.Server',
      defaultMessage: '服务器',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheIpAddressOfThe',
      defaultMessage: 'OB 服务器的地址',
    }),
  },

  {
    allowSearch: false,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'dbName',
    operation: 'FILTER',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.Database',
      defaultMessage: '数据库',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.DatabaseAccessedBySql',
      defaultMessage: 'SQL 访问的数据库',
    }),
  },

  {
    allowSearch: false,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'userName',
    operation: 'FILTER',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.User',
      defaultMessage: '用户',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheUserWhoRunsThe',
      defaultMessage: '执行 SQL 的用户',
    }),
  },

  {
    // ParallelSQL 专属指标
    sqlType: 'parallelSql',
    allowSearch: false,
    displayAlways: true,
    dataType: 'STRING',
    group: 'BASIC',
    // 平均并行度，因为是用于 SQL 列表，展示的是平均值、而不是汇总值
    name: 'avgExpectedWorkerCount',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.AverageParallelism',
      defaultMessage: '平均并行度',
    }),
    tooltip: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.AverageNumberOfExpectedThreads',
      defaultMessage: 'SQL 执行的平均预期线程数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'sqlType',
    operation: 'FILTER',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.SqlType',
      defaultMessage: 'SQL 类型',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheSqlTypeValidValues',
      defaultMessage: 'SQL 类型，包括：SELECT、INSERT、UPDATE、DELETE',
    }),
  },

  {
    allowSearch: false,
    dataType: 'BOOLEAN',
    group: 'BASIC',
    name: 'inner',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.InternalSql',
      defaultMessage: '内部 SQL',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheSqlInitiatedByOb',
      defaultMessage: '由 OB 发起的 SQL 为内部 SQL',
    }),
  },

  {
    // SuspiciousSql 专属指标
    sqlType: 'suspiciousSql',
    allowSearch: false,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'lastExecutedTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.LastExecutionTime',
      defaultMessage: '最后执行时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.TheTimeWhenTheSql',
      defaultMessage: '所选区间内该 SQL 最后一次执行的时间',
    }),
  },

  {
    allowSearch: false,
    dataType: 'STRING',
    group: 'FAVORITE',
    name: 'waitEvent',
    operation: 'FILTER',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumWaitingEvents',
      defaultMessage: '最长等待事件',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheMostTimeConsumingWaiting',
      defaultMessage: '累计耗时最长的等待事件',
    }),
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'FAVORITE',
    name: 'executions',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.NumberOfExecutions',
      defaultMessage: '执行次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalNumberOfSql',
      defaultMessage: '指定时间段内 SQL 的总执行次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'sumElapsedTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TotalResponseTime',
      defaultMessage: '总响应时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalResponseTimeIn',
      defaultMessage: '指定时间段内 SQL 的总响应 时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'FAVORITE',
    name: 'avgElapsedTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.AverageResponseTime',
      defaultMessage: '平均响应时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageResponseTimeIn',
      defaultMessage:
        '指定时间段内 SQL 的平均响应时间（ms）。响应时间指客户端从发送 SQL 请求，到获取 SQL 结果的总时间。',
    }),

    unit: 'ms',
  },

  {
    // TopSql 专属指标
    sqlType: 'topSql',
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'FAVORITE',
    name: 'failCount',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.NumberOfErrorsReported',
      defaultMessage: '报错次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.SqlExecutionErrors',
      defaultMessage: 'SQL 执行出错次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'maxElapsedTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumResponseTime',
      defaultMessage: '最大响应时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheMaximumResponseTimeIn',
      defaultMessage: '指定时间段内 SQL 的最大响应时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'FAVORITE',
    name: 'avgCpuTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.AverageCpuTime',
      defaultMessage: '平均 CPU 时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageCpuTimeIn',
      defaultMessage: '指定时间段内 SQL 平均占用 CPU 时间（ms)',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'maxCpuTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumCpuTime',
      defaultMessage: '最大 CPU 时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheMaximumCpuTimeIn',
      defaultMessage: '指定时间段内 SQL 最大占用 CPU 时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgNetTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.NetworkTransmissionTime',
      defaultMessage: '网络传输时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageNetworkTransmissionTime',
      defaultMessage: '指定时间段内 SQL 的平均网络传输时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgNetWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.NetworkJoiningTime',
      defaultMessage: '网络入队时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageNetworkQueuingTime',
      defaultMessage: '指定时间段内 SQL 的平均网络入队时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgQueueTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.QueuingTime',
      defaultMessage: '排队时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageQueuingTimeIn',
      defaultMessage: '指定时间段内 SQL 的平均排队时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgDecodeTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.ResolutionTime',
      defaultMessage: '解析时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageSyntaxParsingTime',
      defaultMessage: '指定时间段内 SQL 的平均语法解析时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'FAVORITE',
    name: 'avgGetPlanTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.PlanGenerationTime',
      defaultMessage: '计划生成时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageScheduledGenerationTime',
      defaultMessage: '指定时间段内 SQL 计划生成的平均时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'FAVORITE',
    name: 'avgExecuteTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.ExecutionTime',
      defaultMessage: '执行时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageScheduledExecutionTime',
      defaultMessage: '指定时间段内 SQL 计划执行的平均时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgAffectedRows',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.UpdatedRows',
      defaultMessage: '更新行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.AverageNumberOfSqlUpdate',
      defaultMessage: '指定时间段内 SQL 平均更新表记录行数',
    }),
  },

  {
    sqlType: 'slowSql',
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'maxAffectedRows',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumNumberOfUpdatedRows',
      defaultMessage: '最大更新行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumNumberOfSqlUpdate',
      defaultMessage: '指定时间段内 SQL 的最大更新行数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgReturnRows',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.NumberOfReturnedRows',
      defaultMessage: '返回行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageNumberOfRows',
      defaultMessage: '指定时间段内 SQL 的平均返回行数',
    }),
  },

  {
    sqlType: 'slowSql',
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'maxReturnRows',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumNumberOfReturnedRows',
      defaultMessage: '最大返回行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheMaximumNumberOfRows',
      defaultMessage: '指定时间段内 SQL 的最大返回行数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgPartitionCount',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.NumberOfAccessPartitions',
      defaultMessage: '访问分区数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.AverageNumberOfPartitionsAccessed',
      defaultMessage: '指定时间段内 SQL 平均访问的分区数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'failPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ErrorPercentage',
      defaultMessage: '错误占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.ThePercentageOfSqlErrors',
      defaultMessage: '指定时间段内 SQL 的错误占比（%）',
    }),

    unit: '%',
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'PLAN_STAT',
    name: 'retCode4012Count',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TimeoutError',
      defaultMessage: '超时错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalNumberOfTimes',
      defaultMessage: '指定时间段内 SQL 执行发生超时错误（4012）的总次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'PLAN_STAT',
    name: 'retCode4013Count',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MemoryShortageError',
      defaultMessage: '内存不足错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalNumberOfMemory',
      defaultMessage: '指定时间段内 SQL 执行发生内存不足错误（4013）的总次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'PLAN_STAT',
    name: 'retCode5001Count',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.SyntaxError',
      defaultMessage: '语法错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalNumberOfSyntax',
      defaultMessage: '指定时间段内 SQL 执行发生语法解析错误（5001）的总次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'PLAN_STAT',
    name: 'retCode5024Count',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.KeyValueConflictError',
      defaultMessage: '键值冲突错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalNumberOfKey',
      defaultMessage: '指定时间段内 SQL 执行发生键值冲突错误（5024）的总次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'PLAN_STAT',
    name: 'retCode5167Count',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.DataOverrunError',
      defaultMessage: '数据超长错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalNumberOfTimes.1',
      defaultMessage: '指定时间段内 SQL 执行发生数据超长错误（5167）的总次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'PLAN_STAT',
    name: 'retCode5217Count',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.UnknownColumnError',
      defaultMessage: '未知列错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalNumberOfUnknown',
      defaultMessage: '指定时间段内 SQL 执行发生未知列错误（5217）的总次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'INTEGER',
    group: 'PLAN_STAT',
    name: 'retCode6002Count',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TransactionRollbackError',
      defaultMessage: '事务回滚错误',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TotalNumberOfTransactionRollback',
      defaultMessage: '指定时间段内 SQL 执行发生事务回滚错误（6002）的总次数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.WaitingTime',
      defaultMessage: '等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageWaitTimeIn',
      defaultMessage: '指定时间段内 SQL 平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'sumWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TotalWaitTime',
      defaultMessage: '总等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheTotalWaitTimeIn',
      defaultMessage: '指定时间段内 SQL 的总等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgWaitCount',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.WaitingTimes',
      defaultMessage: '等待次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.AverageNumberOfSqlWaits',
      defaultMessage: '指定时间段内 SQL 的平均等待次数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgRpcCount',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.RpcCount',
      defaultMessage: 'RPC 次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageNumberOfRpc',
      defaultMessage: '指定时间段内 SQL 的平均发送 RPC 次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'localPlanPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfLocalPlans',
      defaultMessage: '本地计划占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.PercentageOfLocalSqlPlans',
      defaultMessage: '指定时间段内 SQL 的本地计划占比（%）',
    }),

    unit: '%',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'remotePlanPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.RemotePlanProportion',
      defaultMessage: '远程计划占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.PercentageOfRemoteSqlPlans',
      defaultMessage: '指定时间段内 SQL 的远程计划占比（%）',
    }),

    unit: '%',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'distPlanPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfDistributedPlans',
      defaultMessage: '分布式计划占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.PercentageOfSqlDistributedPlans',
      defaultMessage: '指定时间段内 SQL 的分布式计划占比（%）',
    }),

    unit: '%',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgExecutorRpcCount',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.RpcRequest',
      defaultMessage: 'RPC 请求',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.AverageNumberOfRpcRequests',
      defaultMessage: '指定时间段内 SQL 平均执行 RPC 请求次数',
    }),
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'FAVORITE',
    name: 'missPlanPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MissPlan',
      defaultMessage: '未命中计划',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.ThePlannedMissRateOf',
      defaultMessage: '指定时间段内 SQL 计划未命中率',
    }),

    unit: '%',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgApplicationWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ApplicationWaitingTime',
      defaultMessage: '应用等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageApplicationEventWait',
      defaultMessage: '指定时间段内 Application 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    // SlowSQL 专属指标
    sqlType: 'slowSql',
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'maxApplicationWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.MaximumApplicationWaitingTime',
      defaultMessage: '最大应用等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheMaximumApplicationEventWaiting',
      defaultMessage: '指定时间段内 SQL 的最大 Application 事件等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgConcurrencyWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.ConcurrentWaiting',
      defaultMessage: '并发等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageConcurrencyEventWait',
      defaultMessage: '指定时间段内执行计划的 Concurrency 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    sqlType: 'slowSql',
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'maxConcurrencyWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumConcurrentWaiting',
      defaultMessage: '最大并发等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheMaximumConcurrencyEventWaiting',
      defaultMessage: '指定时间段内 SQL 的最大 Concurrency 事件等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgUserIoWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.IOWaiting',
      defaultMessage: 'IO 等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageWaitTimeIn.1',
      defaultMessage: '指定时间段内 UserIO 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    sqlType: 'slowSql',
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'maxUserIoWaitTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumIOWaiting',
      defaultMessage: '最大 IO 等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheMaximumWaitTimeIn',
      defaultMessage: '指定时间段内 SQL 的最大 UserIO 事件等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: true,
    dataType: 'FLOAT',
    group: 'TIME_STAT',
    name: 'avgScheduleTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.SchedulingWait',
      defaultMessage: '调度等待时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageScheduleEventWait',
      defaultMessage: '指定时间段内 Schedule 事件的平均等待时间（ms）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgRowCacheHit',
    operation: 'SORT',
    title: 'Row Cache Hit',
    tooltip: 'Row Cache Hit',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgBloomFilterCacheHit',
    operation: 'SORT',
    title: 'Bloom filter Cache Hit',
    tooltip: 'Bloom filter Cache Hit',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgBlockCacheHit',
    operation: 'SORT',
    title: 'Block Cache Hit',
    tooltip: 'Block Cache Hit',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgDiskReads',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.PhysicalRead',
      defaultMessage: '物理读',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageNumberOfPhysical',
      defaultMessage: '指定时间段内 SQL 的平均物理读次数',
    }),
  },

  {
    sqlType: 'slowSql',
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'maxDiskReads',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumPhysicalRead',
      defaultMessage: '最大物理读',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MaximumNumberOfPhysicalReads',
      defaultMessage: '指定时间段内 SQL 的最大物理读次数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'retryCount',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.Retries',
      defaultMessage: '重试次数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TotalNumberOfSqlRetries',
      defaultMessage: '指定时间段内 SQL 的总重试次数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'tableScanPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TableScan',
      defaultMessage: '表扫描',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.AverageNumberOfSqlTable',
      defaultMessage: '指定时间段内 SQL 的平均表扫描次数',
    }),

    unit: '%',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'strongConsistencyPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfStronglyConsistentTransactions',
      defaultMessage: '强一致性事务占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.PercentageOfStronglyConsistentSql',
      defaultMessage: '指定时间段内 SQL 强一致性事务占比（%）',
    }),

    unit: 'ms',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'weakConsistencyPercentage',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.ProportionOfTransactionsWithWeak',
      defaultMessage: '弱一致性事务占比',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.ThePercentageOfSqlTransactions',
      defaultMessage: '指定时间段内 SQL 的弱一致性事务占比（%）',
    }),

    unit: '%',
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgMemstoreReadRows',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.MemoryReadRows',
      defaultMessage: '内存读行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageNumberOfMemstore',
      defaultMessage: '指定时间段内 SQL 的平均 Memstore 读行数',
    }),
  },

  {
    allowSearch: false,
    dataType: 'FLOAT',
    group: 'PLAN_STAT',
    name: 'avgSsstoreReadRows',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.NumberOfPhysicalReadRows',
      defaultMessage: '物理读行数',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.slowSqlAttributes.TheAverageNumberOfSsstore',
      defaultMessage: '指定时间段内 SQL 平均读 Ssstore 行数',
    }),
  },
  {
    // 高危 SQL 专属指标
    sqlType: 'highRiskSql',
    displayByDefault: true,
    name: 'executeTime',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.SQLDiagnosis.attributes.sqlTrendsAttributes.ExecutionTime',
      defaultMessage: '执行时间',
    }),

    tooltip: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.TheExecutionTimeOfTheSqlStatementIn',
      defaultMessage: '所选区间内该条 SQL 的执行时间',
    }),
  },
  {
    // 高危 SQL 专属指标
    sqlType: 'highRiskSql',
    allowSearch: false,
    displayAlways: true,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'clientIp',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.RequestIp',
      defaultMessage: '请求 IP',
    }),
    tooltip: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.RequestIp',
      defaultMessage: '请求 IP',
    }),
  },
  {
    // 高危 SQL 专属指标
    sqlType: 'highRiskSql',
    allowSearch: false,
    displayAlways: true,
    dataType: 'STRING',
    group: 'BASIC',
    name: 'highRiskTypes',
    operation: 'SORT',
    title: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.CauseOfDanger',
      defaultMessage: '危险原因',
    }),
    tooltip: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.TheRiskOfThisSqlStatement',
      defaultMessage: '该条 SQL 存在的危险原因',
    }),
  },
];

export const RISK_LEVEL_LIST = [
  {
    value: 'HIGH',
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.HighRisk',
      defaultMessage: '高风险',
    }),

    color: 'error',
  },

  {
    value: 'MEDIUM',
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.MediumRisk',
      defaultMessage: '中风险',
    }),

    color: 'warning',
  },

  {
    value: 'LOW',
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.LowRisk',
      defaultMessage: '低风险',
    }),

    color: 'processing',
  },

  {
    value: 'NORMAL',
    label: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.Normal',
      defaultMessage: '正常',
    }),
    color: 'success',
  },
];

export const performanceMetricGroupList = [
  {
    description: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.WriteAndReadResponseTime',
      defaultMessage: '写读响应时间',
    }),
    key: 'sql_rt',
    name: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.WriteAndReadResponseTime',
      defaultMessage: '写读响应时间',
    }),
    withLabel: false,
    metrics: [
      {
        alarmEnabled: false,
        description: formatMessage({
          id: 'ocp-express.src.constant.sqlDiagnosis.AverageTransactionProcessingTime',
          defaultMessage: '事务平均处理耗时',
        }),
        displayByDefault: true,
        interval: 1,
        isBuiltIn: true,
        key: 'tps_rt',
        name: 'TPS RT',
        unit: formatMessage({
          id: 'ocp-express.src.constant.sqlDiagnosis.S',
          defaultMessage: 'μs',
        }),
      },
      {
        alarmEnabled: false,
        description: formatMessage({
          id: 'ocp-express.src.constant.sqlDiagnosis.AverageProcessingTimeOfSqlStatements',
          defaultMessage: 'SQL 语句平均处理耗时',
        }),
        displayByDefault: true,
        interval: 1,
        isBuiltIn: true,
        key: 'sql_all_rt',
        name: 'QPS RT',
        unit: formatMessage({
          id: 'ocp-express.src.constant.sqlDiagnosis.S',
          defaultMessage: 'μs',
        }),
      },
    ],
  },
  {
    description: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.WriteAndReadRequests',
      defaultMessage: '写读请求量',
    }),
    key: 'sql_count',
    name: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.WriteAndReadRequests',
      defaultMessage: '写读请求量',
    }),
    withLabel: false,
    metrics: [
      {
        alarmEnabled: false,
        description: formatMessage({
          id: 'ocp-express.src.constant.sqlDiagnosis.TransactionsProcessedPerSecond',
          defaultMessage: '每秒处理事务数',
        }),
        displayByDefault: true,
        interval: 1,
        isBuiltIn: true,
        key: 'tps',
        name: 'TPS',
        unit: 'times/s',
      },
      {
        alarmEnabled: false,
        description: formatMessage({
          id: 'ocp-express.src.constant.sqlDiagnosis.SqlStatementsProcessedPerSecond',
          defaultMessage: '每秒处理 SQL 语句数',
        }),
        displayByDefault: true,
        interval: 1,
        isBuiltIn: true,
        key: 'sql_all_count',
        name: 'QPS',
        unit: 'times/s',
      },
    ],
  },
  {
    className: 'performance_and_sql',
    description: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.CpuUsage',
      defaultMessage: 'CPU使用率',
    }),
    key: 'CPU usage rate',
    metrics: [
      {
        alarmEnabled: false,
        description: formatMessage({
          id: 'ocp-express.src.constant.sqlDiagnosis.CpuUsage',
          defaultMessage: 'CPU使用率',
        }),
        displayByDefault: true,
        interval: 1,
        isBuiltIn: true,
        key: 'ob_cpu_percent',
        name: 'cpu_rate',
        unit: '%',
      },
    ],

    name: formatMessage({
      id: 'ocp-express.src.constant.sqlDiagnosis.CpuUsage',
      defaultMessage: 'CPU使用率',
    }),
    withLabel: false,
  },
];

export const highRiskTypeMap = {
  ALTER_TABLE_ADD_OR_DELETE_COLUMN_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.AddOrDeleteColumns',
    defaultMessage: '增加或删除列',
  }),
  DROP_TABLE_OR_DATABASE_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.DeleteATableOrDatabase',
    defaultMessage: '删除表或者数据库',
  }),
  TRUNCATE_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.ClearTheDataTable',
    defaultMessage: '清空数据表',
  }),
  UPDATE_WITHOUT_CONDITION_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.UpdateWithoutConditions',
    defaultMessage: '更新不带条件',
  }),
  UPDATE_CONDITION_ALWAYS_TRUE_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.UpdateConditionConstantToTrue',
    defaultMessage: '更新条件恒为真',
  }),
  DELETE_WITHOUT_CONDITION_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.DeleteWithoutConditions',
    defaultMessage: '删除不带条件',
  }),
  DELETE_CONDITION_ALWAYS_TRUE_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.DeleteConditionConstantToTrue',
    defaultMessage: '删除条件恒为真',
  }),
  RETURN_TOO_MANY_ROWS_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.TheNumberOfReturnedRowsIsTooLarge',
    defaultMessage: '返回行数过大',
  }),
  AFFECT_TOO_MANY_ROWS_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.TheNumberOfAffectedRowsIsTooLarge',
    defaultMessage: '影响行数过大',
  }),
  INVOLVED_TOO_MANY_PARTITION_COUNTS_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.TooManyParticipatingPartitions',
    defaultMessage: '参与分区数过多',
  }),
  SAFE_STMT: formatMessage({
    id: 'ocp-express.src.constant.sqlDiagnosis.Security',
    defaultMessage: '安全',
  }),
};
