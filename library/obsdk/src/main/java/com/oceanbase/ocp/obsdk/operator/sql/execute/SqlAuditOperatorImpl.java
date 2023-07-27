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

package com.oceanbase.ocp.obsdk.operator.sql.execute;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.common.util.time.TimeUtils;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.SqlAuditOperator;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlAuditRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlTextEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.TenantSlowSqlCounter;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySlowSqlRankParam;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySqlTextAny;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryTopSqlRawStatParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlAuditOperatorImpl implements SqlAuditOperator {

    public static String SQL_AUDIT_BASE_STATEMENT =
            "  ( select sql_id, tenant_id, tenant_name, user_id, user_name,  svr_ip, svr_port, db_id, db_name, plan_id, trace_id, sid,client_ip,client_port,user_client_ip, "
                    + " request_time, request_id, elapsed_time, execute_time, total_wait_time_micro, wait_time_micro, get_plan_time,  affected_rows, return_rows, partition_cnt, ret_code, "
                    + " (execute_time - total_wait_time_micro + get_plan_time) as cpu_time, (case when ret_code = 0 then 0 else 1 end) as fail_count, "
                    + " (case when ret_code = -4012 then 1 else 0 end) as ret_code_4012_count, (case when ret_code = -4013 then 1 else 0 end) as ret_code_4013_count, "
                    + " (case when ret_code = -5001 then 1 else 0 end) as ret_code_5001_count, (case when ret_code = -5024 then 1 else 0 end) as ret_code_5024_count, "
                    + " (case when ret_code = -5167 then 1 else 0 end) as ret_code_5167_count, (case when ret_code = -5217 then 1 else 0 end) as ret_code_5217_count, "
                    + " (case when ret_code = -6002 then 1 else 0 end) as ret_code_6002_count, (case event when 'system internal wait' then wait_time_micro else 0 end) as event_0_wait_time, "
                    + " (case event when 'mysql response wait client' then wait_time_micro else 0 end) as event_1_wait_time, (case event when 'sync rpc' then wait_time_micro else 0 end) as event_2_wait_time, "
                    + " (case event when 'db file data read' then wait_time_micro else 0 end) as event_3_wait_time, "
                    + " total_waits, rpc_count, plan_type, (case when plan_type=1 then 1 else 0 end) as plan_type_local_count, (case when plan_type=2 then 1 else 0 end) as plan_type_remote_count, "
                    + " (case when plan_type=3 then 1 else 0 end) as plan_type_dist_count, is_inner_sql, is_executor_rpc, is_hit_plan, consistency_level, (case when is_inner_sql=1 then 1 else 0 end) as inner_sql_count, "
                    + " (case when is_executor_rpc = 1 then 1 else 0 end) as executor_rpc_count, (case when is_hit_plan=1 then 0 else 1 end) as miss_plan_count, "
                    + " (case consistency_level when 3 then 1 else 0 end) as consistency_level_strong, (case consistency_level when 2 then 1 else 0 end) as consistency_level_weak, "
                    + " net_time, net_wait_time, queue_time, decode_time, application_wait_time, concurrency_wait_time, user_io_wait_time, schedule_time, row_cache_hit, "
                    + " bloom_filter_cache_hit, block_cache_hit, disk_reads, retry_cnt, table_scan, memstore_read_row_count, ssstore_read_row_count  from GV$OB_SQL_AUDIT  ";

    public static String JOIN_PLAN_STAT_STATEMENT = " ) S join GV$OB_PLAN_CACHE_PLAN_STAT T on T.sql_id = S.sql_id ) ";


    public static String SQL_AUDIT_WITH_TEXT_STATEMENT =
            "( select S.*, T.statement , T.query_sql from " + SQL_AUDIT_BASE_STATEMENT;

    public static String SELECT_SQL_AUDIT_STATEMENT =
            " select tenant_id as obTenantId, db_id as obDbId, db_name as dbName, statement as statement, "
                    + " query_sql as sqlText, user_id as obUserId, user_name as userName, "
                    + " svr_ip as serverIp, svr_port as serverPort, sql_id as sqlId, "
                    + " sum(affected_rows) as affectedRows, max(affected_rows) as maxAffectedRows, "
                    + " sum(return_rows) as returnRows, max(return_rows) as maxReturnRows, "
                    + " sum(partition_cnt) as partitionCount, sum(fail_count) as failCount,"
                    + " sum(ret_code_4012_count) as retCode4012Count, sum(ret_code_4013_count) as retCode4013Count, "
                    + " sum(ret_code_5001_count) as retCode5001Count, sum(ret_code_5024_count) as retCode5024Count, "
                    + " sum(ret_code_5167_count) as retCode5167Count, sum(ret_code_5217_count) as retCode5217Count, "
                    + " sum(ret_code_6002_count) as retCode6002Count, sum(event_0_wait_time) as event0WaitTimeUs, "
                    + " sum(event_1_wait_time) as event1WaitTimeUs, sum(event_2_wait_time) as event2WaitTimeUs, "
                    + " sum(event_3_wait_time) as event3WaitTimeUs, sum(TOTAL_WAIT_TIME_MICRO) as totalWaitTimeUs, "
                    + " max(TOTAL_WAIT_TIME_MICRO) as maxTotalWaitTimeUs, sum(total_waits) as totalWaits, "
                    + " sum(rpc_count) as rpcCount, sum(plan_type_local_count) as localPlanCount, "
                    + " sum(plan_type_remote_count) as remotePlanCount, sum(plan_type_dist_count) as distPlanCount, "
                    + " sum(inner_sql_count) as innerSqlCount,sum(executor_rpc_count) as executorRpcCount, "
                    + " sum(miss_plan_count) as missPlanCount, sum(elapsed_time) as elapsedTimeUs, "
                    + " max(elapsed_time) as maxElapsedTimeUs, sum(net_time) as netTimeUs, "
                    + " sum(net_wait_time) as netWaitTimeUs, sum(queue_time) as queueTimeUs, "
                    + " sum(decode_time) as decodeTimeUs,sum(get_plan_time) as getPlanTimeUs, "
                    + " sum(execute_time) as executeTimeUs,sum(cpu_time) as cpuTimeUs, "
                    + " max(cpu_time) as maxCpuTimeUs,sum(application_wait_time) as applicationWaitTimeUs, "
                    + " max(application_wait_time) as maxApplicationWaitTimeUs,sum(concurrency_wait_time) as concurrencyWaitTimeUs, "
                    + " max(concurrency_wait_time) as maxConcurrencyWaitTimeUs, sum(user_io_wait_time) as userIoWaitTimeUs, "
                    + " max(user_io_wait_time) as maxUserIoWaitTimeUs, sum(schedule_time) as scheduleTimeUs, "
                    + " sum(row_cache_hit) as rowCacheHit, sum(bloom_filter_cache_hit) as bloomFilterCacheHit, "
                    + " sum(block_cache_hit) as blockCacheHit, sum(disk_reads) as diskReads, "
                    + " max(disk_reads) as maxDiskReads, sum(retry_cnt) as retryCount, "
                    + " sum(table_scan) as tableScans, sum(consistency_level_strong) as strongConsistencyCount, "
                    + " sum(consistency_level_weak) as weakConsistencyCount, sum(memstore_read_row_count) as memstoreReadRows, "
                    + " sum(ssstore_read_row_count) as ssstoreReadRows, count(*) as exec "
                    + " from "
                    + SQL_AUDIT_WITH_TEXT_STATEMENT;

    public static String WHERE_CONDITION_STATEMENT =
            " where tenant_id = ? and char_length(tenant_name) != 0 and (request_time + elapsed_time) >= ? and "
                    + "(request_time + elapsed_time) <= ? ";

    public static String SQL_SERVER_PORT_CONDITION_STATEMENT = " and svr_port = ? ";

    public static String SQL_SERVER_IP_CONDITION_STATEMENT = " and svr_ip = ? ";

    public static String HAVING_STATEMENT = " having 1 = 1 ";

    public static String SQL_INNER_CONDITION_STATEMENT = " and innerSqlCount = 0 ";

    public static String SQL_TEXT_LIKE_CONDITION_STATEMENT = " and statement like ? ";

    public static String ROWS_LIMIT_STATEMENT = " limit 1000 ";

    public static String GROUP_BY_CONDITION_STATEMENT = " group by tenant_id, db_id, sql_id";

    public static Integer SLOW_SQL_THRESHOLD = 100000;

    public static String SQL_AUDIT_SLOW_SQL_CONDITION_STATEMENT =
            " and (elapsed_time > ?) and char_length(tenant_name) != 0 ";

    public static String SELECT_SQL_TEXT_FROM_SQL_AUDIT =
            " select S.*, T.query_sql as sqlText, T.statement as statement from ( "
                    + " select tenant_id as obTenantId, tenant_name as tenantName, user_id as userId, user_name as userName, "
                    + " svr_ip as serverIp, svr_port as serverPort,"
                    + " db_id as obDbId, db_name as dbName, sql_id as sqlId from GV$OB_SQL_AUDIT "
                    + " where tenant_id = ? and db_id = ? and sql_id = ?) S "
                    + " join GV$OB_PLAN_CACHE_PLAN_STAT T "
                    + " on S.obTenantId = T.tenant_id and S.obDbId = db_id and S.sqlId = T.sql_id limit 1 ";

    public static String SELECT_SQL_TEXT_FROM_SQL_AUDIT_WITHOUT_DB =
            " select S.*, T.query_sql as sqlText, T.statement as statement from ( "
                    + " select tenant_id as obTenantId, tenant_name as tenantName, user_id as userId, user_name as userName, "
                    + " svr_ip as serverIp, svr_port as serverPort, "
                    + " db_id as obDbId, db_name as dbName, sql_id as sqlId from GV$OB_SQL_AUDIT "
                    + " where tenant_id = ? and sql_id = ?) S"
                    + " join GV$OB_PLAN_CACHE_PLAN_STAT T "
                    + " on S.obTenantId = T.tenant_id and S.sqlId = T.sql_id limit 1";

    public static String SELECT_TENANT_SLOW_SQL_COUNT =
            "select tenant_id as tenantId, tenant_Name as tenantName, count(distinct db_id, sql_id) as count "
                    + " from GV$OB_SQL_AUDIT where"
                    + " char_length(tenant_name) != 0 and elapsed_time > ? "
                    + " and (request_time + elapsed_time) > ? and (request_time + elapsed_time) < ?"
                    + " group by tenant_id, tenant_name"
                    + " order by count desc limit ?";

    private final ObConnectTemplate connectTemplate;

    public SqlAuditOperatorImpl(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<SqlAuditRawStatEntity> queryTopSql(QueryTopSqlRawStatParam query) {
        List<Object> params = new ArrayList<>();
        String sql = buildQuerySqlAuditRawStatement(params, query);
        Object[] array = params.toArray();
        return connectTemplate.query(sql, array, new BeanPropertyRowMapper<>(SqlAuditRawStatEntity.class));
    }

    @Override
    public List<SqlAuditRawStatEntity> querySlowSqlRawStat(QueryTopSqlRawStatParam query) {
        List<Object> params = new ArrayList<>();
        String sql = buildQuerySlowSqlAuditRawStatement(params, query);
        Object[] array = params.toArray();
        return connectTemplate.query(sql, array, new BeanPropertyRowMapper<>(SqlAuditRawStatEntity.class));
    }

    public String buildQuerySqlAuditRawStatement(List<Object> params, QueryTopSqlRawStatParam query) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT_SQL_AUDIT_STATEMENT).append(WHERE_CONDITION_STATEMENT);
        params.add(query.getObTenantId());
        params.add(query.getStartTimeUs());
        params.add(query.getEndTimeUs());
        if (StringUtils.isNotEmpty(query.getServerIp()) && query.getServerPort() >= 0) {
            sqlBuilder.append(SQL_SERVER_IP_CONDITION_STATEMENT);
            params.add(query.getServerIp());
            sqlBuilder.append(SQL_SERVER_PORT_CONDITION_STATEMENT);
            params.add(query.getServerPort());
        }
        sqlBuilder.append(JOIN_PLAN_STAT_STATEMENT).append(GROUP_BY_CONDITION_STATEMENT).append(HAVING_STATEMENT);
        if (!query.includeInner) {
            sqlBuilder.append(SQL_INNER_CONDITION_STATEMENT);
        }
        if (StringUtils.isNotEmpty(query.getSqlText())) {
            sqlBuilder.append(SQL_TEXT_LIKE_CONDITION_STATEMENT);
            params.add("%" + query.getSqlText() + "%");
        }
        sqlBuilder.append(ROWS_LIMIT_STATEMENT);
        return sqlBuilder.toString();
    }

    public String buildQuerySlowSqlAuditRawStatement(List<Object> params, QueryTopSqlRawStatParam query) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT_SQL_AUDIT_STATEMENT).append(WHERE_CONDITION_STATEMENT)
                .append(SQL_AUDIT_SLOW_SQL_CONDITION_STATEMENT);
        params.add(query.getObTenantId());
        params.add(query.getStartTimeUs());
        params.add(query.getEndTimeUs());
        params.add(SLOW_SQL_THRESHOLD);
        if (StringUtils.isNotEmpty(query.getServerIp()) && query.getServerPort() >= 0) {
            sqlBuilder.append(SQL_SERVER_IP_CONDITION_STATEMENT);
            params.add(query.getServerIp());
            sqlBuilder.append(SQL_SERVER_PORT_CONDITION_STATEMENT);
            params.add(query.getServerPort());
        }
        sqlBuilder.append(JOIN_PLAN_STAT_STATEMENT).append(GROUP_BY_CONDITION_STATEMENT).append(HAVING_STATEMENT);
        if (!query.includeInner) {
            sqlBuilder.append(SQL_INNER_CONDITION_STATEMENT);
        }
        if (StringUtils.isNotEmpty(query.getSqlText())) {
            sqlBuilder.append(SQL_TEXT_LIKE_CONDITION_STATEMENT);
            params.add("%" + query.getSqlText() + "%");
        }
        sqlBuilder.append(ROWS_LIMIT_STATEMENT);
        return sqlBuilder.toString();
    }

    @Override
    public SqlTextEntity querySqlTextAny(QuerySqlTextAny querySqlTextAny) {
        SqlTextEntity entity = new SqlTextEntity();
        try {
            String sql = SELECT_SQL_TEXT_FROM_SQL_AUDIT;
            List<Object> params = new ArrayList<>();
            params.add(querySqlTextAny.getObTenantId());
            params.add(querySqlTextAny.getObDbId());
            params.add(querySqlTextAny.sqlId);
            Object[] array = params.toArray();
            entity = connectTemplate.queryForObject(sql, array, new BeanPropertyRowMapper<>(SqlTextEntity.class));
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
        return entity;
    }

    @Override
    public List<TenantSlowSqlCounter> getTopSlowSql(QuerySlowSqlRankParam param) {
        String sql = SELECT_TENANT_SLOW_SQL_COUNT;
        Object[] array = new Object[] {SLOW_SQL_THRESHOLD, TimeUtils.toUs(param.getStartTime()),
                TimeUtils.toUs(param.getEndTime()), param.getTop()};
        return connectTemplate.query(sql, array, new BeanPropertyRowMapper<>(TenantSlowSqlCounter.class));
    }

    @Override
    public SqlTextEntity querySqlTextAnyWithoutDbId(QuerySqlTextAny querySqlTextAny) {
        SqlTextEntity entity = new SqlTextEntity();
        try {
            String sql = SELECT_SQL_TEXT_FROM_SQL_AUDIT_WITHOUT_DB;
            List<Object> params = new ArrayList<>();
            params.add(querySqlTextAny.getObTenantId());
            params.add(querySqlTextAny.getObDbId());
            params.add(querySqlTextAny.sqlId);
            Object[] array = params.toArray();
            entity = connectTemplate.queryForObject(sql, array, new BeanPropertyRowMapper<>(SqlTextEntity.class));
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
        return entity;
    }

}
