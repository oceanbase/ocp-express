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

import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.tenant.ResourcePoolService;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteResourcePoolTask implements Subtask {

    @Override
    public String getName() {
        return "Delete resource pool";
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
        String zoneName = context.get(ContextKey.ZONE_NAME.getValue(), ContextKey.ZONE_NAMES.getValue());
        Validate.notNull(zoneName);

        log.info("begin to delete resource pool, tenantId={}, zoneName={}", tenantId, zoneName);

        ResourcePoolService resourcePoolService = BeanUtils.getBean(ResourcePoolService.class);

        try {
            resourcePoolService.reduceResourcePool(tenantId, zoneName);
            log.info("delete resource pool success, tenantId={}, zoneName={}", tenantId, zoneName);
        } catch (NotFoundException ex) {
            if (ErrorCodes.OB_TENANT_RESOURCE_POOL_NOT_FOUND.getCode() != ex.getErrorCode().getCode()) {
                throw ex;
            }
            log.info("resource pool not found, may have already been deleted");
        }

        return context;
    }

    @Override
    public Context rollback(Context c) {
        return c;
    }
}
