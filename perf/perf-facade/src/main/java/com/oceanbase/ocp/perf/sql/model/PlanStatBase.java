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

import java.math.BigInteger;

import lombok.Data;

@Data
public abstract class PlanStatBase {

    public Long hitCount;

    public Long executions;

    public Long timeoutCount = 0L;

    public Long diskReads;

    public Long directWrites;

    public Long bufferGets;

    public Long applicationWaitTime;

    public Long concurrencyWaitTime;

    public Long userIoWaitTime;

    public Long rowProcessed;

    public Long elapsedTime;

    public BigInteger cpuTime;

    public Long largeQuery;

    public Double delayedLargeQueryPercentage;

    public Double timeoutPercentage;

    public Boolean tableScan;
}
