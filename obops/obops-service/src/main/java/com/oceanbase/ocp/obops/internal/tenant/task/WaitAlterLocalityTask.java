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

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.enums.RootServiceJobType;
import com.oceanbase.ocp.obsdk.operator.TenantOperator;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaitAlterLocalityTask extends AbstractWaitAlterTenantTask implements Subtask {

    @Getter
    private int waitTimes = 200;

    @Getter
    private int waitPeriod = 20;

    @Override
    public String getName() {
        return "Wait alter locality";
    }

    @Override
    public int getTimeout() {
        return 3600;
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public void checkAlterProgress(TenantOperator operator, ObTenantEntity entity) {
        cancelled.set(false);
        // alter locality
        boolean localityFinished = waitJobFinished(operator, entity.getObTenantId(),
                RootServiceJobType.ALTER_TENANT_LOCALITY, waitTimes, waitPeriod);
        ExceptionUtils.unExpected(localityFinished, ErrorCodes.OB_TENANT_ALTER_LOCALITY_NOT_FINISHED, entity.getName());
    }

}
