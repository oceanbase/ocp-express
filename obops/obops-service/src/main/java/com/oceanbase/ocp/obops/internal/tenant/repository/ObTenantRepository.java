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

package com.oceanbase.ocp.obops.internal.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;

public interface ObTenantRepository
        extends JpaRepository<ObTenantEntity, Long>, JpaSpecificationExecutor<ObTenantEntity> {

    Optional<ObTenantEntity> findByName(String name);

    Optional<ObTenantEntity> findByObTenantId(Long obTenantId);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.name = ?2 where e.obTenantId = ?1")
    int updateName(Long obTenantId, String name);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.locked = ?2 where e.obTenantId = ?1")
    int updateLockStatus(Long obTenantId, Boolean locked);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.readonly = ?2 where e.obTenantId = ?1")
    int updateReadonlyStatus(Long obTenantId, Boolean readonly);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.primaryZone = ?2 where e.obTenantId = ?1")
    int updatePrimaryZone(Long obTenantId, String primaryZone);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.zoneListStr = ?2 where e.obTenantId = ?1")
    int updateZoneList(Long obTenantId, String zoneListStr);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.locality = ?2 where e.obTenantId = ?1")
    int updateLocality(Long obTenantId, String locality);


    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.description = ?2 where e.obTenantId = ?1")
    int updateDescription(Long obTenantId, String description);


    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("update ObTenantEntity e set e.status = ?2 where e.obTenantId = ?1")
    int updateStatus(Long obTenantId, TenantStatus status);
}
