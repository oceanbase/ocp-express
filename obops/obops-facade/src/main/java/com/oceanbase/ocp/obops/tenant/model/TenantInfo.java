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

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantInfo extends Tenant {

    private String charset;

    private String collation;

    private List<TenantZone> zones;

    private String whitelist;

    private List<ObproxyAndConnectionString> obproxyAndConnectionStrings;

    public static TenantInfo fromTenant(Tenant tenant) {
        TenantInfo info = new TenantInfo();
        info.setId(tenant.getId());
        info.setName(tenant.getName());
        info.setObTenantId(tenant.getObTenantId());
        info.setMode(tenant.getMode());
        info.setClusterName(tenant.getClusterName());
        info.setObClusterId(tenant.getObClusterId());
        info.setObVersion(tenant.getObVersion());
        info.setCreateTime(tenant.getCreateTime());
        info.setPrimaryZone(tenant.getPrimaryZone());
        info.setZoneList(tenant.getZoneList());
        info.setLocality(tenant.getLocality());
        info.setStatus(tenant.getStatus());
        info.setLocked(tenant.getLocked());
        info.setReadonly(tenant.getReadonly());
        info.setDescription(tenant.getDescription());
        return info;
    }

}
