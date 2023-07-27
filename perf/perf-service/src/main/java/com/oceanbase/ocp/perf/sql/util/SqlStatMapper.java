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

package com.oceanbase.ocp.perf.sql.util;

import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.avg2;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.avgPercent;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.avgUsTime;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.toDouble;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.usToMsRound2;

import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlAuditRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlTextEntity;
import com.oceanbase.ocp.perf.sql.model.PlanStatBase;
import com.oceanbase.ocp.perf.sql.model.SqlAuditStatBase;
import com.oceanbase.ocp.perf.sql.model.SqlText;

public class SqlStatMapper {

    public static void mapToModel(SqlAuditRawStatEntity entity, SqlAuditStatBase model) {
        model.setMaxElapsedTime(usToMsRound2(entity.maxElapsedTimeUs));
        model.setSumElapsedTime(usToMsRound2(entity.elapsedTimeUs));
        model.setMaxCpuTime(usToMsRound2(entity.maxCpuTimeUs));
        if (entity.exec == null) {
            return;
        }
        model.setExecutions(entity.exec.doubleValue());
        model.setAvgAffectedRows(avg2(entity.affectedRows, entity.exec));
        model.setMaxAffectedRows(toDouble(entity.maxAffectedRows));
        model.setAvgReturnRows(avg2(entity.returnRows, entity.exec));
        model.setMaxReturnRows(toDouble(entity.maxReturnRows));
        model.setAvgPartitionCount(avg2(entity.partitionCount, entity.exec));
        model.setFailCount(entity.failCount);
        model.setFailPercentage(avgPercent(entity.failCount, entity.exec));
        model.setRetCode4012Count(entity.retCode4012Count);
        model.setRetCode4013Count(entity.retCode4013Count);
        model.setRetCode5001Count(entity.retCode5001Count);
        model.setRetCode5024Count(entity.retCode5024Count);
        model.setRetCode5167Count(entity.retCode5167Count);
        model.setRetCode5217Count(entity.retCode5217Count);
        model.setRetCode6002Count(entity.retCode6002Count);
        model.setAvgWaitTime(avgUsTime(entity.totalWaitTimeUs, entity.exec));
        model.setSumWaitTime(usToMsRound2(entity.totalWaitTimeUs));
        model.setMaxWaitTime(usToMsRound2(entity.maxTotalWaitTimeUs));
        model.setAvgWaitCount(avg2(entity.totalWaits, entity.exec));
        model.setAvgRpcCount(avg2(entity.rpcCount, entity.exec));
        model.setLocalPlanPercentage(avgPercent(entity.localPlanCount, entity.exec));
        model.setRemotePlanPercentage(avgPercent(entity.remotePlanCount, entity.exec));
        model.setDistPlanPercentage(avgPercent(entity.distPlanCount, entity.exec));
        model.setAvgExecutorRpcCount(avg2(entity.executorRpcCount, entity.exec));
        model.setMissPlanPercentage(avgPercent(entity.missPlanCount, entity.exec));
        model.setAvgElapsedTime(avgUsTime(entity.elapsedTimeUs, entity.exec));
        model.setAvgNetTime(avgUsTime(entity.netTimeUs, entity.exec));
        model.setAvgNetWaitTime(avgUsTime(entity.netWaitTimeUs, entity.exec));
        model.setAvgQueueTime(avgUsTime(entity.queueTimeUs, entity.exec));
        model.setAvgDecodeTime(avgUsTime(entity.decodeTimeUs, entity.exec));
        model.setAvgGetPlanTime(avgUsTime(entity.getPlanTimeUs, entity.exec));
        model.setAvgExecuteTime(avgUsTime(entity.executeTimeUs, entity.exec));
        model.setAvgCpuTime(avgUsTime(entity.cpuTimeUs, entity.exec));
        model.setAvgApplicationWaitTime(avgUsTime(entity.applicationWaitTimeUs, entity.exec));
        model.setMaxApplicationWaitTime(usToMsRound2(entity.maxApplicationWaitTimeUs));
        model.setAvgConcurrencyWaitTime(avgUsTime(entity.concurrencyWaitTimeUs, entity.exec));
        model.setMaxConcurrencyWaitTime(usToMsRound2(entity.maxConcurrencyWaitTimeUs));
        model.setAvgUserIoWaitTime(avgUsTime(entity.userIoWaitTimeUs, entity.exec));
        model.setMaxUserIoWaitTime(usToMsRound2(entity.maxUserIoWaitTimeUs));
        model.setAvgScheduleTime(avgUsTime(entity.scheduleTimeUs, entity.exec));
        model.setAvgRowCacheHit(avg2(entity.rowCacheHit, entity.exec));
        model.setAvgBloomFilterCacheHit(avg2(entity.bloomFilterCacheHit, entity.exec));
        model.setAvgBlockCacheHit(avg2(entity.blockCacheHit, entity.exec));
        model.setAvgBlockIndexCacheHit(avg2(entity.blockIndexCacheHit, entity.exec));
        model.setAvgDiskReads(avg2(entity.diskReads, entity.exec));
        model.setMaxDiskReads(toDouble(entity.maxDiskReads));
        model.setRetryCount(entity.retryCount);
        model.setTableScanPercentage(avgPercent(entity.tableScans, entity.exec));
        model.setStrongConsistencyPercentage(avgPercent(entity.strongConsistencyCount, entity.exec));
        model.setWeakConsistencyPercentage(avgPercent(entity.weakConsistencyCount, entity.exec));
        model.setAvgMemstoreReadRows(avg2(entity.memstoreReadRows, entity.exec));
        model.setAvgSsstoreReadRows(avg2(entity.ssstoreReadRows, entity.exec));
        model.setAvgUsedWorkerCount(avg2(entity.usedWorkerCount, entity.exec));
        model.setAvgExpectedWorkerCount(avg2(entity.expectedWorkerCount, entity.exec));
    }

    public static SqlText mapToModel(SqlTextEntity entity) {
        SqlText sqltext = new SqlText();
        sqltext.setFulltext(entity.sqlText);
        sqltext.setUserName(entity.userName);
        sqltext.setStatement(entity.statement);
        return sqltext;
    }

    public static void mapToModel(PlanRawStatEntity a, PlanStatBase model) {
        model.executions = a.executions;
        model.diskReads = a.diskReads;
        model.timeoutCount = a.timeoutCount;
        model.directWrites = a.directWrites;
        model.bufferGets = a.bufferGets;
        model.applicationWaitTime = a.applicationWaitTimeUs;
        model.concurrencyWaitTime = a.concurrencyWaitTimeUs;
        model.userIoWaitTime = a.userIoWaitTimeUs;
        model.rowProcessed = a.rowsProcessed;
        model.elapsedTime = a.elapsedTimeUs;
        model.cpuTime = a.cpuTimeUs;
        model.largeQuery = a.largeQuery;
        model.delayedLargeQueryPercentage = 0D;
        model.timeoutPercentage = 0D;
        model.tableScan = a.tableScan;
    }
}
