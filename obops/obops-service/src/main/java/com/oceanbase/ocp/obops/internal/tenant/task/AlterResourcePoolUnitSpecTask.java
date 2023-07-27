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

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.tenant.ResourcePoolService;
import com.oceanbase.ocp.obops.tenant.param.UnitSpecParam;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;
import com.oceanbase.ocp.task.util.ContextUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlterResourcePoolUnitSpecTask implements Subtask {

    @Override
    public String getName() {
        return "Alter resource pool unit spec";
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
        String zoneName = context.getParallelValue(ContextKey.ZONE_NAMES.getValue());
        Validate.notNull(tenantId);
        Validate.notNull(zoneName);
        String unitSpecJson = ContextUtils.getContextParallelValue(context, ContextKey.UNIT_SPEC_JSON_LIST);
        UnitSpecParam unitSpec = null;
        if (unitSpecJson != null) {
            unitSpec = JsonUtils.fromJson(unitSpecJson, UnitSpecParam.class);
        }

        log.info("begin to alter resource pool unit spec, tenantId={}, zoneName={}, unitSpec={}", tenantId,
                zoneName, unitSpec);

        ResourcePoolService resourcePoolService = BeanUtils.getBean(ResourcePoolService.class);
        if (unitSpec != null) {
            resourcePoolService.modifyResourcePool(tenantId, zoneName, unitSpec);
        }

        log.info("alter resource pool unit spec finished");
        return context;
    }

    @Override
    public Context rollback(Context c) {
        log.info("do nothing when rollback");
        return c;
    }
}
