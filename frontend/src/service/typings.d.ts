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

declare namespace Alarm {
  export type AppType = 'OB' | 'OCP' | 'OBProxy' | 'Backup' | 'OMS' | 'ODP';

  export type Scope = 'ObCluster' | 'ObTenant' | 'AppCluster' | 'Service' | 'Host';

  export type Level = 1 | 2 | 3 | 4 | 5;

  export type Status = 'Inactive' | 'Active' | 'Silenced' | 'Inhibited';

  interface Labels {
    ob_cluster_group?: string;
    ob_cluster?: string;
    ob_tenant?: string;
    tenant_name?: string;
    host_name?: string;
    host_ip?: string;
    obproxy_cluster?: string;
    obproxy_cluster_id?: string;
    app_cluster?: string;
    service?: string;
    alarm_is_build_in_rule?: 'true' | 'false';
  }

  export type TemplateVariableCategory =
    | 'AlarmRule'
    | 'Message'
    | 'AggregationMessage'
    | 'ChannelTemplate';

  export interface TemplateVariable {
    gmtCreate: string;
    gmtModified: string;
    name: string;
    category: TemplateVariableCategory;
    description1: string;
    description2: string;
  }

  export interface AlarmMetric {
    gmtCreate: string;
    gmtModified: string;
    alarmMetric: string;
    metricExpression: string;
    windowSizeSeconds: number;
    scope: Scope;
    targetLabels: string;
    appType: AppType;
    name1: string;
    name2: string;
    description1: string;
    description2: string;
    unit: string;
    valueTypical: number;
    valueMaxLimit: number;
    valueMinLimit: number;
  }

  /*
   * 告警项
   */

  export interface Rule {
    gmtCreate: string;
    gmtModified: string;
    createdBy: string;
    modifiedBy: string;
    id: number;
    isBuildIn: boolean;
    isEnabled: boolean;
    appType: AppType;
    alarmType: string;
    name1: string;
    name2: string;
    scope: Scope;
    matchConditions: [
      {
        obCluster: string;
        obTenants: string[];
        appCluster: string;
        serviceNames: string[];
        hostIps: string[];
      }
    ];
    level: Level;
    targetLabels: string[];
    summaryTemplate1: string;
    descriptionTemplate1: string;
    summaryTemplate2: string;
    descriptionTemplate2: string;
    expression: string;
    durationSeconds: number;
    evaluationIntervalSeconds: number;
    resolveTimeoutSeconds: number;
    alarmGroupNames: string[];
  }

  /*
   * 告警通道
   */

  export type RecipientAddrSource = 'uid' | 'email' | 'mobile' | 'employe_id';

  export type ChannelType = 'HTTP' | 'Script';

  export type HTTPMethod = 'POST' | 'GET' | 'PUT';

  export interface Channel {
    gmtCreate: string;
    gmtModified: string;
    createdBy: string;
    modifiedBy: string;
    id: number;
    isDefault: boolean;
    name: string;
    lastSentAt: string;
    channelType: ChannelType;
    recipientAddrSource: RecipientAddrSource;
    isGroupChannel: boolean;
    messageTemplate1: string;
    messageTemplate2: string;
    isAggregationEnabled: boolean;
    aggregationMessageTemplate1: string;
    aggregationMessageTemplate2: string;
    aggregationRule: {
      aggregateWaitSeconds: number;
      aggregateIntervalSeconds: number;
      repeatIntervalSeconds: number;
    };
    channelSettings: {
      httpMethod: HTTPMethod;
      httpUrlTemplate: string;
      httpHeadersTemplate: string;
      httpProxy: string;
      httpBodyTemplate: string;
      scriptCommandTemplate: string;
      scriptContentEnabled: boolean;
      scriptContent: string;
    };
  }

  interface AlarmListData {
    contents?: API.OcpAlarmRule[];
    page: { totalElements?: number };
  }
}

declare namespace SQLDiagnosis {
  export type SqlType =
    | 'suspiciousSql'
    | 'topSql'
    | 'slowSql'
    | 'parallelSql'
    | 'newSql'
    | 'highRiskSql';

  export type SqlAuditStatDetailAttributeGroup = 'FAVORITE' | 'BASIC' | 'TIME_STAT' | 'PLAN_STAT';
  export type AttributeDataType = 'BOOLEAN' | 'INTEGER' | 'FLOAT' | 'STRING';
  export type AttributeOperation = 'NONE' | 'SORT' | 'FILTER';
  export type FilterExpressionList = {
    searchAttr?: string;
    searchOp?: string;
    searchVal?: string;
    value?: string;
    label?: string;
  }[];

  export type QueryValues = {
    tab: SqlType;
    tenantId: string;
    startTime?: string;
    endTime?: string;
    rangeKey?: string;
    serverId?: string;
    inner?: string;
    sqlText?: string;
    filterExpression?: string;
    filterExpressionList?: SQLDiagnosis.FilterExpressionList;
    customColumns?: string[];
    customColumnName?: string;
    fields?: SQLDiagnosis.SqlAuditStatDetailAttribute[];
    page?: string;
    size?: string;
    collapsed?: boolean;
    filters: Record<string, string[]>;
    sorter: {
      order?: 'ascend' | 'descend';
      field?: string;
      highlight?: boolean;
    };
  };

