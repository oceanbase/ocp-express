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

import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.internal.parameter.ObParameterService;
import com.oceanbase.ocp.obops.internal.tenant.util.TenantParameterUtils;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetObTenantParametersTask implements Subtask {

    @Override
    public String getName() {
        return "Set ob tenant parameters";
    }

    @Override
    public int getTimeout() {
        return 300;
    }

    @Override
    public Context run(Context c) {
        Long tenantId = c.getLong(ContextKey.OB_TENANT_ID);
        String obTenantParameters = c.get(ContextKey.OB_TENANT_PARAMETER_MAP);
        Validate.notNull(tenantId, "tenantId should not be null");
        Validate.notNull(obTenantParameters, "obTenantParameters should not be null");

        ObParameterService obParameterService = BeanUtils.getBean(ObParameterService.class);
        Map<String, String> parameters = TenantParameterUtils.contextStringToParams(obTenantParameters);
        obParameterService.setTenantParameters(tenantId, parameters);

        return c;
    }

    @Override
    public Context rollback(Context c) {
        log.info("do nothing when rollback");
        return c;
    }

    @Override
    public void cancel() {
        log.info("do nothing when cancel");
    }
}
