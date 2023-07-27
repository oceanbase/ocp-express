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

import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateTenantStatusTask implements Subtask {

    @Override
    public String getName() {
        return "Update tenant status";
    }

    @Override
    public int getTimeout() {
        return 300;
    }

    @Override
    public void cancel() {}

    @Override
    public Context run(Context context) {
        Long obTenantId = context.getLong(ContextKey.OB_TENANT_ID);
        String statusStr = context.get(ContextKey.TARGET_TENANT_STATUS.getValue());

        log.info("begin to update tenant status, obTenantId={}, targetTenantStatus={}", obTenantId, statusStr);

        TenantDaoManager tenantDaoManager = BeanUtils.getBean(TenantDaoManager.class);
        if (StringUtils.isNotBlank(statusStr)) {
            TenantStatus status = TenantStatus.fromValue(statusStr);
            tenantDaoManager.updateStatus(obTenantId, status);
        }

        log.info("update status task finished");
        return context;
    }

    @Override
    public Context rollback(Context c) {
        return c;
    }
}
