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
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oceanbase.ocp.task.entity.SubtaskLogEntity;

public interface SubtaskLogRepository extends JpaRepository<SubtaskLogEntity, Long> {

    /**
     * Query subtask log by subtask-instance-id.
     *
     * @param subtaskInstanceId ID of subtask instance
     * @return subtask logs
     */
    List<SubtaskLogEntity> findAllBySubtaskInstanceId(long subtaskInstanceId);

    /**
     * Query subtask logs by subtask-instance-ids.
     *
     * @param subtaskInstanceIds ID of subtask instances
     * @return subtask logs
     */
    List<SubtaskLogEntity> findAllBySubtaskInstanceIdIn(Collection<Long> subtaskInstanceIds);

}
