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
package com.oceanbase.ocp.task.engine.coordinator.worker;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.PessimisticLockException;
import org.springframework.dao.ConcurrencyFailureException;

import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.dao.TaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceRepository;
import com.oceanbase.ocp.task.engine.config.TaskCoordinatorConfig;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;
import com.oceanbase.ocp.task.entity.TaskInstanceEntity;
import com.oceanbase.ocp.task.entity.TaskInstanceOverview;
import com.oceanbase.ocp.task.hook.TaskHook;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunningTaskWorker extends AbstractTaskWorker {

    private static final String NAME = "running_task_worker";

    private final TaskInstanceRepository taskInstanceRepository;
    private final TaskInstanceOverviewRepository taskOverviewRepo;
    private final List<TaskHook<TaskInstanceOverview>> postTaskHooks;

    public RunningTaskWorker(TaskCoordinatorConfig c) {
        super(c);
        this.taskInstanceRepository = c.getTaskInstanceRepository();
        this.taskOverviewRepo = c.getTaskOverviewRepo();
        this.postTaskHooks = c.getPostTaskHooks();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String metricName() {
        return "ocp_task_running_task_worker_duration";
    }

    @Override
    public void work() {
        Set<Long> allByState = taskInstanceRepository.findAllIdByState(TaskState.RUNNING);

        for (Long taskInstanceId : allByState) {
            try {
                lockAndScheduleRunningTask(taskInstanceId);
            } catch (Throwable throwable) {
                log.warn("Handle running task, taskId={}, msg={}", taskInstanceId, throwable.getMessage());
            }
        }
    }

    private void lockAndScheduleRunningTask(long taskInstanceId) {
        Optional<TaskInstanceEntity> entityOpt = taskInstanceRepository.findById(taskInstanceId);
        if (!entityOpt.isPresent()) {
            return;
        }
        TaskState newState = TaskState.RUNNING;
        try {
            // Any subtask failed, then task is failed.
            if (entityOpt.get().getSubtasks().stream().anyMatch(i -> i.getState() == SubtaskState.FAILED)) {
                newState = markFailedTask(entityOpt.get());
            } else {
                newState = markRunningTask(entityOpt.get());
            }
        } catch (ConcurrencyFailureException | PessimisticLockException ignore) {
            log.info("Optimistic locking, taskInstanceId={}", taskInstanceId);
        } catch (Exception e) {
            newState = TaskState.FAILED;
            transactionWithoutResult(t -> taskOverviewRepo.updateStateAndEndTimeById(taskInstanceId, TaskState.FAILED,
                    OffsetDateTime.now()));
            log.warn("Mark task state failed, taskInstanceId=" + taskInstanceId, e);
        } finally {
            if (newState != TaskState.RUNNING) {
                executePostTaskHooks(taskOverviewRepo.findById(taskInstanceId).get());
            }
        }
    }

    private TaskState markFailedTask(TaskInstanceEntity entity) {
        log.info("Mark failed task, entity={}", entity);
        Function<SubtaskInstanceEntity, Integer> retryCountFunc =
                item -> Optional.of(item)
                        .map(SubtaskInstanceEntity::getSubtaskInstance)
                        .map(Subtask::getRetryCount)
                        .orElse(0);
        boolean taskFailed = entity.getSubtasks().stream()
                .anyMatch(subtask -> subtask.isFailed()
                        && subtask.getRunTime() >= retryCountFunc.apply(subtask));
        if (taskFailed) {
            return updateState(entity);
        }
        return TaskState.RUNNING;
    }

    private TaskState markRunningTask(TaskInstanceEntity entity) {
        int concurrency = Optional.of(entity)
                .map(TaskInstanceEntity::getArgument)
                .map(Argument::getConcurrency)
                .orElse(-1);
        markSubtaskState(entity.getOperation(), entity.getSubtasks(), concurrency);
        return updateState(entity);
    }

    private void executePostTaskHooks(TaskInstanceOverview t) {
        if (CollectionUtils.isEmpty(postTaskHooks)) {
            return;
        }
        postTaskHooks.forEach(hook -> {
            try {
                hook.getConsumer().accept(t);
            } catch (Exception ex) {
                log.warn("Execute hook failed, ex:", ex);
            }
        });
    }

    public TaskState updateState(TaskInstanceEntity entity) {
        boolean isSuccessful = true;
        boolean isFailed = false;
        TaskOperation taskOperation = entity.getOperation();
        for (SubtaskInstanceEntity t : entity.getSubtasks()) {
            log.debug("Subtask {}, state: {}", t.getId(), t.getState());
            if (taskOperation == TaskOperation.ROLLBACK) {
                if (!t.isPending()) {
                    isSuccessful = false;
                }
            } else {
                if (!t.isSuccessful()) {
                    isSuccessful = false;
                }
            }

            if (t.isRunning()) {
                return TaskState.RUNNING;
            } else if (t.isFailed()) {
                isFailed = true;
            }
        }
        if (isFailed) {
            transactionWithoutResult(t -> taskOverviewRepo.updateStateAndEndTimeById(entity.getId(), TaskState.FAILED,
                    OffsetDateTime.now()));
            return TaskState.FAILED;
        }
        if (isSuccessful) {
            transactionWithoutResult(t -> taskOverviewRepo.updateStateAndEndTimeById(entity.getId(),
                    TaskState.SUCCESSFUL, OffsetDateTime.now()));
            return TaskState.SUCCESSFUL;
        }
        return TaskState.RUNNING;
    }

}
