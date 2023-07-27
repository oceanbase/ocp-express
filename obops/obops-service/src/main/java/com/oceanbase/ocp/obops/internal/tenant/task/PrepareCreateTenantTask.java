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

import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.tenant.TenantService;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareCreateTenantTask implements Subtask {

    @Override
    public String getName() {
        return "Prepare create tenant";
    }

    @Override
    public int getTimeout() {
        return 30;
    }

    @Override
    public Context run(Context c) {
        c.put(ContextKey.TARGET_TENANT_STATUS, TenantStatus.NORMAL.toString());
        return c;
    }

    @Override
    public Context rollback(Context c) {
        deleteTenant(c);
        return c;
    }

    private void deleteTenant(Context c) {
        TenantService tenantService = BeanUtils.getBean(TenantService.class);
        String tenantName = c.get(ContextKey.TENANT_NAME);
        try {
            tenantService.deleteTenantRelatedInfo(tenantName);
            tenantService.deleteTenantInfo(tenantName);
        } catch (Exception e) {
            log.warn("delete tenant failed, tenantName={}", tenantName, e);
        }
    }

    @Override
    public void cancel() {}
}
