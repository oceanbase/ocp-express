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

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oceanbase.ocp.core.ob.tenant.ObTenantCompactionEntity;

@Repository
public interface ObTenantCompactionRepository extends JpaRepository<ObTenantCompactionEntity, Long> {

    @Query(value = "SELECT * FROM ob_tenant_compaction WHERE ob_tenant_id = :obTenantId ORDER BY frozen_scn DESC LIMIT :times",
            nativeQuery = true)
    List<ObTenantCompactionEntity> listRecentCompactions(@Param("obTenantId") Long obTenantId,
            @Param("times") Integer times);
}
