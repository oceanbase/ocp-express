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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetSuperUserPasswordTask implements Subtask {

    @Getter
    private int waitTimes = 60;

    @Getter
    private int waitPeriod = 5;

    @Override
    public String getName() {
        return "Set super user password";
    }

    @Override
    public int getTimeout() {
        return 300;
    }

    @Override
    public void cancel() {}

    @Override
    public Context run(Context context) {
        String tenantName = context.get(ContextKey.TENANT_NAME);
        TenantMode tenantMode = TenantMode.fromValue(context.get(ContextKey.TENANT_MODE));
        String oldPassword = context.get(ContextKey.OLD_PASSWORD.getValue());
        String newPassword = context.get(ContextKey.NEW_PASSWORD.getValue());
        Validate.notEmpty(tenantName);
        Validate.notNull(tenantMode);
        Validate.notNull(oldPassword);
        Validate.notNull(newPassword);

        log.info("begin to set super user password, tenantName={}, tenantMode={}", tenantName, tenantMode);

        ObAccessorFactory accessorFactory = BeanUtils.getBean(ObAccessorFactory.class);

        boolean success = false;
        for (int i = 0; i < waitTimes; i++) {
            waitFor(waitPeriod);
            try {
                ObAccessor accessor = accessorFactory.createObAccessor(tenantName, tenantMode, oldPassword);
                accessor.user().alterSuperPassword(newPassword);
                success = true;
                break;
            } catch (RuntimeException e) {
                log.warn("set super user password failed, error message:{}", e.getMessage());
            }
        }

        ExceptionUtils.unExpected(success, ErrorCodes.OB_TENANT_SET_PASSWORD_FAILED, tenantName);

        log.info("set super user password success");
        return context;
    }

    @Override
    public Context rollback(Context context) {
        log.info("do nothing when roll back");
        return context;
    }

    private void waitFor(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (Exception e) {
            log.error("Failed to wait {} seconds", seconds);
        }
    }
}
