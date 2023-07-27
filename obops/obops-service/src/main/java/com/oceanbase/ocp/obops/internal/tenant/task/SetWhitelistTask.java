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

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.tenant.TenantWhitelistService;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetWhitelistTask implements Subtask {

    @Override
    public String getName() {
        return "Set whitelist";
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
        String whitelist = context.get(ContextKey.WHITELIST.getValue());
        Validate.notNull(tenantId);
        Validate.notNull(whitelist);

        log.info("begin to set whitelist, tenantId={}, whitelist={}", tenantId, whitelist);

        TenantWhitelistService tenantWhitelistService = BeanUtils.getBean(TenantWhitelistService.class);
        tenantWhitelistService.modifyWhitelist(tenantId, whitelist);

        log.info("set whitelist success");
        return context;
    }

    @Override
    public Context rollback(Context c) {
        log.info("do nothing when roll back");
        return c;
    }
}
