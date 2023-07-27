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

package com.oceanbase.ocp.core.ob.tenant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantDaoManager {

    ObTenantEntity nullSafeGetObTenant(String tenantName);

    ObTenantEntity nullSafeGetObTenant(Long obTenantId);

    default ObTenantEntity nullSafeGetTenant(Long obTenantId) {
        return nullSafeGetObTenant(obTenantId);
    }

    default ObTenantEntity nullSafeGetTenant(String tenantName) {
        return nullSafeGetObTenant(tenantName);
    }

    Optional<ObTenantEntity> getTenant(String tenantName);

    Optional<ObTenantEntity> getTenant(Long obTenantId);

    /**
     * Check whether tenant exists.
     *
     * @param tenantName tenant name
     * @return true if tenant exists, false if not.
     */
    boolean isTenantExist(String tenantName);

    /**
     * Check whether tenant not exists.
     *
     * @param tenantName tenant name
     * @return false if tenant exists, true if not.
     */
    boolean isTenantNotExist(String tenantName);

    /**
     * Save OB tenant.
     *
     * @param entity Tenant entity {@link ObTenantEntity}
     * @return Tenant Entity {@link ObTenantEntity}
     */
    ObTenantEntity saveTenant(ObTenantEntity entity);

    /**
     * Delete OB tenant entity.
     *
     * @param entity {@link ObTenantEntity}
     */
    void deleteTenant(ObTenantEntity entity);

    /**
     * Update tenant name.
     *
     * @param obTenantId tenant id
     * @param name name of tenant
     * @return updated count
     */
    int updateName(Long obTenantId, String name);

    /**
     * Update OB tenant lock status.
     *
     * @param tenantId OB Tenant id
     * @param locked whether locked or not
     * @return updated row count
     */
    int updateLockStatus(Long tenantId, Boolean locked);

    /**
     * Update OB tenant readonly status.
     *
     * @param tenantId OB Tenant id
     * @param readonly true or false
     * @return updated row count
     */
    int updateReadonlyStatus(Long tenantId, Boolean readonly);

    /**
     * Update OB Zone primary.
     *
     * @param tenantId OB Tenant id
     * @param primaryZone primary zone info
     * @return updated row count
     */
    int updatePrimaryZone(Long tenantId, String primaryZone);

    /**
     * Update OB zone list.
     *
     * @param tenantId OB Tenant id
     * @param zoneListStr zone list
     * @return updated row count
     */
    int updateZoneList(Long tenantId, String zoneListStr);

    /**
     * Update OB zone locality.
     *
     * @param tenantId OB Tenant id
     * @param locality string of locality
     * @return updated row count
     */
    int updateLocality(Long tenantId, String locality);

    /**
     * Update OB tenant description.
     *
     * @param tenantId OB Tenant id
     * @param description string of description
     * @return updated row count
     */
    int updateDescription(Long tenantId, String description);

    /**
     * Update OB tenant status.
     *
     * @param obTenantId OB Tenant id
     * @param status {@link TenantStatus}
     * @return updated row count
     */
    int updateStatus(Long obTenantId, TenantStatus status);

    /**
     * Query OB tenant by specified parameters.
     *
     * @param queryParam {@link QueryTenantParam}
     * @param pageable {@link Pageable}
     * @return {@link Page} of {@link ObTenantEntity}
     */
    Page<ObTenantEntity> queryTenant(QueryTenantParam queryParam, Pageable pageable);

    /**
     * List all tenants.
     *
     * @return list of {@link ObTenantEntity}
     */
    List<ObTenantEntity> queryAllTenant();
}
