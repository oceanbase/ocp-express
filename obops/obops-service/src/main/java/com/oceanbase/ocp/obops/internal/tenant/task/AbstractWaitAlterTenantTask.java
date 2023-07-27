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
import java.util.concurrent.atomic.AtomicBoolean;

import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obsdk.enums.RootServiceJobType;
import com.oceanbase.ocp.obsdk.operator.TenantOperator;
import com.oceanbase.ocp.obsdk.operator.tenant.model.TenantJobProgress;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractWaitAlterTenantTask implements Subtask {

    protected AtomicBoolean cancelled = new AtomicBoolean(false);

    abstract void checkAlterProgress(TenantOperator operator, ObTenantEntity entity);

    @Override
    public Context run(Context context) {
        Long tenantId = context.getLong(ContextKey.OB_TENANT_ID);

        TenantDaoManager tenantDaoManager = BeanUtils.getBean(TenantDaoManager.class);
        ObOperatorFactory operatorFactory = BeanUtils.getBean(ObOperatorFactory.class);
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        TenantOperator operator = operatorFactory.createObOperator().tenant();

        log.info("begin to wait alter tenant progress finish, tenant={}", entity.getName());

        checkAlterProgress(operator, entity);

        log.info("alter tenant progress finished");
        return context;
    }

    @Override
    public Context rollback(Context c) {
        log.info("do nothing when rollback");
        return c;
    }

    protected boolean waitJobFinished(TenantOperator operator, Long tenantId, RootServiceJobType type, int waitTimes,
            int waitPeriod) {
        log.info("begin to wait root service job finished, job type is {}", type);

        TenantJobProgress jobProgress = operator.getJobProgress(tenantId, type);
        if (jobProgress == null) {
            log.info("root service job not found");
            return true;
        }

        boolean finished = false;
        for (int i = 0; i < waitTimes; i++) {
            if (cancelled.get()) {
                throw new RuntimeException("task was cancelled");
            }
            jobProgress = operator.getJobProgress(tenantId, type);
            if (jobProgress.finished()) {
                log.info("root service job finished, job={}", jobProgress);
                finished = true;
                break;
            }
            log.info("root service job not finished, wait for another check, job={}", jobProgress);
            waitFor(waitPeriod);
        }
        return finished;
    }

    private void waitFor(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (Exception e) {
            log.error("Failed to wait {} seconds", seconds);
        }
    }
}
