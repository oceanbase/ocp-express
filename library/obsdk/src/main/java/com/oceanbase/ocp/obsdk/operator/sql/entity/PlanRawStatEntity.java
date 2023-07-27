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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.oceanbase.ocp.obsdk.operator.sql.model.PlanUid;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryPlanRawStatBySql;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class PlanRawStatEntity {

    @JsonProperty("ObTenantId")
    public Long obTenantId;

    @JsonProperty("ObServerId")
    public Long obServerId;

    @JsonProperty("ServerIp")
    public String serverIp;

    @JsonProperty("ServerPort")
    public String serverPort;

    @JsonProperty("PlanId")
    public Long planId;

    @JsonProperty("FirstLoadTimeUs")
    public Long firstLoadTimeUs;

    @JsonProperty("SqlId")
    public String sqlId;

    @JsonProperty("AvgExecUs")
    public Long avgExecUs;

    @JsonProperty("SlowestExeTimeUs")
    public Long slowestExeTimeUs;

    @JsonProperty("SlowCount")
    public Long slowCount;

    @JsonProperty("HitCount")
    public Long hitCount;

    @JsonProperty("Executions")
    public Long executions;

    @JsonProperty("DiskReads")
    public Long diskReads;

    @JsonProperty("DirectWrites")
    public Long directWrites;

    @JsonProperty("BufferGets")
    public Long bufferGets;

    @JsonProperty("ApplicationWaitTimeUs")
    public Long applicationWaitTimeUs;

    @JsonProperty("ConcurrencyWaitTimeUs")
    public Long concurrencyWaitTimeUs;

    @JsonProperty("UserIoWaitTimeUs")
    public Long userIoWaitTimeUs;

    @JsonProperty("RowsProcessed")
    public Long rowsProcessed;

    @JsonProperty("ElapsedTimeUs")
    public Long elapsedTimeUs;

    @JsonProperty("CpuTimeUs")
    public BigInteger cpuTimeUs;

    @JsonProperty("LargeQuery")
    public Long largeQuery;

    @JsonProperty("DelayedLargeQuery")
    public Long delayedLargeQuery;

    @JsonProperty("TimeoutCount")
    public Long timeoutCount;

    @JsonProperty("TableScan")
    public Boolean tableScan;

    @JsonProperty("ObDbId")
    public Long obDbId;

    @JsonIgnore
    private PlanUid planUid;

    @JsonProperty("PlanUnionHash")
    public String planUnionHash;

    @JsonProperty("PlanHash")
    public String planHash;

    @JsonProperty("PlanSize")
    public Integer planSize;

    @JsonProperty("PlanHash")
    public Long schemaVersion;

    @JsonProperty("OutLineData")
    public String outLineData;

    @JsonProperty("SchemaVersion")
    public Long mergedVersion;

    @JsonProperty("OutLineId")
    public Long outlineId;

    @JsonProperty("PlanType")
    public Integer planType;



    @JsonIgnore
    public PlanUid getUid() {
        if (planUid == null) {
            planUid = PlanUid.of(obServerId, planId, firstLoadTimeUs);
        }
        return planUid;
    }

    public boolean isValid(QueryPlanRawStatBySql query) {
        if (obServerId == null || planId == null) {
            log.warn("topPlan dirty data: {}", this);
            return false;
        }
        return true;
    }

}
