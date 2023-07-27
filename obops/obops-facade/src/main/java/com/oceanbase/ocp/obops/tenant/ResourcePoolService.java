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

package com.oceanbase.ocp.obops.tenant;

import java.util.List;

import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.obops.tenant.model.ResourcePool;
import com.oceanbase.ocp.obops.tenant.model.Unit;
import com.oceanbase.ocp.obops.tenant.param.CreateResourcePoolParam;
import com.oceanbase.ocp.obops.tenant.param.UnitSpecParam;

public interface ResourcePoolService {

    List<ResourcePool> createResourcePoolList(List<CreateResourcePoolParam> paramList);

    void deleteResourcePoolList(List<ResourcePool> poolList);

    ResourcePool addResourcePool(Long tenantId, CreateResourcePoolParam param);

    void reduceResourcePool(Long tenantId, String zoneName);

    List<ResourcePool> listResourcePool(ObTenantEntity entity);

    List<Unit> listUnit(Long obTenantId);

    void splitResourcePool(Long tenantId);

    void modifyResourcePool(Long tenantId, String zoneName, UnitSpecParam unitSpec);

    void modifyTenantUnitCount(Long tenantId, Long unitCount);
}
