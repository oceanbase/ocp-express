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

import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareAlterTenantTask implements Subtask {

    @Override
    public String getName() {
        return "Prepare alter tenant";
    }

    @Override
    public int getTimeout() {
        return 300;
    }

    @Override
    public void cancel() {}

    @Override
    public Context run(Context context) {
        Long tenantId = context.getLong(ContextKey.OB_TENANT_ID);

        log.info("prepare alter tenant, tenantId={}", tenantId);

        TenantDaoManager tenantDaoManager = BeanUtils.getBean(TenantDaoManager.class);
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        context.put(ContextKey.FORMER_TENANT_STATUS.getValue(), entity.getStatus().toString());

        tenantDaoManager.updateStatus(entity.getObTenantId(), TenantStatus.MODIFYING);

        context.put(ContextKey.TARGET_TENANT_STATUS.getValue(), TenantStatus.NORMAL.toString());

        log.info("update status finished");
        return context;
    }

    @Override
    public Context rollback(Context context) {
        Long tenantId = context.getLong(ContextKey.OB_TENANT_ID);

        log.info("begin to rollback, tenantId={}", tenantId);

        TenantDaoManager tenantDaoManager = BeanUtils.getBean(TenantDaoManager.class);
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        String formerStatus = context.get(ContextKey.FORMER_TENANT_STATUS.getValue());
        if (StringUtils.isNotEmpty(formerStatus)) {
            TenantStatus status = TenantStatus.fromValue(formerStatus);
            tenantDaoManager.updateStatus(entity.getObTenantId(), status);
        }
        log.info("rollback finished");
        return context;
    }
}
