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
package com.oceanbase.ocp.monitor.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.monitor.constants.ExporterStatus;
import com.oceanbase.ocp.monitor.entity.ExporterAddressEntity;

public interface ExporterRepository extends JpaRepository<ExporterAddressEntity, Long> {

    List<ExporterAddressEntity> findAllByStatus(ExporterStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE ExporterAddressEntity e SET e.status=:status WHERE e.exporterUrl=:exporterUrl")
    int updateStatusByExporterUrl(@Param("exporterUrl") String exporterUrl, @Param("status") ExporterStatus status);

    int deleteAllByExporterUrlIn(Collection<String> urls);
}
