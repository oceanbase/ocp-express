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

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.entity.TaskInstanceOverview;

public interface TaskInstanceOverviewRepository
        extends JpaRepository<TaskInstanceOverview, Long>, JpaSpecificationExecutor<TaskInstanceOverview> {

    @Modifying
    @Query(value = "UPDATE TaskInstanceOverview u set u.state=?2, u.endTime=?3 WHERE u.id = ?1")
    int updateStateAndEndTimeById(Long id, TaskState state, OffsetDateTime endTime);

    @Query(value = "SELECT DISTINCT u.creator FROM TaskInstanceOverview u WHERE u.type= ?1")
    List<String> findtDistinctCreator(TaskType taskType);

}
