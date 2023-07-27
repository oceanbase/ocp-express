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
package com.oceanbase.ocp.task.engine.manager;

import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.dao.SubtaskInstanceRepository;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;
import com.oceanbase.ocp.task.model.SubtaskInstance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubtaskInstanceManagerImpl implements SubtaskInstanceManager {

    private final SubtaskInstanceRepository subtaskInstanceRepository;

    public SubtaskInstanceManagerImpl(SubtaskInstanceRepository subtaskInstanceRepository) {
        this.subtaskInstanceRepository = subtaskInstanceRepository;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public SubtaskInstance retrySubtask(long id) {
        SubtaskInstanceEntity entity = subtaskInstanceRepository.lockAndFindById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.TASK_NOT_EXISTS));
        ExceptionUtils.illegalArgs(entity.getState() == SubtaskState.FAILED, ErrorCodes.TASK_STATE_INVALID,
                id, entity.getState());
        ExceptionUtils.illegalArgs(entity.getOperation() != SubtaskOperation.ROLLBACK, ErrorCodes.TASK_STATE_INVALID,
                id, entity.getOperation());
        log.info("Retry subtask, taskId={}, name={}", id, entity.getName());
        entity.getTaskInstance().setState(TaskState.RUNNING);
        entity.setState(SubtaskState.READY);
        entity.setOperation(SubtaskOperation.RETRY);
        return SubtaskInstance.fromEntity(subtaskInstanceRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void skipSubtask(long id) {
        SubtaskInstanceEntity entity = subtaskInstanceRepository.lockAndFindById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.TASK_NOT_EXISTS));
        ExceptionUtils.illegalArgs(entity.getState() == SubtaskState.FAILED, ErrorCodes.TASK_STATE_INVALID,
                id, entity.getState());
        log.info("Skip subtask, taskId={}, taskOperation={}, name={}",
                id, entity.getTaskInstance().getOperation(), entity.getName());
        entity.getTaskInstance().setState(TaskState.RUNNING);
        entity.setState(SubtaskState.READY);
        if (entity.getTaskInstance().getOperation() == TaskOperation.ROLLBACK) {
            entity.setOperation(SubtaskOperation.ROLLBACK_SKIP);
        } else {
            entity.setOperation(SubtaskOperation.SKIP);
        }
        subtaskInstanceRepository.saveAndFlush(entity);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void cancelSubtask(long id) {
        SubtaskInstanceEntity entity = nullSafeGetById(id);
        ExceptionUtils.illegalArgs(entity.getState() == SubtaskState.RUNNING, ErrorCodes.TASK_STATE_INVALID,
                id, entity.getState());
        log.info("Cancel subtask, taskId={}, name={}", id, entity.getName());
        entity.setState(SubtaskState.CANCELING);
        subtaskInstanceRepository.saveAndFlush(entity);
    }

    private SubtaskInstanceEntity nullSafeGetById(long id) {
        return subtaskInstanceRepository.lockAndFindById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.TASK_NOT_EXISTS));
    }

}
