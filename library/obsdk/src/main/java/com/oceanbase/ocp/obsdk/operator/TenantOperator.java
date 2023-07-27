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

package com.oceanbase.ocp.obsdk.operator;

import java.util.List;
import java.util.Optional;

import com.oceanbase.ocp.obsdk.enums.RootServiceJobType;
import com.oceanbase.ocp.obsdk.operator.tenant.model.CreateTenantInput;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.obsdk.operator.tenant.model.TenantJobProgress;

public interface TenantOperator {

    /**
     * Create tenant
     *
     * @param input {@link CreateTenantInput}
     * @return {@link ObTenant}
     */
    ObTenant createTenant(CreateTenantInput input);

    /**
     * Query tenant
     *
     * @return List of {@link ObTenant}
     */
    List<ObTenant> listTenant();

    /**
     * Query specified tenant
     *
     * @param obTenantId tenant id
     * @return {@link ObTenant}
     */
    ObTenant getTenant(Long obTenantId);

    /**
     * Query specified tenant
     *
     * @param tenantName tenant name
     * @return {@link ObTenant}
     */
    ObTenant getTenant(String tenantName);

    /**
     * Delete tenant
     *
     * @param tenantName tenant name
     */
    void deleteTenant(String tenantName);

    /**
     * Lock tenant
     *
     * @param tenantName tenant name
     */
    void lockTenant(String tenantName);

    /**
     * Unlock tenant
     *
     * @param tenantName tenant name
     */
    void unlockTenant(String tenantName);

    /**
     * Get the whitelist of tenant
     *
     * @param obTenantId tenant id
     * @return whitelist
     */
    Optional<String> getWhitelist(Long obTenantId);

    /**
     * Modify the whitelist of tenant
     *
     * @param tenantName tenant name
     * @param whitelist whitelist
     */
    void modifyWhitelist(String tenantName, String whitelist);

    /**
     * Modify primary zone of tenant
     *
     * @param tenantName tenant name
     * @param primaryZone target primary zone
     */
    void modifyPrimaryZone(String tenantName, String primaryZone);

    /**
     * Modify locality of tenant
     *
     * @param tenantName tenant name
     * @param locality target locality
     */
    void modifyLocality(String tenantName, String locality);

    /**
     * Modify resource pool list of tenant
     *
     * @param tenantName tenant name
     * @param poolList target resource pool list
     */
    void modifyResourcePoolList(String tenantName, List<String> poolList);

    /**
     * Obtain the job progress of tenant
     *
     * @param type job type
     * @return {@link TenantJobProgress}
     */
    TenantJobProgress getJobProgress(Long obTenantId, RootServiceJobType type);
}
