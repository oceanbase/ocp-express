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

package com.oceanbase.ocp.obops.tenant.model;

import java.time.OffsetDateTime;

import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Deprecated
    private Long id;

    private String name;

    private Long obTenantId;

    private TenantMode mode;

    private String clusterName;

    private Long obClusterId;

    private String obVersion;

    private OffsetDateTime createTime;

    private String primaryZone;

    private String zoneList;

    private String locality;

    private TenantStatus status;

    private Boolean locked;

    private Boolean readonly;

    private String description;
}
