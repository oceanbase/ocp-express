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

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.SqlPlanOperator;
import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryPlanRawStatBySql;

public class SqlPlanOperatorImpl implements SqlPlanOperator {

    private final ObConnectTemplate connectTemplate;

    public SqlPlanOperatorImpl(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }


    public static String SELECT_SQL_PLAN_AFTER_V4 = "SELECT T.*, S.id as obServerId from  ( "
            + " SELECT "
            + " tenant_id as obTenantId, db_id as obDbId, plan_id as planId, outline_id as outLineId,  svr_ip as serverIp, svr_port as serverPort, "
            + "avg_exe_usec as avgExecUs, slowest_exe_usec as slowestExeTimeUs, slow_count as slowCount, hit_count as hitCount, "
            + " executions , disk_reads as diskReads, direct_writes as directWrites, buffer_gets as bufferGets, "
            + " application_wait_time as applicationWaitTimeUs, concurrency_wait_time as concurrencyWaitTimeUs, user_io_wait_time as userIoWaitTimeUs, "
            + " rows_processed as rowsProcessed, elapsed_time as elapsedTimeUs, cpu_time as cpuTimeUs, large_querys as large, "
            + " delayed_large_querys as largeQuery, table_scan as tableScan, timeout_count as timeOutCount, "
            + " time_to_usec(first_load_time) as firstLoadTimeUs, sql_id as sqlId, type as plan_type, plan_hash as planHash, schema_version as schemaVersion, "
            + " plan_size as planSize, outline_data as outLineData, query_sql as querySql"
            + " from GV$OB_PLAN_CACHE_PLAN_STAT where tenant_id = ? and db_id = ? and sql_id = ? and object_type = 'sql_plan' ) T"
            + " join DBA_OB_SERVERS S on T.serverIp = S.svr_ip and T.serverPort = S.svr_port ";

    @Override
    public List<PlanRawStatEntity> queryPlanRawStat(QueryPlanRawStatBySql query) {
        String sql = SELECT_SQL_PLAN_AFTER_V4;
        List<Object> params = new ArrayList<>();
        params.add(query.getObTenantId());
        params.add(query.getObDbId());
        params.add(query.getSqlId());
        Object[] array = params.toArray();
        return connectTemplate.query(sql, array, new BeanPropertyRowMapper<>(PlanRawStatEntity.class));
    }
}
