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

package com.oceanbase.ocp.obops.internal.tenant.task;

import java.util.List;

import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.tenant.TenantCompactionCollectService;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectAllTenantCompactionTask implements Subtask {

    @Override
    public String getName() {
        return "Collect all tenants mini compactions";
    }

    @Override
    public int getTimeout() {
        return 600;
    }

    @Override
    public Context run(Context ctx) {
        log.info("Begin to collect all tenants major compaction info");
        TenantDaoManager tenantDaoManager = BeanUtils.getBean(TenantDaoManager.class);
        TenantCompactionCollectService compactionCollectService =
                BeanUtils.getBean(TenantCompactionCollectService.class);
        List<ObTenantEntity> tenants = tenantDaoManager.queryAllTenant();
        for (ObTenantEntity tenant : tenants) {
            try {
                compactionCollectService.collectMajorCompaction(tenant);
            } catch (Exception ex) {
                log.warn("Failed to collect major compaction for tenant:{}, errMsg:{}, cause:{}", tenant.getName(),
                        ex.getMessage(), ex.getCause());
            }
        }
        log.info("Finish to collect all tenants major compaction info");
        return ctx;
    }

    @Override
    public Context rollback(Context ctx) {
        return ctx;
    }

    @Override
    public void cancel() {

    }
}
