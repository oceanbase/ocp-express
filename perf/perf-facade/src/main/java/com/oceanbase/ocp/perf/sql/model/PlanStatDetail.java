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

import java.time.OffsetDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlanStatDetail extends PlanStatBase {

    public String uid;

    public String server;

    public Long planId;

    public OffsetDateTime firstLoadTime;

    public String planType;

    public String planHash;

    public String planUnionHash;

    public Integer planSize;

    public Long schemaVersion;

    public Long mergedVersion;

    public Long obServerId;

    public Long obDbId;

    public Long serverId;

    public String outlineData;

    public Long outlineId;

    public Long firstLoadTimeUs;

    public PlanExplain planExplain;

}


