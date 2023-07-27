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

package com.oceanbase.ocp.obops.tenant;

import java.util.List;

import com.oceanbase.ocp.obops.tenant.model.CheckTenantPasswordResult;
import com.oceanbase.ocp.obops.tenant.model.ResourcePool;
import com.oceanbase.ocp.obops.tenant.param.CreatePasswordInVaultParam;
import com.oceanbase.ocp.obops.tenant.param.CreateTenantParam;
import com.oceanbase.ocp.obops.tenant.param.TenantChangePasswordParam;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.task.model.TaskInstance;

public interface TenantOperationService {

    /**
     * Create tenant.
     *
     * @param param create tenant param
     * @return create tenant task instance
     */
    TaskInstance createTenant(CreateTenantParam param);

    List<ResourcePool> createResourcePoolForTenant(CreateTenantParam param);

    ObTenant createTenantOnResourcePool(CreateTenantParam param, List<ResourcePool> resourcePoolList);

    /**
     * Delete tenant.
     *
     * @param obTenantId ID of tenant
     */
    void deleteTenant(Long obTenantId);

    void deleteObTenant(String tenantName);

    /**
     * Lock tenant.
     */
    void lockTenant(Long tenantId);

    /**
     * Unlock tenant.
     */
    void unlockTenant(Long tenantId);

    /**
     * Modify zone priority of tenant.
     *
     * @param tenantId ID of tenant
     * @param primaryZone zone priority
     */
    void modifyPrimaryZone(Long tenantId, String primaryZone);

    /**
     * Modify superuser password of tenant.
     *
     * @param tenantId ID of tenant
     * @param param modify tenant password param
     */
    void changePassword(Long tenantId, TenantChangePasswordParam param);

    /**
     * Create or replace tenant password in Express Vault, throw exception if the
     * password is incorrect.
     *
     * @param param replace tenant password param
     */
    void createOrReplacePassword(CreatePasswordInVaultParam param);


    /**
     * Check the admin password of tenant.
     *
     * @param param tenant password param
     */
    CheckTenantPasswordResult checkTenantPassword(CreatePasswordInVaultParam param);
}
