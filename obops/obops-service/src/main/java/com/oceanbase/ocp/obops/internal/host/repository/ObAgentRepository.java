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
package com.oceanbase.ocp.obops.internal.host.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.core.agent.ObAgentEntity;
import com.oceanbase.ocp.core.constants.ObAgentOperation;

@Repository
public interface ObAgentRepository extends JpaRepository<ObAgentEntity, Long> {

    Optional<ObAgentEntity> findByIpAndMgrPort(String ip, Integer mgrPort);

    Optional<ObAgentEntity> findByIpAndObServerSvrPort(String ip, Integer obSvrPort);

    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "UPDATE ObAgentEntity e SET e.operation=:operation WHERE e.id=:id")
    void updateAgentOperation(@Param("id") Long obAgentId, @Param("operation") ObAgentOperation operation);
}
