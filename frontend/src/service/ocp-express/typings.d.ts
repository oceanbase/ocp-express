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

/* eslint-disable */
// 该文件由 OneAPI 自动生成，请勿手动修改！

declare namespace API {
  interface AddReplicaParam {
    zoneName: string;
    replicaType: ReplicaType;
    resourcePool: AddReplicaParamPoolParam;
  }

  interface AddReplicaParamPoolParam {
    unitSpec: UnitSpecParam;
    unitCount: number;
  }

  type AgentState = 'UNKNOWN' | 'STARTING' | 'RUNNING' | 'STOPPING' | 'STOPPED';

  interface AuthenticatedUser {
    id?: number;
    username?: string;
    password?: string;
    accountExpired?: boolean;
    accountLocked?: boolean;
    credentialsExpired?: boolean;
    enabled?: boolean;
    email?: string;
    mobile?: string;
    department?: string;
    description?: string;
    origin?: string;
    createTime?: string;
    updateTime?: string;
    needChangePassword?: boolean;
    lockExpiredTime?: string;
  }

  interface BaseResponse {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
  }

  interface BasicCluster {
    clusterName?: string;
    obClusterId?: number;
    status?: ObClusterStatus;
    obVersion?: string;
    communityEdition?: boolean;
    obZones?: Array<ObZone>;
    obServers?: Array<ObServer>;
    rootServices?: Array<RootService>;
    serverArchs?: Record<string, any>;
    serverDataPaths?: Record<string, any>;
    serverLogPaths?: Record<string, any>;
  }

  interface BasicCreatorInfo {
    id?: number;
    name?: string;
  }

  interface BasicSubtaskInstance {
    id?: number;
    name?: string;
    description?: string;
    status?: SubtaskState;
    operation?: SubtaskOperation;
  }

  interface BasicTaskInstance {
    id?: number;
    name?: string;
    creator?: BasicCreatorInfo;
    tenantInfo?: BasicTenantInfo;
    status?: TaskState;
    startTime?: string;
    finishTime?: string;
    subtasks?: Array<BasicSubtaskInstance>;
  }

  interface BasicTenantInfo {
    obTenantId?: number;
    name?: string;
  }

  interface BatchConcurrentLimitRequest {
    /** SQL 的查询开始时间 */
    startTime: string;
    /** 可疑SQL查询的开始时间 */
    endTime: string;
    /** 要限流的SQL列表, 格式为：[{数据库名、sqlId}] */
    sqlList?: Array<BatchConcurrentLimitRequestSql>;
    /** 限流并发度 用于SQL限流 */
    concurrentNum?: number;
  }

  interface BatchConcurrentLimitRequestSql {
    /** 数据库名 */
    dbName?: string;
    /** sqlId */
    sqlId?: string;
  }

  interface BatchConcurrentLimitResult {
    /** 批量限流是否成功 */
    result?: boolean;
    /** 批量限流失败的SQL Sql -> exceptionMessage */
    failedSql?: Record<string, any>;
  }

  interface BatchDropOutlineRequest {
    /** outline 列表 */
    outlineList?: Array<BatchDropOutlineRequestOutline>;
  }

  interface BatchDropOutlineRequestOutline {
    /** 数据库名 */
    dbName?: string;
    /** sqlId */
    sqlId?: string;
    /** outline 名 */
    outlineName: string;
  }

  interface BatchDropOutlineResult {
    /** 批量drop outline 是否成功 */
    result?: boolean;
    /** 批量限流失败的SQL Outline -> exceptionMessage */
    failedSql?: Record<string, any>;
  }

  interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
  }

  interface Charset {
    name?: string;
    description?: string;
    maxLen?: number;
    collations?: Array<Collation>;
  }

  interface CheckTenantPasswordResult {
    successful?: boolean;
    failedReason?: string;
  }

  interface CloseSessionParam {
    sessionIds?: Array<number>;
  }

  interface ClusterInfo {
    clusterName?: string;
    obClusterId?: number;
    status?: ObClusterStatus;
    obVersion?: string;
    performanceStats?: PerformanceStats;
    communityEdition?: boolean;
    zones?: Array<Zone>;
    tenants?: Array<TenantInfo>;
  }

  interface ClusterInitParam {
    clusterName?: string;
    obClusterId?: number;
    rootSysPassword?: string;
    serverList?: Array<ClusterInitParamObServerAddr>;
  }

  interface ClusterInitParamObServerAddr {
    address?: string;
    sqlPort?: number;
    /** 此字段可能不准确，或者获取不到，暂时先保留. */
    withRootServer?: boolean;
  }

  interface ClusterParameter {
    name?: string;
    parameterType?: ClusterParameterType;
    currentValue?: ClusterParameterValue;
    section?: string;
    description?: string;
    needRestart?: boolean;
    readonly?: boolean;
  }

  type ClusterParameterType = 'CLUSTER' | 'OB_CLUSTER_PARAMETER' | 'TENANT' | 'OB_TENANT_PARAMETER';

  interface ClusterParameterValue {
    /** 参数所有的取值 */
    values?: Array<string>;
    /** 集群中所有的取值是否都一致 */
    singleValueInCluster?: boolean;
    /** 每个 OBServer 上的取值 */
    serverValues?: Array<ServerParameterValue>;
    /** 每个租户上的取值 */
    tenantValues?: Array<ClusterParameterValueTenantParameterValue>;
    /** 原始取值列表 */
    obParameters?: Array<ObParameter>;
  }

  interface ClusterParameterValueTenantParameterValue {
    tenantId?: number;
    tenantName?: string;
    value?: string;
  }

  interface ClusterPropertiesServerAddressInfo {
    address?: string;
    svrPort?: number;
    sqlPort?: number;
    withRootServer?: boolean;
    agentMgrPort?: number;
    agentMonPort?: number;
  }

  interface ClusterUnitSpecLimit {
    /** CPU 下限，单位：核 */
    cpuLowerLimit?: number;
    /** 内存下限，单位：GB */
    memoryLowerLimit?: number;
    clusterName?: string;
    obClusterId?: number;
  }

  interface ClusterUnitView {
    clusterId?: number;
    regionInfos: Array<ClusterUnitViewOfRegion>;
    tenantInfos: Array<ClusterUnitViewOfTenant>;
    deletableUnitCount: number;
    unusedUnitMaxReserveHour: number;
  }

  interface ClusterUnitViewOfRegion {
    obRegionName?: string;
    zoneInfos?: Array<ClusterUnitViewOfZone>;
  }

  interface ClusterUnitViewOfServer {
    serverIp?: string;
    serverPort?: number;
    hostType?: string;
    zone?: string;
    region?: string;
    totalMemorySizeByte?: number;
    memorySizeAssignedByte?: number;
    memoryAssignedPercent?: number;
    totalCpuCount?: number;
    cpuCountAssigned?: number;
    cpuAssignedPercent?: number;
    totalDiskSizeByte?: number;
    diskUsedByte?: number;
    diskUsedPercent?: number;
    unitCount?: number;
    unusedUnitCount?: number;
    unitInfos?: Array<ClusterUnitViewOfUnit>;
  }

  interface ClusterUnitViewOfTenant {
    obTenantId?: number;
    tenantId?: number;
    tenantName?: string;
    primaryZone?: string;
    locality?: string;
    tenantGroup?: string;
    status?: TenantStatus;
  }

  interface ClusterUnitViewOfUnit {
    obUnitId?: number;
    obTenantId?: number;
    tenantName?: string;
    serverIp?: string;
    serverPort?: number;
    zone?: string;
    region?: string;
    unitConfig?: string;
    /** UNIT规格大小的简写，模式为： 3C12G ~ 4c64G; 取min和max拼接而成； */
    unitConfigAliasName?: string;
    resourcePoolName?: string;
    maxCpuAssignedCount?: number;
    minCpuAssignedCount?: number;
    maxMemoryAssignedByte?: number;
    minMemoryAssignedByte?: number;
    diskUsedByte?: number;
    migrateSvrIp?: string;
    /** migrateType 和 migrateAddr对应关系为： migrateType = MIGRATE_IN, migrateAddr为目标端地址；
migrateType = MIGRATE_OUT, migrateAddr为源端地址 migrateType =ROLLBACK_MIGRATE_IN,
migrateAddr为回滚目标端地址 migrateType = ROLLBACK_MIGRATE_OUT, migrateAddr为回滚源端地址
migrateType = NOT_IN_MIGRATE */
    migrateType?: MigrateType;
    timestamp?: number;
    manualMigrate?: boolean;
  }

  interface ClusterUnitViewOfZone {
    obZoneName?: string;
    memorySizeAssignedByte?: number;
    cpuCountAssigned?: number;
    diskSizeUsedByte?: number;
    unitCount?: number;
    serverInfos?: Array<ClusterUnitViewOfServer>;
  }

  interface Collation {
    name?: string;
    isDefault?: boolean;
  }

  interface CompactionDuration {
    /** 合并结果, 成功、失败、合并中 */
    result?: TenantCompactionResult;
    /** 合并耗时，单位:s */
    costTime?: number;
    /** 合并开始时间 */
    startTime?: string;
    /** 合并结束时间，正在合并或者合并失败，这个值是空 */
    endTime?: string;
  }

  type CompatibleType = 'ALL' | 'ORACLE' | 'MYSQL';

  type ConnectionStringType = 'OBPROXY' | 'DIRECT';

  interface Context {
    parallelIdx?: number;
    stringMap?: Record<string, any>;
    listMap?: Record<string, any>;
  }

  interface CreateDatabaseParam {
    dbName?: string;
    collation?: string;
    primaryZone?: string;
    readonly?: boolean;
  }

  interface CreateDbRoleParam {
    roleName?: string;
    globalPrivileges?: Array<string>;
    roles?: Array<string>;
  }

  interface CreateDbUserParam {
    username?: string;
    password?: string;
    globalPrivileges?: Array<string>;
    dbPrivileges?: Array<DbPrivilegeParam>;
    roles?: Array<string>;
  }

  interface CreatePasswordInVaultParam {
    tenantName?: string;
    newPassword?: string;
  }

  interface CreateTenantParam {
    name?: string;
    mode?: TenantMode;
    primaryZone?: string;
    charset?: string;
    collation?: string;
    description?: string;
    whitelist?: string;
    timeZone?: string;
    rootPassword?: string;
    zones?: Array<CreateTenantParamZoneParam>;
    parameters?: Array<TenantParameterParam>;
  }

  interface CreateTenantParamPoolParam {
    unitSpec: UnitSpecParam;
    unitCount: number;
  }

  interface CreateTenantParamZoneParam {
    name: string;
    replicaType: ReplicaType;
    resourcePool: CreateTenantParamPoolParam;
  }

  interface CustomColumn {
    /** 自定义列的表达式 */
    expression?: string;
    /** 根据表达式计算的值 */
    value?: Record<string, any>;
  }

  interface CustomPage {
    /** 总记录数 */
    totalElements?: number;
    /** 总页数 */
    totalPages?: number;
    /** 当前页码 */
    number?: number;
    /** 当前页包含的记录条数 */
    size?: number;
  }

  type DataSource = Record<string, any>;

  interface Database {
    dbName?: string;
    charset?: string;
    collation?: string;
    primaryZone?: string;
    readonly?: boolean;
    createTime?: string;
    connectionUrls?: Array<ObproxyAndConnectionString>;
    requiredSize?: number;
    id?: number;
  }

  interface DbObject {
    objectType?: ObjectType;
    objectName?: string;
    schemaName?: string;
  }

  type DbPrivType =
    | 'ALTER'
    | 'CREATE'
    | 'DELETE'
    | 'DROP'
    | 'INSERT'
    | 'SELECT'
    | 'UPDATE'
    | 'INDEX'
    | 'CREATE VIEW'
    | 'CREATE_VIEW'
    | 'SHOW VIEW'
    | 'SHOW_VIEW';

  interface DbPrivilege {
    dbName?: string;
    privileges?: Array<DbPrivType>;
  }

  interface DbPrivilegeParam {
    dbName?: string;
    privileges?: Array<DbPrivType>;
  }

  interface DbRole {
    name?: string;
    createTime?: string;
    updateTime?: string;
    globalPrivileges?: Array<GlobalPrivilege>;
    objectPrivileges?: Array<ObjectPrivilege>;
    /** What roles are granted to this role. */
    grantedRoles?: Array<string>;
    /** What users are this role granted to. */
    userGrantees?: Array<string>;
    /** What roles are this role granted to. */
    roleGrantees?: Array<string>;
  }

  interface DbUser {
    username?: string;
    globalPrivileges?: Array<GlobalPrivilege>;
    dbPrivileges?: Array<DbPrivilege>;
    objectPrivileges?: Array<ObjectPrivilege>;
    grantedRoles?: Array<string>;
    isLocked?: boolean;
    createTime?: string;
    connectionStrings?: Array<ObproxyAndConnectionString>;
    accessibleDatabases?: Array<string>;
  }

  interface DeleteReplicaParam {
    zoneName: string;
  }

  interface DownloadLogRequest {
    ip?: string;
    port?: number;
    logType?: Array<string>;
    startTime?: string;
    endTime?: string;
    keyword?: Array<string>;
    keywordType?: string;
    excludeKeyword?: Array<string>;
    excludeKeywordType?: string;
    logLevel?: Array<LogLevel>;
  }

  type GlobalPrivilege = Record<string, any>;

  interface GrantDbPrivilegeParam {
    dbPrivileges?: Array<DbPrivilegeParam>;
  }

  interface GrantGlobalPrivilegeParam {
    globalPrivileges?: Array<string>;
  }

  interface GrantObjectPrivilegeParam {
    objectPrivileges?: Array<ObjectPrivilege>;
  }

  interface GrantRoleParam {
    roles?: Array<string>;
  }

  interface HostInfo {
    hostname?: string;
    os?: string;
    clockDiffMillis?: number;
    totalMemory?: number;
    architecture?: string;
    cpuCount?: number;
  }

  type HttpServletRequest = Record<string, any>;

  type IndexStatus = 'VALID' | 'CHECKING' | 'INELEGIBLE' | 'ERROR' | 'UNUSABLE' | 'UNKNOWN';

  type IndexType =
    | 'PRIMARY'
    | 'NORMAL_LOCAL'
    | 'NORMAL_GLOBAL'
    | 'UNIQUE_LOCAL'
    | 'UNIQUE_GLOBAL'
    | 'OTHER';

  interface InitProperties {
    cluster?: InitPropertiesClusterProperties;
    agentUsername?: string;
    agentPassword?: string;
  }

  interface InitPropertiesClusterProperties {
    name?: string;
    obClusterId?: number;
    rootSysPassword?: string;
    serverAddresses?: Array<InitPropertiesClusterPropertiesServerAddressInfo>;
  }

  interface InitPropertiesClusterPropertiesServerAddressInfo {
    address?: string;
    svrPort?: number;
    sqlPort?: number;
    withRootServer?: boolean;
    agentMgrPort?: number;
    agentMonPort?: number;
  }

  interface IterableResponse {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_SqlAuditStatSummary_;
  }

  interface IterableResponseIterableData {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<Record<string, any>>;
  }

  interface IterableResponseIterableData_Charset_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<Charset>;
  }

  interface IterableResponseIterableData_ClusterParameter_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<ClusterParameter>;
  }

  interface IterableResponseIterableData_Database_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<Database>;
  }

  interface IterableResponseIterableData_DbObject_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<DbObject>;
  }

  interface IterableResponseIterableData_DbRole_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<DbRole>;
  }

  interface IterableResponseIterableData_DbUser_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<DbUser>;
  }

  interface IterableResponseIterableData_Map_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<Record<string, any>>;
  }

  interface IterableResponseIterableData_Map_Object_Object__ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<Record<string, any>>;
  }

  interface IterableResponseIterableData_OcpPrometheusQueryResult_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<OcpPrometheusQueryResult>;
  }

  interface IterableResponseIterableData_Outline_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<Outline>;
  }

  interface IterableResponseIterableData_PlanStatGroup_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<PlanStatGroup>;
  }

  interface IterableResponseIterableData_SeriesMetricValues_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<SeriesMetricValues>;
  }

  interface IterableResponseIterableData_SlowSqlRankInfo_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<SlowSqlRankInfo>;
  }

  interface IterableResponseIterableData_SqlAuditStatSummary_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<SqlAuditStatSummary>;
  }

  interface IterableResponseIterableData_String_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<string>;
  }

  interface IterableResponseIterableData_TenantCompactionHistory_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<TenantCompactionHistory>;
  }

  interface IterableResponseIterableData_TenantParameterInfo_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<TenantParameterInfo>;
  }

  interface IterableResponseIterableData_TenantParameter_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<TenantParameter>;
  }

  interface IterableResponseIterableData_string_ {
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<string>;
  }

  interface IterableResponse_Charset_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_Charset_;
  }

  interface IterableResponse_ClusterParameter_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_ClusterParameter_;
  }

  interface IterableResponse_Database_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_Database_;
  }

  interface IterableResponse_DbObject_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_DbObject_;
  }

  interface IterableResponse_DbRole_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_DbRole_;
  }

  interface IterableResponse_DbUser_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_DbUser_;
  }

  interface IterableResponse_Map_Object_Object__ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_Map_;
  }

  interface IterableResponse_OcpPrometheusQueryResult_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_OcpPrometheusQueryResult_;
  }

  interface IterableResponse_Outline_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_Outline_;
  }

  interface IterableResponse_PlanStatGroup_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_PlanStatGroup_;
  }

  interface IterableResponse_SeriesMetricValues_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_SeriesMetricValues_;
  }

  interface IterableResponse_SlowSqlRankInfo_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_SlowSqlRankInfo_;
  }

  interface IterableResponse_SqlAuditStatSummary_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_SqlAuditStatSummary_;
  }

  interface IterableResponse_TenantCompactionHistory_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_TenantCompactionHistory_;
  }

  interface IterableResponse_TenantParameterInfo_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_TenantParameterInfo_;
  }

  interface IterableResponse_TenantParameter_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_TenantParameter_;
  }

  interface IterableResponse_string_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: IterableResponseIterableData_String_;
  }

  interface LogEntry {
    /** 日志时间 */
    logAt?: string;
    /** 日志类型 */
    logType?: string;
    /** 日志行 */
    logLine?: string;
    /** 日志级别 */
    logLevel?: string;
  }

  type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL' | 'EDIAG' | 'WDIAG';

  interface LoginKey {
    publicKey?: string;
  }

  type Map_String_Object_ = Record<string, any>;

  interface Metric {
    name?: string;
    labels?: MetricLabels;
    seriesKey?: string;
    hashCode?: number;
  }

  interface MetricClass {
    id?: number;
    key?: string;
    name?: string;
    name_en?: string;
    description?: string;
    description_en?: string;
    scope?: MetricScope;
    type?: MetricType;
    metricGroups?: Array<MetricGroup>;
  }

  interface MetricGroup {
    id?: number;
    key?: string;
    className?: string;
    name?: string;
    description?: string;
    metrics?: Array<MetricMeta>;
    /** 是否为带标签的监控数据群组，方便调用方区数据类型来决定使用哪个 API 获取监控数据详情数据，默认为 false. */
    withLabel?: boolean;
  }

  type MetricLabels = Record<string, any>;

  interface MetricMeta {
    id?: number;
    key?: string;
    name?: string;
    description?: string;
    unit?: string;
    displayByDefault?: boolean;
    interval?: number;
    isBuiltIn?: boolean;
    minObVersion?: string;
    maxObVersion?: string;
  }

  interface MetricQueryDebugServiceDebugQueryParam {
    /** 根据监控的 metric 来查询监控表达式. */
    queryMetricExpression?: Array<string>;
    /** 根据监控表达式中的 metric、label 信息查询 seriesId. */
    querySeriesIds?: MetricQueryDebugServiceSeriesIdByMetric;
    /** 根据 seriesId 查询监控的原始值. */
    queryMetricBySeriesId?: MetricQueryDebugServiceMetricBySeriesId;
  }

  interface MetricQueryDebugServiceMetricBySeriesId {
    seriesId?: Array<number>;
    step?: number;
    startTime?: number;
    endTime?: number;
  }

  interface MetricQueryDebugServiceSeriesIdByMetric {
    metrics?: string;
    labels?: string;
  }

  type MetricScope = 'CLUSTER' | 'TENANT' | 'HOST';

  type MetricType = 'NORMAL' | 'TOP';

  type MigrateType =
    | 'MIGRATE_IN'
    | 'MIGRATE_OUT'
    | 'ROLLBACK_MIGRATE_IN'
    | 'ROLLBACK_MIGRATE_OUT'
    | 'NOT_IN_MIGRATE';

  type ModelAndView = Record<string, any>;

  interface ModifyDatabaseParam {
    collation?: string;
    primaryZone?: string;
    /** 如果为 true，表示将 primary zone 设置为与租户一致 */
    primaryZoneToDefault?: boolean;
    readonly?: boolean;
  }

  interface ModifyDbPrivilegeParam {
    dbPrivileges: Array<DbPrivilegeParam>;
  }

  interface ModifyDbUserPasswordParam {
    newPassword?: string;
  }

  interface ModifyGlobalPrivilegeParam {
    globalPrivileges: Array<string>;
  }

  interface ModifyObjectPrivilegeParam {
    objectPrivileges: Array<ObjectPrivilege>;
  }

  interface ModifyPrimaryZoneParam {
    primaryZone: string;
  }

  interface ModifyReplicaParam {
    zoneName: string;
    /** 副本类型。不允许批量修改，批量修改副本时，此参数无效。 */
    replicaType?: ReplicaType;
    /** 资源池信息。null 表示不修改。 */
    resourcePool?: ModifyReplicaParamPoolParam;
  }

  interface ModifyReplicaParamPoolParam {
    /** unit 规格。null 表示不修改。 */
    unitSpec?: UnitSpecParam;
    /** unit 数量。null 表示不修改。对于 OB 4.0 以上版本，如果修改 unit 数量，必须所有 zone 上保持相同的 unit 数量。 */
    unitCount?: number;
  }

  interface ModifyRoleParam {
    roles: Array<string>;
  }

  interface ModifyTenantDescriptionParam {
    description: string;
  }

  interface ModifyWhitelistParam {
    whitelist: string;
  }

  interface NoDataResponse {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: string;
  }

  type NodeType = 'JAVA_TASK';

  interface ObAgentDetail {
    id?: number;
    createTime?: string;
    updateTime?: string;
    ip?: string;
    mgrPort?: number;
    monPort?: number;
    obServerSvrPort?: number;
    operation?: ObAgentOperation;
    obAgentProcesses?: Array<ObAgentProcess>;
    ready?: boolean;
    version?: string;
  }

  type ObAgentOperation = 'EXECUTE' | 'RESTARTING';

  interface ObAgentProcess {
    /** 进程名称. */
    name?: string;
    /** 状态 */
    state?: AgentState;
    /** 版本 */
    version?: string;
    /** pid */
    pid?: number;
    /** 开始运行时间 */
    startAtMillis?: number;
    /** 进程端口号. */
    port?: number;
  }

  type ObClusterStatus =
    | 'RUNNING'
    | 'UNAVAILABLE'
    | 'STOPPED'
    | 'CREATING'
    | 'TAKINGOVER'
    | 'DELETING'
    | 'MOVINGOUT'
    | 'RESTARTING'
    | 'STARTING'
    | 'STOPPING'
    | 'SWITCHOVER'
    | 'FAILOVER'
    | 'UPGRADING'
    | 'OPERATING'
    | 'ABANDONED'
    | 'DISCONNECTING'
    | 'STARTING_IN_READ_ONLY';

  interface ObParameter {
    svrIp?: string;
    svrPort?: number;
    zone?: string;
    name?: string;
    value?: string;
    dataType?: string;
    section?: string;
    editLevel?: string;
    info?: string;
  }

  interface ObServer {
    svrIp?: string;
    svrPort?: number;
    /** sql_port in OCP */
    innerPort?: number;
    withRootserver?: boolean;
    status?: string;
    zone?: string;
    buildVersion?: string;
    stopTime?: number;
    startServiceTime?: number;
    blockMigrateInTime?: number;
    id?: number;
  }

  interface ObServerAddr {
    address?: string;
    sqlPort?: number;
    /** 此字段可能不准确，或者获取不到，暂时先保留. */
    withRootServer?: boolean;
  }

  type ObServerInnerStatus = 'ACTIVE' | 'INACTIVE' | 'DELETING' | 'TAKEOVER_BY_RS';

  type ObServerStatus =
    | 'RUNNING'
    | 'UNAVAILABLE'
    | 'SERVICE_STOPPED'
    | 'PROCESS_STOPPED'
    | 'DELETED'
    | 'CREATING'
    | 'SERVICE_STOPPING'
    | 'PROCESS_STOPPING'
    | 'STARTING'
    | 'RESTARTING'
    | 'DELETING';

  type ObTenantCompactionStatus = 'IDLE' | 'COMPACTING' | 'VERIFYING';

  interface ObZone {
    /** zone name, value of __all_zone.zone */
    zone?: string;
    /** region */
    region?: string;
    /** idc */
    idc?: string;
    /** zone status, optional values: ACTIVE/INACTIVE */
    status?: string;
    /** merge status, optional values: IDLE/MERGING/INDEX */
    mergeStatus?: string;
    /** 广播版本，如果正在合并，表示合并中的版本 */
    broadcastVersion?: number;
    /** 全部已合并的版本

<pre>
和 last_merged_version 的差别在于：
当 merger_completion_percentage 设置成 < 100 时:
- 副本合并完成百分比 > merger_completion_percentage 就算这个zone合并完成了, last_merged_version 就会提升
- all_merged_version 要全部副本完成合并才会提升
merger_completion_percentage 一般不会设置，异常情况下才会调整，当 merger_completion_percentage = 100 时,
all_merged_version 的值和 last_merged_version 是一致的
</pre> */
    allMergedVersion?: number;
    /** 已合并的版本 */
    lastMergedVersion?: number;
    /** merge start time in microseconds */
    mergeStartTime?: number;
    /** last merge time in microseconds */
    lastMergedTime?: number;
    /** is merge timeout, 1:true ; 0: false */
    mergeTimeout?: boolean;
  }

  type ObZoneInnerStatus = 'ACTIVE' | 'INACTIVE';

  type ObZoneStatus =
    | 'RUNNING'
    | 'UNAVAILABLE'
    | 'SERVICE_STOPPED'
    | 'STOPPED'
    | 'DELETED'
    | 'CREATING'
    | 'STOPPING'
    | 'STARTING'
    | 'RESTARTING'
    | 'DELETING'
    | 'OPERATING';

  interface ObjectPrivilege {
    object?: DbObject;
    privileges?: Array<ObjectPrivilegeType>;
  }

  type ObjectPrivilegeType =
    | 'SELECT'
    | 'UPDATE'
    | 'INSERT'
    | 'DELETE'
    | 'ALTER'
    | 'INDEX'
    | 'REFERENCES'
    | 'EXECUTE';

  type ObjectType = 'TABLE' | 'VIEW' | 'PROCEDURE' | 'STORED_PROCEDURE';

  interface ObproxyAndConnectionString {
    /** 连接串类型，OBPROXY | 直连 */
    type?: ConnectionStringType;
    /** OBProxy 地址，仅当 type 为 OBPROXY 时有效 */
    obProxyAddress?: string;
    /** OBProxy 端口，仅当 type 为 OBPROXY 时有效 */
    obProxyPort?: number;
    /** 连接串 */
    connectionString?: string;
  }

  interface OcpPrometheusData {
    /** epoch second */
    timestamp?: number;
    value?: number;
    format?: string;
  }

  type OcpPrometheusDataContainer = Record<string, any>;

  interface OcpPrometheusMeasurement {
    metric?: string;
    labelStr?: string;
    labels?: Record<string, any>;
  }

  interface OcpPrometheusQueryResult {
    isScalar?: boolean;
    value?: number;
    measurement?: OcpPrometheusMeasurement;
    interval?: number;
    data?: Array<OcpPrometheusData>;
    scanDataIter?: OcpPrometheusDataContainer;
  }

  type OneApiResult_object_ = Record<string, any>;

  type OneApiResult_offsetdatetime_ = Record<string, any>;

  type OneApiResult_string_ = Record<string, any>;

  interface Outline {
    /** outline Id */
    outlineId?: number;
    /** 租户名 */
    tenantName?: string;
    /** OB租户ID */
    obTenantId?: number;
    /** 数据库 Id */
    obDbId?: number;
    /** 数据库 Name */
    dbName?: string;
    /** outline 名字 */
    outlineName?: string;
    /** Sql 绑定了哪个索引, 如索引绑定，会返回绑定了哪个表的哪个索引 */
    bindIndex?: TableIndex;
    type?: OutlineType;
    /** 创建Outline的 Sql Id */
    sqlId?: string;
    /** outline 的 Hint 内容 */
    outlineContent?: string;
    /** 计划绑定时 执行计划的UID */
    planUid?: string;
    /** 计划绑定时 执行计划的采集时间 */
    planCollectTimeUs?: number;
    /** 目标 */
    outlineTarget?: string;
    /** outline 参数，保留参数，直接从OB捞出来的，因为被做了二进制处理, 导致展示出来很怪，暂未用到。 */
    outlineParams?: string;
    /** signature 参数化后的SQL 如：select * from test where id = ? and name = ? */
    visibleSignature?: string;
    /** 创建outline时的sql文本 */
    sqlText?: string;
    /** 单机处理SQL的最大并发数 */
    concurrentNum?: number;
    /** 限流对象 如： select * from test where id = 1 and name = ? */
    limitTarget?: string;
    /** outline的状态 生效中或者已经取消 */
    status?: OutlineStatus;
    /** outline的创建时间 即 绑定时间 */
    createTime?: string;
    /** outline 的删除时间 */
    deleteTime?: string;
    /** 创建 outline 的OCP用户名 */
    operatorName?: string;
  }

  type OutlineStatus = 'VALID' | 'CANCELED';

  type OutlineType = 'INDEX' | 'PLAN' | 'CONCURRENT_LIMIT';

  type Pageable = Record<string, any>;

  interface PaginatedResponse {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PaginatedResponsePaginatedData_BasicTaskInstance_;
  }

  interface PaginatedResponsePaginatedData {
    page?: CustomPage;
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<Record<string, any>>;
  }

  interface PaginatedResponsePaginatedData_BasicTaskInstance_ {
    page?: CustomPage;
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<BasicTaskInstance>;
  }

  interface PaginatedResponsePaginatedData_MetricClass_ {
    page?: CustomPage;
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<MetricClass>;
  }

  interface PaginatedResponsePaginatedData_PropertyMeta_ {
    page?: CustomPage;
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<PropertyMeta>;
  }

  interface PaginatedResponsePaginatedData_TenantInfo_ {
    page?: CustomPage;
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<TenantInfo>;
  }

  interface PaginatedResponsePaginatedData_TenantSession_ {
    page?: CustomPage;
    /** use List not Iterable due swagger ui does not support Iterable type */
    contents?: Array<TenantSession>;
  }

  interface PaginatedResponse_BasicTaskInstance_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PaginatedResponsePaginatedData_BasicTaskInstance_;
  }

  interface PaginatedResponse_MetricClass_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PaginatedResponsePaginatedData_MetricClass_;
  }

  interface PaginatedResponse_PropertyMeta_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PaginatedResponsePaginatedData_PropertyMeta_;
  }

  interface PaginatedResponse_TenantInfo_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PaginatedResponsePaginatedData_TenantInfo_;
  }

  interface PaginatedResponse_TenantSession_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PaginatedResponsePaginatedData_TenantSession_;
  }

  type PartitionRole = 'LEADER' | 1 | 'FOLLOWER' | 2 | 'UNKNOWN' | -1;

  interface PerformanceStats {
    active_session?: number;
    qps?: number;
    tps?: number;
    unit_num?: number;
  }

  interface PlanExplain {
    /** 操作列表 */
    rootOperations?: Array<PlanOperation>;
  }

  interface PlanOperation {
    /** 逻辑算子 */
    operator?: string;
    /** 操作对象 */
    objectName?: string;
    /** 预估行 */
    rows?: number;
    /** 代价 */
    cost?: number;
    /** 输出过滤 */
    property?: string;
    /** 子操作 */
    children?: Array<PlanOperation>;
  }

  interface PlanStatBase {
    /** 期间内的命中率 */
    hitCount?: number;
    /** 期间内的执行次数 */
    executions?: number;
    /** 期间内的超时次数 */
    timeoutCount?: number;
    /** 当前磁盘读 */
    diskReads?: number;
    /** 当前磁盘写 */
    directWrites?: number;
    /** 当前缓存读 */
    bufferGets?: number;
    /** 当前Application事件等待时间（毫秒） */
    applicationWaitTime?: number;
    /** 当前Concurrency事件等待时间（毫秒） */
    concurrencyWaitTime?: number;
    /** 当前UserIO事件等待时间（毫秒） */
    userIoWaitTime?: number;
    /** 当前处理行数 */
    rowProcessed?: number;
    /** 期间内的平均响应时间 */
    elapsedTime?: number;
    /** 期间内的平均Cpu时间 */
    cpuTime?: number;
    /** 期间内的大查询百分比 */
    largeQuery?: number;
    /** 期间内被置入队列的大查询的百分比 */
    delayedLargeQueryPercentage?: number;
    /** 期间内执行超时的百分比 */
    timeoutPercentage?: number;
    /** 是否包含表扫描 */
    tableScan?: boolean;
  }

  interface PlanStatDetail {
    /** 期间内的命中率 */
    hitCount?: number;
    /** 期间内的执行次数 */
    executions?: number;
    /** 期间内的超时次数 */
    timeoutCount?: number;
    /** 当前磁盘读 */
    diskReads?: number;
    /** 当前磁盘写 */
    directWrites?: number;
    /** 当前缓存读 */
    bufferGets?: number;
    /** 当前Application事件等待时间（毫秒） */
    applicationWaitTime?: number;
    /** 当前Concurrency事件等待时间（毫秒） */
    concurrencyWaitTime?: number;
    /** 当前UserIO事件等待时间（毫秒） */
    userIoWaitTime?: number;
    /** 当前处理行数 */
    rowProcessed?: number;
    /** 期间内的平均响应时间 */
    elapsedTime?: number;
    /** 期间内的平均Cpu时间 */
    cpuTime?: number;
    /** 期间内的大查询百分比 */
    largeQuery?: number;
    /** 期间内被置入队列的大查询的百分比 */
    delayedLargeQueryPercentage?: number;
    /** 期间内执行超时的百分比 */
    timeoutPercentage?: number;
    /** 是否包含表扫描 */
    tableScan?: boolean;
    /** Plan的唯一标识。由obServerId、planId、firstLoadTimeUs的值计算而得，在集群内唯一。使用Base64编码。 */
    uid?: string;
    /** Plan执行的Server，包括ip和port */
    server?: string;
    /** Plan的Id。OB的PlanId是Server范围的自增值，在重启Server后重置。 */
    planId?: number;
    /** Plan首次被加载的时间 */
    firstLoadTime?: string;
    /** Plan的类型：LOCAL、REMOTE、DIST */
    planType?: string;
    /** Plan的哈希值，包括Plan的操作、目标及其版本，不包括cost。 */
    planHash?: string;
    /** Plan的唯一性标识，利用Plan的操作、目标来进行计算 */
    planUnionHash?: string;
    /** Plan的大小 */
    planSize?: number;
    /** Schema版本 */
    schemaVersion?: number;
    /** 合并版本 */
    mergedVersion?: number;
    /** OB端的ServerId */
    obServerId?: number;
    /** OB 端的 Database id */
    obDbId?: number;
    /** OCP端的ServerId */
    serverId?: number;
    /** Plan是否在诊断结果中 */
    hitDiagnosis?: boolean;
    /** Plan的outline_data字段 */
    outlineData?: string;
    /** Plan的outline_id字段 */
    outlineId?: number;
    /** 首次加载时间 */
    firstLoadTimeUs?: number;
    /** Plan Explain */
    planExplain?: PlanExplain;
  }

  interface PlanStatGroup {
    /** Plan的哈希值，包括Plan的操作、目标及其版本，不包括cost。 */
    planHash?: string;
    /** Plan的唯一性标识，利用Plan的操作、目标来进行计算 */
    planUnionHash?: string;
    /** planHash 和 type相同的plan列表 */
    plans?: Array<PlanStatDetail>;
    /** 合并版本 */
    mergedVersion?: number;
    /** Plan首次被加载的时间 */
    firstLoadTime?: string;
    /** Plan的类型：LOCAL、REMOTE、DIST */
    planType?: string;
    /** 期间内的命中率 */
    hitPercentage?: number;
    /** 期间内的平均Cpu时间 */
    avgCpuTime?: number;
    /** PlanExplain 执行计划各个算子 */
    planExplain?: PlanExplain;
    /** 该执行计划总共执行次数 */
    executions?: number;
    /** 期间内的超时次数 */
    timeoutCount?: number;
    /** 执行计划对应的query sql，参数化后的数据 */
    querySql?: string;
  }

  interface PropertyManager {
    observerLogRegex?: string;
    obproxyLogRegex?: string;
    hostLogRegex?: string;
    ocpLogDownloadTmpDir?: string;
    ocpLogDownloadHttpReadTimeout?: number;
    ocpLogDownloadHttpConnectTimeout?: number;
    monitorAgentLogLevel?: string;
    managerAgentLogLevel?: string;
    maxLogDownloadSpeedMB?: number;
    userLoginLockoutMinutes?: number;
    userLoginMaxAttempts?: number;
    maxReserveHourForUnusedUnit?: number;
    systemAccountActivated?: boolean;
    metricCollectSecondInterval?: number;
    /** ob版本与参数模版映射关系 */
    obVersionParameterTemplateRelation?: string;
  }

  interface PropertyMeta {
    id?: number;
    key?: string;
    application?: string;
    profile?: string;
    label?: string;
    value?: string;
    runningValue?: string;
    needRestart?: boolean;
    fatal?: boolean;
    description?: string;
    createTime?: string;
    updateTime?: string;
  }

  interface QueryLogRequest {
    ip?: string;
    port?: number;
    logType?: string;
    startTime?: string;
    endTime?: string;
    keyword?: Array<string>;
    keywordType?: string;
    excludeKeyword?: Array<string>;
    excludeKeywordType?: string;
    logLevel?: Array<LogLevel>;
    position?: string;
    limit?: number;
  }

  interface QueryLogResult {
    /** 日志条目 */
    logEntries?: Array<LogEntry>;
    /** 查询到的位置，以"文件 id:文件 offset"的方式表示 */
    position?: string;
    /** 对应的主机 ip */
    ip?: string;
    /** 对应的主机 observer port */
    port?: number;
  }

  type ReplicaType = 'FULL' | 'LOGONLY' | 'READONLY';

  type Resource = Record<string, any>;

  interface ResourceAssignStats {
    /** unit 总数 */
    unitCount?: number;
    cpuCoreTotal?: number;
    cpuCoreAssigned?: number;
    cpuCoreAssignedPercent?: number;
    memoryTotal?: string;
    memoryAssigned?: string;
    memoryInBytesTotal?: number;
    memoryInBytesAssigned?: number;
    memoryAssignedPercent?: number;
    diskTotal?: string;
    diskAssigned?: string;
    diskInBytesTotal?: number;
    diskInBytesAssigned?: number;
    diskAssignedPercent?: number;
  }

  interface ResourcePool {
    id?: number;
    name?: string;
    unitCount?: number;
    unitConfig?: UnitConfig;
    zoneList?: Array<string>;
  }

  interface ResourceStats {
    /** unit 总数 */
    unitCount?: number;
    cpuCoreTotal?: number;
    cpuCoreAssigned?: number;
    cpuCoreAssignedPercent?: number;
    memoryTotal?: string;
    memoryAssigned?: string;
    memoryInBytesTotal?: number;
    memoryInBytesAssigned?: number;
    memoryAssignedPercent?: number;
    diskTotal?: string;
    diskAssigned?: string;
    diskInBytesTotal?: number;
    diskInBytesAssigned?: number;
    diskAssignedPercent?: number;
    diskUsed?: string;
    diskFree?: string;
    diskInBytesUsed?: number;
    diskInBytesFree?: number;
    diskUsedPercent?: number;
  }

  type ResponseEntity = Record<string, any>;

  type ResponseEntity_Resource_ = Record<string, any>;

  interface RevokeDbPrivilegeParam {
    dbPrivileges?: Array<DbPrivilegeParam>;
  }

  interface RevokeGlobalPrivilegeParam {
    globalPrivileges?: Array<string>;
  }

  interface RevokeObjectPrivilegeParam {
    objectPrivileges?: Array<ObjectPrivilege>;
  }

  interface RevokeRoleParam {
    roles?: Array<string>;
  }

  interface RootServer {
    ip?: string;
    svrPort?: number;
    role?: RootServerRole;
  }

  type RootServerRole = 'LEADER' | 'FOLLOWER' | 'UNKNOWN';

  interface RootService {
    svrIp?: string;
    svrPort?: number;
    zone?: string;
    role?: PartitionRole;
  }

  interface SeriesMetricValues {
    metric?: Metric;
    data?: Array<ValueData>;
  }

  interface Server {
    id?: number;
    ip?: string;
    port?: number;
    sqlPort?: number;
    version?: string;
    architecture?: string;
    withRootserver?: boolean;
    status?: ObServerStatus;
    innerStatus?: ObServerInnerStatus;
    zoneName?: string;
    startTime?: string;
    stopTime?: string;
    stats?: ServerResourceStats;
    performanceStats?: PerformanceStats;
    availableOperations?: Array<string>;
    dataPath?: string;
    logPath?: string;
  }

  interface ServerAddressInfo {
    address?: string;
    svrPort?: number;
    sqlPort?: number;
    withRootServer?: boolean;
    agentMgrPort?: number;
    agentMonPort?: number;
  }

  interface ServerParameterValue {
    svrIp?: string;
    svrPort?: number;
    value?: string;
  }

  interface ServerResourceStats {
    /** unit 总数 */
    unitCount?: number;
    cpuCoreTotal?: number;
    cpuCoreAssigned?: number;
    cpuCoreAssignedPercent?: number;
    memoryTotal?: string;
    memoryAssigned?: string;
    memoryInBytesTotal?: number;
    memoryInBytesAssigned?: number;
    memoryAssignedPercent?: number;
    diskTotal?: string;
    diskAssigned?: string;
    diskInBytesTotal?: number;
    diskInBytesAssigned?: number;
    diskAssignedPercent?: number;
    diskUsed?: string;
    diskFree?: string;
    diskInBytesUsed?: number;
    diskInBytesFree?: number;
    diskUsedPercent?: number;
    ip?: string;
    port?: number;
    zone?: string;
    /** leader 分区数量 */
    partitionCount?: number;
    timestamp?: number;
  }

  interface SessionClientStats {
    totalCount?: number;
    activeCount?: number;
    clientIp?: string;
  }

  interface SessionDbStats {
    totalCount?: number;
    activeCount?: number;
    dbName?: string;
  }

  interface SessionStats {
    totalCount?: number;
    activeCount?: number;
    maxActiveTime?: number;
    dbStats?: Array<SessionDbStats>;
    userStats?: Array<SessionUserStats>;
    clientStats?: Array<SessionClientStats>;
  }

  interface SessionUserStats {
    totalCount?: number;
    activeCount?: number;
    dbUser?: string;
  }

  interface SlowSqlRankInfo {
    /** 租户Id */
    tenantId?: number;
    /** 租户名 */
    tenantName?: string;
    /** 数量 */
    count?: number;
  }

  interface Sql {
    /** 数据库名 */
    dbName?: string;
    /** sqlId */
    sqlId?: string;
  }

  interface SqlAuditStatBase {
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
    /** 期间内的总等待时间（毫秒） */
    sumWaitTime?: number;
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
    /** 期间内的平均RowCache命中次数 */
    avgRowCacheHit?: number;
    /** 期间内的平均BloomFilterCache命中次数 */
    avgBloomFilterCacheHit?: number;
    /** 期间内的平均BlockCache命中次数 */
    avgBlockCacheHit?: number;
    /** 期间内的平均BlockIndexCache命中次数 */
    avgBlockIndexCacheHit?: number;
    /** 期间内的平均物理读次数 */
    avgDiskReads?: number;
    /** 期间内的总计重试次数 */
    retryCount?: number;
    /** 期间内的表扫描百分比 */
    tableScanPercentage?: number;
    /** 期间内的强一致性事务百分比 */
    strongConsistencyPercentage?: number;
    /** 期间内的弱一致性事务百分比 */
    weakConsistencyPercentage?: number;
    /** 期间内的平均Memstore读行数 */
    avgMemstoreReadRows?: number;
    /** 期间内的平均SsStore读行数 */
    avgSsstoreReadRows?: number;
    /** 期间内最大更新行数 */
    maxAffectedRows?: number;
    /** 期间内最大返回行数 */
    maxReturnRows?: number;
    /** 期间内的最大等待时间（毫秒） */
    maxWaitTime?: number;
    /** 期间内的最大Application事件等待时间（毫秒） */
    maxApplicationWaitTime?: number;
    /** 期间内的最大Concurrency事件等待时间（毫秒） */
    maxConcurrencyWaitTime?: number;
    /** 期间内的最大UserIO事件等待时间（毫秒） */
    maxUserIoWaitTime?: number;
    /** 期间内的最大物理读次数 */
    maxDiskReads?: number;
    /** 期间内平均并行度 */
    avgExpectedWorkerCount?: number;
    /** 期间内sql平均使用线程数 */
    avgUsedWorkerCount?: number;
  }

  interface SqlAuditStatSummary {
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
    /** 期间内的总等待时间（毫秒） */
    sumWaitTime?: number;
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
    /** 期间内的平均RowCache命中次数 */
    avgRowCacheHit?: number;
    /** 期间内的平均BloomFilterCache命中次数 */
    avgBloomFilterCacheHit?: number;
    /** 期间内的平均BlockCache命中次数 */
    avgBlockCacheHit?: number;
    /** 期间内的平均BlockIndexCache命中次数 */
    avgBlockIndexCacheHit?: number;
    /** 期间内的平均物理读次数 */
    avgDiskReads?: number;
    /** 期间内的总计重试次数 */
    retryCount?: number;
    /** 期间内的表扫描百分比 */
    tableScanPercentage?: number;
    /** 期间内的强一致性事务百分比 */
    strongConsistencyPercentage?: number;
    /** 期间内的弱一致性事务百分比 */
    weakConsistencyPercentage?: number;
    /** 期间内的平均Memstore读行数 */
    avgMemstoreReadRows?: number;
    /** 期间内的平均SsStore读行数 */
    avgSsstoreReadRows?: number;
    /** 期间内最大更新行数 */
    maxAffectedRows?: number;
    /** 期间内最大返回行数 */
    maxReturnRows?: number;
    /** 期间内的最大等待时间（毫秒） */
    maxWaitTime?: number;
    /** 期间内的最大Application事件等待时间（毫秒） */
    maxApplicationWaitTime?: number;
    /** 期间内的最大Concurrency事件等待时间（毫秒） */
    maxConcurrencyWaitTime?: number;
    /** 期间内的最大UserIO事件等待时间（毫秒） */
    maxUserIoWaitTime?: number;
    /** 期间内的最大物理读次数 */
    maxDiskReads?: number;
    /** 期间内平均并行度 */
    avgExpectedWorkerCount?: number;
    /** 期间内sql平均使用线程数 */
    avgUsedWorkerCount?: number;
    /** SQL的Id */
    sqlId?: string;
    /** SQL执行所在的Server（svr_ip:svr_port） */
    server?: string;
    /** SQL执行所在的Server的IP */
    serverIp?: string;
    /** SQL执行所在的Server的Port */
    serverPort?: number;
    /** SQL访问的数据库 */
    dbName?: string;
    /** SQL的用户 */
    userName?: string;
    /** SQL的类型 */
    sqlType?: string;
    /** SQL的文本（前100字符） */
    sqlTextShort?: string;
    /** 是否内部SQL */
    inner?: boolean;
    /** 期间内的最长等待事件 */
    waitEvent?: string;
    /** 自定义列 */
    customColumns?: Array<CustomColumn>;
    startTimeUs?: number;
    endTimeUs?: number;
    obUserId?: number;
    obDbId?: number;
  }

  interface SqlText {
    /** SQL全文本 */
    fulltext?: string;
    /** 数据库用户名 */
    userName?: string;
    /** sql里的表名 */
    tables?: Array<string>;
    /** SQL是否能绑定outline, 2270以下的版本要求 select后第一个字符必须为空格 */
    supportOutline?: boolean;
    /** SQL参数化后的文本 */
    statement?: string;
    /** 数据库名 */
    dbName?: string;
  }

  interface SubtaskInstance {
    id?: number;
    seriesId?: number;
    name?: string;
    description?: string;
    timeout?: number;
    status?: SubtaskState;
    executor?: string;
    runTime?: number;
    context?: Context;
    createTime?: string;
    /** 子任务首次开始执行时间. */
    startTime?: string;
    /** 子任务最后执行的结束时间. */
    finishTime?: string;
    nodeType?: NodeType;
    parallelIdx?: number;
    operation?: SubtaskOperation;
    upstreams?: Array<number>;
    downstreams?: Array<number>;
    prohibitRollback?: boolean;
  }

  interface SubtaskLog {
    /** 子任务执行的日志. */
    log?: string;
  }

  type SubtaskOperation =
    | 'execute'
    | 'EXECUTE'
    | 'retry'
    | 'RETRY'
    | 'rollback'
    | 'ROLLBACK'
    | 'skip'
    | 'SKIP'
    | 'rollback_skip'
    | 'ROLLBACK_SKIP'
    | 'cancel'
    | 'CANCEL';

  type SubtaskState = 'PENDING' | 'READY' | 'RUNNING' | 'CANCELING' | 'FAILED' | 'SUCCESSFUL';

  interface SuccessResponse {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: Record<string, any>;
  }

  interface SuccessResponse_AuthenticatedUser_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: AuthenticatedUser;
  }

  interface SuccessResponse_BasicCluster_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: BasicCluster;
  }

  interface SuccessResponse_BatchConcurrentLimitResult_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: BatchConcurrentLimitResult;
  }

  interface SuccessResponse_BatchDropOutlineResult_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: BatchDropOutlineResult;
  }

  interface SuccessResponse_CheckTenantPasswordResult_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: CheckTenantPasswordResult;
  }

  interface SuccessResponse_ClusterInfo_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: ClusterInfo;
  }

  interface SuccessResponse_ClusterUnitSpecLimit_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: ClusterUnitSpecLimit;
  }

  interface SuccessResponse_ClusterUnitView_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: ClusterUnitView;
  }

  interface SuccessResponse_Database_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: Database;
  }

  interface SuccessResponse_DbRole_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: DbRole;
  }

  interface SuccessResponse_DbUser_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: DbUser;
  }

  interface SuccessResponse_HostInfo_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: HostInfo;
  }

  interface SuccessResponse_LoginKey_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: LoginKey;
  }

  interface SuccessResponse_Map_String_Number__ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: Record<string, any>;
  }

  interface SuccessResponse_Map_String_String__ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: Record<string, any>;
  }

  interface SuccessResponse_ObAgentDetail_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: ObAgentDetail;
  }

  interface SuccessResponse_PlanExplain_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PlanExplain;
  }

  interface SuccessResponse_PropertyMeta_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: PropertyMeta;
  }

  interface SuccessResponse_QueryLogResult_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: QueryLogResult;
  }

  interface SuccessResponse_Server_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: Server;
  }

  interface SuccessResponse_SessionStats_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: SessionStats;
  }

  interface SuccessResponse_SqlText_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: SqlText;
  }

  interface SuccessResponse_SubtaskInstance_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: SubtaskInstance;
  }

  interface SuccessResponse_SubtaskLog_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: SubtaskLog;
  }

  interface SuccessResponse_SystemInfo_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: SystemInfo;
  }

  interface SuccessResponse_TaskInstance_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: TaskInstance;
  }

  interface SuccessResponse_TenantCompaction_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: TenantCompaction;
  }

  interface SuccessResponse_TenantInfo_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: TenantInfo;
  }

  interface SuccessResponse_TenantPreCheckResult_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: TenantPreCheckResult;
  }

  interface SuccessResponse_TenantSession_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: TenantSession;
  }

  interface SuccessResponse_WrappedTaskInstance_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: WrappedTaskInstance;
  }

  interface SuccessResponse_object_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: Record<string, any>;
  }

  interface SuccessResponse_string_ {
    successful?: boolean;
    timestamp?: string;
    duration?: number;
    status?: number;
    traceId?: string;
    /** server 名称，OCP 多节点部署，调试时客户端并不知道服务端是哪个节点，此字段能用于区分。值是 internal ip address */
    server?: string;
    /** result data */
    data?: string;
  }

  interface SystemInfo {
    propertyManager?: PropertyManager;
    dataSource?: DataSource;
    /** VPC 模式信息 */
    vpcModeInfo?: SystemInfoVpcModeInfo;
    metaTenantName?: string;
  }

  interface SystemInfoVpcModeInfo {
    /** VPC 模式是否开启 */
    vpcModeEnabled?: boolean;
  }

  interface TableIndex {
    /** 数据库名 */
    dbName?: string;
    /** 表名 */
    tableName?: string;
    /** 索引名 */
    indexName?: string;
    /** 索引列组合 已按顺序排好序 */
    columnNames?: Array<string>;
    /** 索引状态 */
    indexStatus?: IndexStatus;
    /** 索引类型 */
    indexType?: IndexType;
  }

  interface TaskInstance {
    id?: number;
    name?: string;
    taskDefinitionId?: number;
    status?: TaskState;
    type?: TaskType;
    obTenantId?: number;
    creator?: string;
    executor?: string;
    context?: Context;
    createTime?: string;
    startTime?: string;
    finishTime?: string;
    subtasks?: Array<SubtaskInstance>;
    operation?: TaskOperation;
    /** 是否允许回滚. */
    prohibitRollback?: boolean;
  }

  type TaskOperation = 'execute' | 'EXECUTE' | 'retry' | 'RETRY' | 'rollback' | 'ROLLBACK';

  type TaskState = 'RUNNING' | 'FAILED' | 'SUCCESSFUL';

  type TaskType = 'MANUAL' | 'SYS_ONCE' | 'SCHEDULED' | 'SYS_SCHEDULED';

  interface Tenant {
    id?: number;
    name?: string;
    obTenantId?: number;
    mode?: TenantMode;
    clusterName?: string;
    obClusterId?: number;
    obVersion?: string;
    createTime?: string;
    primaryZone?: string;
    zoneList?: string;
    locality?: string;
    status?: TenantStatus;
    locked?: boolean;
    readonly?: boolean;
    description?: string;
  }

  interface TenantChangePasswordParam {
    newPassword?: string;
  }

  interface TenantCompaction {
    /** 租户ID */
    obTenantId?: number;
    /** 租户名 */
    tenantName?: string;
    /** 广播版本，rs调度合并任务时广播的版本 */
    broadcastScn?: number;
    /** 是否合并出错 */
    error?: boolean;
    /** 租户合并状态 */
    status?: ObTenantCompactionStatus;
    /** 租户冻结版本 */
    frozenScn?: number;
    /** 上次合并版本 */
    lastScn?: number;
    /** 合并是否已暂停 */
    suspend?: boolean;
    /** 上次合并结束时间 */
    lastFinishTime?: string;
    /** 合并开始时间 */
    startTime?: string;
  }

  interface TenantCompactionHistory {
    /** 租户ID */
    obTenantId?: number;
    /** 租户名 */
    tenantName?: string;
    /** 合并时间段及其结果，按照startTime正序 */
    compactionList?: Array<TenantCompactionHistoryCompactionDuration>;
  }

  interface TenantCompactionHistoryCompactionDuration {
    /** 合并结果, 成功、失败、合并中 */
    result?: TenantCompactionResult;
    /** 合并耗时，单位:s */
    costTime?: number;
    /** 合并开始时间 */
    startTime?: string;
    /** 合并结束时间，正在合并或者合并失败，这个值是空 */
    endTime?: string;
  }

  type TenantCompactionResult = 'SUCCESS' | 'FAIL' | 'COMPACTING';

  interface TenantInfo {
    id?: number;
    name?: string;
    obTenantId?: number;
    mode?: TenantMode;
    clusterName?: string;
    obClusterId?: number;
    obVersion?: string;
    createTime?: string;
    primaryZone?: string;
    zoneList?: string;
    locality?: string;
    status?: TenantStatus;
    locked?: boolean;
    readonly?: boolean;
    description?: string;
    charset?: string;
    collation?: string;
    zones?: Array<TenantZone>;
    whitelist?: string;
    obproxyAndConnectionStrings?: Array<ObproxyAndConnectionString>;
  }

  type TenantMode = 'ORACLE' | 'MYSQL';

  interface TenantParameter {
    /** 参数名 */
    name?: string;
    /** 参数类型 */
    parameterType?: TenantParameterType;
    /** 当前值 */
    currentValue?: string;
    /** 租户兼容性 */
    compatibleType?: CompatibleType;
    /** 描述 */
    description?: string;
    /** 是否重启生效 */
    needRestart?: boolean;
    /** 是否只读 */
    readonly?: boolean;
  }

  interface TenantParameterInfo {
    /** 参数名 */
    name?: string;
    /** 参数类型 */
    parameterType?: TenantParameterType;
    /** 租户兼容性 */
    compatibleType?: CompatibleType;
    /** 描述 */
    description?: string;
    /** 是否重启生效 */
    needRestart?: boolean;
    /** 是否只读 */
    readonly?: boolean;
  }

  interface TenantParameterParam {
    name: string;
    value: string;
    parameterType?: TenantParameterType;
  }

  type TenantParameterType = 'OB_TENANT_PARAMETER' | 'OB_SYSTEM_VARIABLE';

  interface TenantParameterValue {
    tenantId?: number;
    tenantName?: string;
    value?: string;
  }

  interface TenantPreCheckResult {
    obTenantId?: number;
    emptySuperUserPassword?: boolean;
  }

  interface TenantSession {
    id?: number;
    dbUser?: string;
    clientIp?: string;
    dbName?: string;
    command?: string;
    time?: number;
    status?: string;
    info?: string;
    proxyIp?: string;
    transHash?: string;
  }

  type TenantStatus =
    | 'NORMAL'
    | 'UNAVAILABLE'
    | 'CREATING'
    | 'MODIFYING'
    | 'RESTORING'
    | 'DELETING';

  interface TenantZone {
    name?: string;
    replicaType?: string;
    resourcePool?: ResourcePool;
    units?: Array<Unit>;
  }

  interface Unit {
    id?: number;
    resourcePoolId?: number;
    serverId?: number;
    serverIp?: string;
    serverPort?: number;
    hostId?: number;
    zoneName?: string;
    status?: string;
  }

  interface UnitConfig {
    maxCpuCoreCount?: number;
    minCpuCoreCount?: number;
    maxMemoryByte?: number;
    maxMemorySize?: number;
    minMemoryByte?: number;
    minMemorySize?: number;
    maxDiskSizeByte?: number;
    maxDiskSize?: number;
    maxIops?: number;
    minIops?: number;
    maxSessionNum?: number;
    name?: string;
  }

  interface UnitSpecParam {
    cpuCore: number;
    memorySize: number;
  }

  interface UpdateClusterParameterParam {
    /** 参数名 */
    name: string;
    /** 参数值 */
    value: string;
    /** 参数类型，取值：OB_CLUSTER_PARAMETER（OB 集群配置项）、OB_TENANT_PARAMETER（OB
租户配置项）。为了兼容旧版行为，不传时默认为 OB_CLUSTER_PARAMETER */
    parameterType?: ClusterParameterType;
    /** 指定生效的 Zone 列表，不能与 servers 同时指定。如果 zones 与 servers 都不传，表示对所有 Zone 都生效 */
    zones?: Array<string>;
    /** 指定生效的 OBServer 列表，不能与 zones 同时指定。如果 zones 与 servers 都不传，表示对所有 OBServer 都生效 */
    servers?: Array<string>;
    /** 指定生效的租户列表，不能与 allTenants 同时指定。仅当 parameterType 为 OB_TENANT_PARAMETER 时有效。 */
    tenants?: Array<string>;
    /** 是否对所有租户生效，不能与 tenants 同时指定。仅当 parameterType 为 OB_TENANT_PARAMETER 时有效。 */
    allTenants?: boolean;
  }

  interface ValueData {
    /** 时间戳，epoch second */
    timestamp?: number;
    /** 量测值 */
    value?: number;
  }

  interface WrappedTaskInstance {
    id?: number;
    name?: string;
    taskDefinitionId?: number;
    status?: TaskState;
    type?: TaskType;
    creator?: BasicCreatorInfo;
    tenantInfo?: BasicTenantInfo;
    executor?: string;
    createTime?: string;
    startTime?: string;
    finishTime?: string;
    subtasks?: Array<SubtaskInstance>;
    operation?: TaskOperation;
    prohibitRollback?: boolean;
  }

  interface Zone {
    name?: string;
    idcName: string;
    regionName?: string;
    servers: Array<Server>;
    status?: ObZoneStatus;
    innerStatus?: ObZoneInnerStatus;
    clusterId?: number;
    obClusterId?: number;
    rootServer?: RootServer;
    serverCount?: number;
    hostCount?: number;
    performanceStats?: PerformanceStats;
    availableOperations?: Array<string>;
  }

  interface ZoneParam {
    name: string;
    replicaType: ReplicaType;
    resourcePool: CreateTenantParamPoolParam;
  }
}
