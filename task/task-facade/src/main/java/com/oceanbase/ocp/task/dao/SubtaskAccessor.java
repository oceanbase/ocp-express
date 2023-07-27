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

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;
import com.oceanbase.ocp.task.entity.SubtaskLogEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SubtaskAccessor {

    private final SubtaskInstanceOverviewRepository subtaskRepo;
    private final SubtaskLogRepo subtaskLogRepo;

    public SubtaskAccessor(SubtaskInstanceOverviewRepository subtaskRepo, SubtaskLogRepo subtaskLogRepo) {
        this.subtaskRepo = subtaskRepo;
        this.subtaskLogRepo = subtaskLogRepo;
    }

    public Set<Long> findAllIdByState(SubtaskState state) {
        return subtaskRepo.findAllIdByState(state);
    }

    public Set<Long> findAllByStateAndUpdateTimeGreaterThan(SubtaskState state, OffsetDateTime updateTime) {
        return subtaskRepo.findAllIdByStateAndUpdateTimeGreaterThan(state, updateTime);
    }

    public Optional<SubtaskInstanceOverview> findById(long subtaskId) {
        return subtaskRepo.findById(subtaskId);
    }

    public SubtaskInstanceOverview setRunning(SubtaskInstanceOverview overview) {
        // Set start time if subtask first time scheduled.
        if (overview.getStartTime() == null) {
            overview.setStartTime(OffsetDateTime.now());
        }
        overview.setEndTime(null);
        return setState(overview, SubtaskState.RUNNING);
    }

    public SubtaskInstanceOverview setReady(SubtaskInstanceOverview overview) {
        overview.setEndTime(null);
        return setState(overview, SubtaskState.READY);
    }

    public SubtaskInstanceOverview setFailed(SubtaskInstanceOverview overview) {
        overview.setEndTime(OffsetDateTime.now());
        return setState(overview, SubtaskState.FAILED);
    }

    public SubtaskInstanceOverview setExecuteEnd(SubtaskInstanceOverview overview) {
        overview.setEndTime(OffsetDateTime.now());
        if (overview.getState() == SubtaskState.FAILED) {
            return setState(overview, SubtaskState.FAILED);
        }
        if (overview.getOperation() == SubtaskOperation.ROLLBACK
                || overview.getOperation() == SubtaskOperation.ROLLBACK_SKIP) {
            return setState(overview, SubtaskState.PENDING);
        }
        return setState(overview, SubtaskState.SUCCESSFUL);
    }

    public TaskType getTaskTypeBySubtaskId(long subtaskId) {
        String typeStr = subtaskRepo.getTaskTypeBySubtaskId(subtaskId);
        return TaskType.fromValue(typeStr);
    }

    private SubtaskInstanceOverview setState(SubtaskInstanceOverview overview, SubtaskState state) {
        try {
            overview.setState(state);
            SubtaskInstanceOverview newOverview = subtaskRepo.saveAndFlush(overview);
            String message = String.format("Set state for subtask: %s, operation:%s, state: %s",
                    overview.getId(), overview.getOperation(), state.getValue());
            boolean isTerminateState = state.isTerminateState()
                    || (overview.getOperation() == SubtaskOperation.ROLLBACK && state == SubtaskState.PENDING);
            if (isTerminateState) {
                SubtaskLogEntity logEntity = new SubtaskLogEntity();
                logEntity.setSubtaskInstanceId(overview.getId());
                logEntity.setLogContent(message.getBytes(StandardCharsets.UTF_8));
                logEntity.setRunTime(overview.getRunTime());
                subtaskLogRepo.save(logEntity);
            }
            log.info(message);
            return newOverview;
        } catch (Throwable throwable) {
            log.info("Modify subtask state failed, error message={}", throwable.getMessage());
        }
        return overview;
    }

}
