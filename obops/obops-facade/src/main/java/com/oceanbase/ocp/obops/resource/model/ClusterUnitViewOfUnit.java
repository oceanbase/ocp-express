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

package com.oceanbase.ocp.obops.resource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterUnitViewOfUnit {

    private Long obUnitId;

    private Long obTenantId;

    private String tenantName;

    private String serverIp;

    private Integer serverPort;

    private String zone;

    private String region;

    private String unitConfig;

    private String unitConfigAliasName;

    private String resourcePoolName;

    private Double maxCpuAssignedCount;

    private Double minCpuAssignedCount;

    private Double maxMemoryAssignedByte;

    private Double minMemoryAssignedByte;

    private Long diskUsedByte;

    private String migrateSvrIp;

    private MigrateType migrateType;

    private Long timestamp;

    private Boolean manualMigrate;
}
