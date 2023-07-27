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
package com.oceanbase.ocp.task.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.oceanbase.ocp.task.constants.ScheduleType;
import com.oceanbase.ocp.task.entity.TaskDefinitionEntity;

public interface TaskDefinitionRepository extends JpaRepository<TaskDefinitionEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM TaskDefinitionEntity  u WHERE u.id = ?1")
    Optional<TaskDefinitionEntity> lockAndFindById(Long id);

    List<TaskDefinitionEntity> findAllByTemplateName(String templateName);

    List<TaskDefinitionEntity> findAllByTemplateNameIn(Collection<String> templateNames);

    /**
     * Update next scheduled time.
     */
    @Modifying
    @Query("UPDATE TaskDefinitionEntity u SET u.lastRunTime = ?2, u.nextRunTime = ?3 WHERE u.id = ?1")
    void updateRunTime(Long id, Date lastRunTime, Date nextRunTime);

    List<TaskDefinitionEntity> findAllByScheduleTypeAndEnabled(ScheduleType scheduleType, boolean enabled);

}
