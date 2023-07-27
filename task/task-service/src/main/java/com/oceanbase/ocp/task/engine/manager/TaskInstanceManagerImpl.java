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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.task.TaskInstanceManager;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.dao.TaskInstanceRepository;
import com.oceanbase.ocp.task.engine.coordinator.TaskCoordinator;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;
import com.oceanbase.ocp.task.entity.TaskInstanceEntity;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.model.SubtaskInstance;
import com.oceanbase.ocp.task.model.TaskInstance;
import com.oceanbase.ocp.task.runtime.Template;

public class TaskInstanceManagerImpl implements TaskInstanceManager {

    private final TaskInstanceRepository taskInstanceRepository;
    private final TaskCoordinator taskCoordinator;
    private final Supplier<String> creatorSupplier;

    public TaskInstanceManagerImpl(TaskInstanceRepository taskInstanceRepository, TaskCoordinator taskCoordinator,
            Supplier<String> creatorSupplier) {
        this.taskInstanceRepository = taskInstanceRepository;
        this.taskCoordinator = taskCoordinator;
        this.creatorSupplier = creatorSupplier;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void rollbackTask(long taskInstanceId) {
        TaskInstanceEntity entity = taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.TASK_NOT_EXISTS));

        ExceptionUtils.illegalArgs(entity.getState() == TaskState.FAILED, ErrorCodes.TASK_STATE_INVALID, taskInstanceId,
                entity.getState());

        if (prohibitRollback(entity)) {
            throw new IllegalArgumentException(ErrorCodes.TASK_PROHIBIT_ROLLBACK);
        }
        entity.setState(TaskState.RUNNING);
        entity.setOperation(TaskOperation.ROLLBACK);
        Set<SubtaskInstanceEntity> tasks = entity.getSubtasks();
        Set<Long> pendingTasks = tasks.stream()
                .filter(taskInstanceEntity -> taskInstanceEntity.getState() == SubtaskState.PENDING)
                .map(SubtaskInstanceEntity::getId)
                .collect(Collectors.toSet());
        for (SubtaskInstanceEntity taskEntity : tasks) {
            if (taskEntity.getState() == SubtaskState.PENDING) {
                continue;
            }

            taskEntity.setOperation(SubtaskOperation.ROLLBACK);

            List<Long> dependenciesIds = taskEntity.getDownstreams().stream()
                    .map(SubtaskInstanceEntity::getId)
                    .collect(Collectors.toList());
            boolean ready = dependenciesIds.isEmpty() || pendingTasks.containsAll(dependenciesIds);
            if (ready) {
                taskEntity.setState(SubtaskState.READY);
            }
        }
        taskInstanceRepository.saveAndFlush(entity);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public TaskInstance retryTask(long taskInstanceId) {
        TaskInstanceEntity entity = taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.TASK_NOT_EXISTS));
        ExceptionUtils.illegalArgs(entity.getState() == TaskState.FAILED, ErrorCodes.TASK_STATE_INVALID, taskInstanceId,
                entity.getState());
        ExceptionUtils.illegalArgs(entity.getOperation() != TaskOperation.ROLLBACK, ErrorCodes.TASK_STATE_INVALID,
                taskInstanceId, entity.getOperation());
        entity.setState(TaskState.RUNNING);
        entity.setOperation(TaskOperation.RETRY);
        Set<SubtaskInstanceEntity> tasks = entity.getSubtasks();
        for (SubtaskInstanceEntity taskEntity : tasks) {
            if (taskEntity.getState() == SubtaskState.SUCCESSFUL) {
                continue;
            }
            if (taskEntity.getState() == SubtaskState.FAILED) {
                taskEntity.setOperation(SubtaskOperation.RETRY);
                taskEntity.setState(SubtaskState.READY);
                taskEntity.setRunTime(taskEntity.getRunTime() + 1);
            }
        }
        return toDto(taskInstanceRepository.saveAndFlush(entity));
    }

    @Override
    public TaskInstance getTaskInstance(long taskInstanceId) {
        TaskInstanceEntity entity = taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.TASK_NOT_EXISTS));
        return toDto(entity);
    }

    @Override
    public TaskInstance submitManualTask(Template template, Argument argument) {
        return submitTask(template, argument);
    }

    private TaskInstance submitTask(Template template, Argument argument) {
        return toDto(taskCoordinator.submitTask(TaskType.MANUAL, template, argument, creatorSupplier.get()));
    }

    private TaskInstance toDto(TaskInstanceEntity entity) {
        if (entity == null) {
            return null;
        }
        return TaskInstance.builder()
                .id(entity.getId())
                .name(entity.getName())
                .taskDefinitionId(entity.getTaskDefinitionId())
                .status(entity.getState())
                .type(entity.getType())
                .obTenantId(entity.getObTenantId())
                .creator(entity.getCreator())
                .executor(entity.getExecutor())
                .context(entity.getArgument())
                .createTime(entity.getCreateTime())
                .startTime(entity.getStartTime())
                .finishTime(entity.getEndTime())
                .subtasks(Optional.ofNullable(entity.getSubtasks()).orElse(Collections.emptySet()).stream()
                        .map(SubtaskInstance::fromEntity)
                        .collect(Collectors.toSet()))
                .operation(entity.getOperation())
                .prohibitRollback(prohibitRollback(entity))
                .build();
    }

    private boolean prohibitRollback(TaskInstanceEntity entity) {
        // If any finished subtask prohibit rollback, then task is not allowed rollback.
        if (entity.getSubtasks() != null) {
            Predicate<SubtaskInstanceEntity> predicate =
                    t -> t.getState() != SubtaskState.PENDING && t.getState() != SubtaskState.READY
                            && t.prohibitRollback();
            if (entity.getSubtasks().stream().anyMatch(predicate)) {
                return true;
            }
        }
        return Optional.ofNullable(entity.getArgument())
                .map(context -> context.get(ContextKey.PROHIBIT_ROLLBACK))
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

}
