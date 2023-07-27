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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;

public interface SubtaskInstanceOverviewRepository extends JpaRepository<SubtaskInstanceOverview, Long> {

    List<SubtaskInstanceOverview> findAllByTaskIdIn(Collection<Long> taskIds);

    @Query(value = "SELECT u.id FROM SubtaskInstanceOverview u WHERE u.state = ?1")
    Set<Long> findAllIdByState(SubtaskState state);

    @Query(value = "SELECT u.id FROM SubtaskInstanceOverview u WHERE u.state = ?1 AND u.updateTime >= ?2")
    Set<Long> findAllIdByStateAndUpdateTimeGreaterThan(SubtaskState state, OffsetDateTime updateTime);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("UPDATE SubtaskInstanceOverview u SET u.state=?2 WHERE u.id IN (?1)")
    int updateStateByIdIn(Collection<Long> ids, SubtaskState state);

    @Query(value = "SELECT type FROM task_instance WHERE id = (SELECT task_id FROM subtask_instance WHERE id=?1)",
            nativeQuery = true)
    String getTaskTypeBySubtaskId(long subtaskId);

}
