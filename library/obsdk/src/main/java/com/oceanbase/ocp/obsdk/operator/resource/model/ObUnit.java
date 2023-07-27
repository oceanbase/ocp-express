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

package com.oceanbase.ocp.obsdk.operator.resource.model;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class ObUnit {

    private Long unitId;

    private Long resourcePoolId;

    private String zone;

    private String svrIp;

    private Integer svrPort;

    private String status;

    private ObReplicaType replicaType;

    private String resourcePoolName;

    private Timestamp resourcePoolUpdateTime;

    private ObUnitConfig obUnitConfig;

    private Long tenantId;

    private String tenantName;

    private String migrateFromSvrIp;

    private String migrateFromSvrPort;

    private Boolean manualMigrate;

    public boolean isMigrating() {
        return StringUtils.isNotEmpty(migrateFromSvrIp);
    }

    public boolean isManualMigrate() {
        return manualMigrate != null && manualMigrate;
    }

    public boolean unused() {
        return tenantId == null || tenantId == -1;
    }

    public boolean deletable(int maxReserveHour) {
        return unused()
                && null != resourcePoolUpdateTime
                && Instant.now().minus(Duration.ofHours(maxReserveHour)).isAfter(resourcePoolUpdateTime.toInstant());
    }
}
