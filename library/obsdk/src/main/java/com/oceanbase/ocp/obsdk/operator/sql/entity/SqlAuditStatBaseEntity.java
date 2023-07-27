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

package com.oceanbase.ocp.obsdk.operator.sql.entity;

import java.math.BigInteger;

import javax.persistence.MappedSuperclass;

import lombok.Data;

@Data
@MappedSuperclass
public abstract class SqlAuditStatBaseEntity {

    public Long exec;

    public Long affectedRows;

    public Long maxAffectedRows;

    public Long returnRows;

    public Long maxReturnRows;

    public Long partitionCount;

    public Long failCount;

    public Long retCode4012Count;

    public Long retCode4013Count;

    public Long retCode5001Count;

    public Long retCode5024Count;

    public Long retCode5167Count;

    public Long retCode5217Count;

    public Long retCode6002Count;

    public Long event0WaitTimeUs;

    public Long event1WaitTimeUs;

    public Long event2WaitTimeUs;

    public Long event3WaitTimeUs;

    public Long totalWaitTimeUs;

    public Long maxTotalWaitTimeUs;

    public Long totalWaits;

    public Long rpcCount;

    public Long localPlanCount;

    public Long remotePlanCount;

    public Long distPlanCount;

    public Long innerSqlCount;

    public Long executorRpcCount;

    public Long missPlanCount;

    public Long elapsedTimeUs;

    public Long maxElapsedTimeUs;

    public Long netTimeUs;

    public Long netWaitTimeUs;

    public Long queueTimeUs;

    public Long decodeTimeUs;

    public Long getPlanTimeUs;

    public Long executeTimeUs;

    public BigInteger cpuTimeUs;

    public BigInteger maxCpuTimeUs;

    public BigInteger applicationWaitTimeUs;

    public BigInteger maxApplicationWaitTimeUs;

    public BigInteger concurrencyWaitTimeUs;

    public BigInteger maxConcurrencyWaitTimeUs;

    public BigInteger userIoWaitTimeUs;

    public BigInteger maxUserIoWaitTimeUs;

    public BigInteger scheduleTimeUs;

    public Long rowCacheHit;

    public Long bloomFilterCacheHit;

    public Long blockCacheHit;

    public Long blockIndexCacheHit;

    public Long diskReads;

    public Long maxDiskReads;

    public Long retryCount;

    public Long tableScans;

    public Long strongConsistencyCount;

    public Long weakConsistencyCount;

    public Long memstoreReadRows;

    public Long ssstoreReadRows;

    public Long expectedWorkerCount;

    public Long usedWorkerCount;

    public Long batchTime;

    public Double missPlanPercentage;

    public Long missPlans;

}
