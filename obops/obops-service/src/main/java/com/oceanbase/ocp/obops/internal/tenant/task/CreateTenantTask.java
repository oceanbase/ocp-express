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

import java.util.List;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.tenant.ResourcePoolService;
import com.oceanbase.ocp.obops.tenant.TenantOperationService;
import com.oceanbase.ocp.obops.tenant.model.ResourcePool;
import com.oceanbase.ocp.obops.tenant.param.CreateTenantParam;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

/**
 * Create ob tenant.
 * <p>
 * The existence of OB_TENANT_ID in Context means that tenant exists on the OB
 * side; The existence of RESOURCE_POOL_LIST_JSON in Context means that resource
 * pools exist on the OB side.
 */
@Slf4j
public class CreateTenantTask implements Subtask {

    @Override
    public String getName() {
        return "Create ob tenant";
    }

    @Override
    public int getTimeout() {
        return 1200;
    }

    @Override
    public void cancel() {}

    @Override
    public Context run(Context context) {
        TenantOperationService tenantOperationService = BeanUtils.getBean(TenantOperationService.class);
        TenantDaoManager tenantDaoManager = BeanUtils.getBean(TenantDaoManager.class);

        String tenantName = context.get(ContextKey.TENANT_NAME);

        String createTenantParamJson = context.get(ContextKey.CREATE_TENANT_PARAM_JSON);
        CreateTenantParam param = JsonUtils.fromJson(createTenantParamJson, CreateTenantParam.class);
        log.info("begin create tenant, param={}", param);

        // Create resource pools.
        List<ResourcePool> resourcePoolList = tenantOperationService.createResourcePoolForTenant(param);
        log.info("create resource pool success, resourcePoolList={}", resourcePoolList);
        context.put(ContextKey.RESOURCE_POOL_LIST_JSON, JsonUtils.toJsonString(resourcePoolList));

        // Create tenant.
        ObTenant obTenant = tenantOperationService.createTenantOnResourcePool(param, resourcePoolList);
        log.info("create ob tenant success, obTenant={}", obTenant);
        Long obTenantId = obTenant.getTenantId();
        context.put(ContextKey.OB_TENANT_ID, obTenantId);

        // Save obTenantId to metadb.
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(tenantName);
        tenantEntity.setObTenantId(obTenantId);
        log.info("save tenant success, entity={}", tenantEntity);
        tenantDaoManager.saveTenant(tenantEntity);

        return context;
    }

    @Override
    public Context rollback(Context context) {
        TenantOperationService tenantOperationService = BeanUtils.getBean(TenantOperationService.class);
        ResourcePoolService resourcePoolService = BeanUtils.getBean(ResourcePoolService.class);

        // If tenant exists, delete it.
        if (context.contains(ContextKey.OB_TENANT_ID)) {
            String tenantName = context.get(ContextKey.TENANT_NAME);
            Long obTenantId = context.getLong(ContextKey.OB_TENANT_ID);
            tenantOperationService.deleteObTenant(tenantName);
            log.info("rollback, delete ob tenant, tenantName={}, obTenantId={}", tenantName, obTenantId);
            context.remove(ContextKey.OB_TENANT_ID);
        }

        // If resource pools exist, delete them.
        if (context.contains(ContextKey.RESOURCE_POOL_LIST_JSON)) {
            String resourcePoolListJson = context.get(ContextKey.RESOURCE_POOL_LIST_JSON);
            List<ResourcePool> resourcePoolList = JsonUtils.fromJsonList(resourcePoolListJson, ResourcePool.class);
            resourcePoolService.deleteResourcePoolList(resourcePoolList);
            log.info("rollback, delete resource pool, resourcePoolList={}", resourcePoolList);
            context.remove(ContextKey.RESOURCE_POOL_LIST_JSON);
        }
        return context;
    }

    @Override
    public Context retry(Context context) {
        TenantOperationService tenantOperationService = BeanUtils.getBean(TenantOperationService.class);
        TenantDaoManager tenantDaoManager = BeanUtils.getBean(TenantDaoManager.class);

        String tenantName = context.get(ContextKey.TENANT_NAME);

        String createTenantParamJson = context.get(ContextKey.CREATE_TENANT_PARAM_JSON);
        CreateTenantParam param = JsonUtils.fromJson(createTenantParamJson, CreateTenantParam.class);
        log.info("retry create tenant, param={}", param);

        // If resource pools do not exist, recreate them.
        List<ResourcePool> resourcePoolList;
        if (context.contains(ContextKey.RESOURCE_POOL_LIST_JSON)) {
            String resourcePoolListJson = context.get(ContextKey.RESOURCE_POOL_LIST_JSON);
            resourcePoolList = JsonUtils.fromJsonList(resourcePoolListJson, ResourcePool.class);
            log.info("resource pool exists, resourcePoolList={}", resourcePoolList);
        } else {
            resourcePoolList = tenantOperationService.createResourcePoolForTenant(param);
            log.info("create resource pool success, resourcePoolList={}", resourcePoolList);
            context.put(ContextKey.RESOURCE_POOL_LIST_JSON, JsonUtils.toJsonString(resourcePoolList));
        }

        // If tenant does not exist, recreate it.
        Long obTenantId;
        if (context.contains(ContextKey.OB_TENANT_ID)) {
            obTenantId = context.getLong(ContextKey.OB_TENANT_ID);
            log.info("ob tenant exists, obTenantId={}", obTenantId);
        } else {
            ObTenant obTenant = tenantOperationService.createTenantOnResourcePool(param, resourcePoolList);
            log.info("create ob tenant success, obTenant={}", obTenant);
            obTenantId = obTenant.getTenantId();
            context.put(ContextKey.OB_TENANT_ID, obTenantId);
        }

        // Save obTenantId to metadb.
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(tenantName);
        tenantEntity.setObTenantId(obTenantId);
        tenantDaoManager.saveTenant(tenantEntity);
        log.info("save tenant success, entity={}", tenantEntity);
        context.put(ContextKey.OB_TENANT_ID, obTenantId);

        return context;
    }
}