  export interface SqlAuditStatSampleAttribute {
    /** 指标的顺序 */
    ordinal?: number;
    /** 指标的名称(英文；与数据查询接口返回数据的属性名一致） */
    name?: string;
    /** 指标的展示标题（按指定语言的） */
    title?: string;
    /** 指标的说明信息（按指定语言的） */
    tooltip?: string;
    /** 指标的度量单位 */
    unit?: string;
    /** 是否默认展示 */
    displayByDefault?: boolean;
    /** 数据类型 */
    dataType?: AttributeDataType;
  }

  export interface SqlAuditStatDetailAttribute {
    sqlType?: SqlType;
    /** 指标的名称(英文；与数据查询接口返回数据的属性名一致） */
    name?: string;
    /** 指标的展示标题（按指定语言的） */
    title?: string;
    /** 列表是否展示指标的说明信息（按指定语言的） */
    showTooltip?: string;
    /** 指标的说明信息（按指定语言的） */
    tooltip?: string;
    /** 指标的度量单位 */
    unit?: string;
    /** 指标的分组 */
    group?: SqlAuditStatDetailAttributeGroup;
    /** 指标支持的列操作 */
    operation?: AttributeOperation;
    /** 是否默认展示 */
    displayByDefault?: boolean;
    /** 是否总是展示（不可隐藏） */
    displayAlways?: boolean;
    /** 是否允许用于高级搜索 */
    allowSearch?: boolean;
    /** 数据类型 */
    dataType?: AttributeDataType;
  }

  export interface SqlAuditStatSample {
    /** 期间内的总执行次数 */
    executions?: number;
    /** 期间内的平均每秒执行次数 */
    execPs?: number;
    /** 期间内平均更新行数 */
    avgAffectedRows?: number;
    /** 期间内平均返回行数 */
    avgReturnRows?: number;
    /** 期间内平均访问分区数 */
    avgPartitionCount?: number;
    /** 期间内的总错误次数 */
    failCount?: number;
    /** 期间内的错误百分比 */
    failPercentage?: number;
    /** 期间内结果码4012的发生次数 */
    retCode4012Count?: number;
    /** 期间内结果码4013的发生次数 */
    retCode4013Count?: number;
    /** 期间内结果码5001的发生次数 */
    retCode5001Count?: number;
    /** 期间内结果码5024的发生次数 */
    retCode5024Count?: number;
    /** 期间内结果码5167的发生次数 */
    retCode5167Count?: number;
    /** 期间内结果码5217的发生次数 */
    retCode5217Count?: number;
    /** 期间内结果码6002的发生次数 */
    retCode6002Count?: number;
    /** 期间内的平均等待时间（毫秒） */
    avgWaitTime?: number;
    /** 期间内的平均等待次数 */
    avgWaitCount?: number;
    /** 期间内的平均发送RPC次数 */
    avgRpcCount?: number;
    /** 期间内的本地计划百分比 */
    localPlanPercentage?: number;
    /** 期间内的远程计划百分比 */
    remotePlanPercentage?: number;
    /** 期间内的分布式计划百分比 */
    distPlanPercentage?: number;
    /** 期间内的平均响应时间（毫秒） */
    avgElapsedTime?: number;
    /** 期间内的最大响应时间（毫秒） */
    maxElapsedTime?: number;
    /** 期间内的总响应时间（毫秒） */
    sumElapsedTime?: number;
    /** 期间内的平均CPU时间（毫秒） */
    avgCpuTime?: number;
    /** 期间内的最大CPU时间（毫秒） */
    maxCpuTime?: number;
    /** 期间内的平均网络传输时间（毫秒） */
    avgNetTime?: number;
    /** 期间内的平均网络入队时间（毫秒） */
    avgNetWaitTime?: number;
    /** 期间内的平均排队时间（毫秒） */
    avgQueueTime?: number;
    /** 期间内的平均语法解析时间（毫秒） */
    avgDecodeTime?: number;
    /** 期间内的平均计划生成时间（毫秒） */
    avgGetPlanTime?: number;
    /** 期间内的平均计划执行时间（毫秒） */
    avgExecuteTime?: number;
    /** 期间内的平均执行RPC请求次数 */
    avgExecutorRpcCount?: number;
    /** 期间内的计划命中率 */
    missPlanPercentage?: number;
    /** 期间内的平均Application事件等待时间（毫秒） */
    avgApplicationWaitTime?: number;
    /** 期间内的平均Concurrency事件等待时间（毫秒） */
    avgConcurrencyWaitTime?: number;
    /** 期间内的平均UserIO事件等待时间（毫秒） */
    avgUserIoWaitTime?: number;
    /** 期间内的平均Schedule事件等待时间（毫秒） */
    avgScheduleTime?: number;
    /** 期间内的总计RowCache命中次数 */
    avgRowCacheHit?: number;
    /** 期间内的平均命中次数 */
    avgBloomFilterCacheHit?: number;
    /** 期间内的平均命中次数 */
    avgBlockCacheHit?: number;
    /** 期间内的总计命中次数 */
    avgBlockIndexCacheHit?: number;
    /** 期间内的总计磁盘度次数 */
    avgDiskReads?: number;
    /** 期间内的总计重试次数 */
    retryCount?: number;
    /** 期间内的平均表扫描次数 */
    tableScanPercentage?: number;
    /** 期间内的强一致性事务百分比 */
    strongConsistencyPercentage?: number;
    /** 期间内的弱一致性事务百分比 */
    weakConsistencyPercentage?: number;
    /** 期间内的平均Memstore读行数 */
    avgMemstoreReadRows?: number;
    /** 期间内的平均Sstore读行数 */
    avgSsstoreReadRows?: number;
    /** 采样时间点 */
    timestamp?: string;
  }
}
