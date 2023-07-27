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

package com.oceanbase.ocp.perf.sql.model;

import lombok.Data;

@Data
public abstract class SqlAuditStatBase {

    private Double executions;

    private Double execPs;

    private Double avgAffectedRows;

    private Double avgReturnRows;

    private Double avgPartitionCount;

    private Long failCount;

    private Double failPercentage;

    private Long retCode4012Count;

    private Long retCode4013Count;

    private Long retCode5001Count;

    private Long retCode5024Count;

    private Long retCode5167Count;

    private Long retCode5217Count;

    private Long retCode6002Count;

    private Double avgWaitTime;

    private Double sumWaitTime;

    private Double avgWaitCount;

    private Double avgRpcCount;

    private Double localPlanPercentage;

    private Double remotePlanPercentage;

    private Double distPlanPercentage;

    private Double avgElapsedTime;

    private Double maxElapsedTime;

    private Double sumElapsedTime;

    private Double avgCpuTime;

    private Double maxCpuTime;

    private Double avgNetTime;

    private Double avgNetWaitTime;

    private Double avgQueueTime;

    private Double avgDecodeTime;

    private Double avgGetPlanTime;

    private Double avgExecuteTime;

    private Double avgExecutorRpcCount;

    private Double missPlanPercentage;

    private Double avgApplicationWaitTime;

    private Double avgConcurrencyWaitTime;

    private Double avgUserIoWaitTime;

    private Double avgScheduleTime;

    private Double avgRowCacheHit;

    private Double avgBloomFilterCacheHit;

    private Double avgBlockCacheHit;

    private Double avgBlockIndexCacheHit;

    private Double avgDiskReads;

    private Long retryCount;

    private Double tableScanPercentage;

    private Double strongConsistencyPercentage;

    private Double weakConsistencyPercentage;

    private Double avgMemstoreReadRows;

    private Double avgSsstoreReadRows;

    private Double maxAffectedRows;

    private Double maxReturnRows;

    private Double maxWaitTime;

    private Double maxApplicationWaitTime;

    private Double maxConcurrencyWaitTime;

    private Double maxUserIoWaitTime;

    private Double maxDiskReads;

    private Double avgExpectedWorkerCount;

    private Double avgUsedWorkerCount;

    public Double getAvgLogicalReads() {
        return this.getAvgRowCacheHit() * 2 + this.getAvgBloomFilterCacheHit() * 2 + this.getAvgBlockCacheHit()
                + this.getAvgDiskReads();
    }

    public Long getSumLogicalReads() {
        return Math.round(this.getAvgLogicalReads() * this.getExecutions());
    }
}
