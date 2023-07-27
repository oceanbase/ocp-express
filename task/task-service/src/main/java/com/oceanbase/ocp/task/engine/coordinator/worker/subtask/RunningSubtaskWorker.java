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
package com.oceanbase.ocp.task.engine.coordinator.worker.subtask;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.function.BiFunction;

import org.hibernate.PessimisticLockException;
import org.springframework.dao.ConcurrencyFailureException;

import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.dao.SubtaskAccessor;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.runner.RunnerFactory;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunningSubtaskWorker extends TimeoutSubtaskWorker {

    /**
     * Timeout offset to wait AsyncTimeout callback.
     */
    private static final long TIMEOUT_OFFSET = 10L;

    private final SubtaskAccessor subtaskAccessor;
    private final RunnerFactory runnerFactory;

    public RunningSubtaskWorker(SubtaskCoordinatorConfig c, RunnerFactory runnerFactory) {
        super(c, runnerFactory);
        this.subtaskAccessor = c.getSubtaskAccessor();
        this.runnerFactory = runnerFactory;
    }

    @Override
    public String name() {
        return "running_subtask_worker";
    }

    @Override
    public String metricName() {
        return "ocp_task_running_subtask_worker_duration";
    }

    @Override
    public void work() {
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(2);
        Set<Long> subtaskIds = getSubtaskIds(SubtaskState.RUNNING, startTime);
        if (subtaskIds == null || subtaskIds.isEmpty()) {
            return;
        }
        for (Long subtaskId : subtaskIds) {
            SubtaskInstanceOverview subtask = getAndCheckSubtaskState(subtaskId, SubtaskState.RUNNING);
            if (subtask == null) {
                continue;
            }
            try {
                handleTimeoutOrCancel(subtask);
            } catch (Exception e) {
                log.warn("Handle running subtask failed.", e);
                subtaskAccessor.setFailed(subtask);
            }
        }
    }

    private void handleTimeoutOrCancel(SubtaskInstanceOverview entity) {
        // Handle canceled subtask, mark task failed and send interrupt signal to
        // running thread.
        if (SubtaskOperation.CANCEL == entity.getOperation()) {
            SubtaskInstanceOverview markedSubtask = markSubtask(entity);
            if (markedSubtask == null) {
                return;
            }
            log.info("Running task canceled, taskId={}, name={}, operation={}",
                    markedSubtask.getId(), markedSubtask.getName(), markedSubtask.getOperation());
            BiFunction<TaskType, String, Runnable> runFunc =
                    (taskType, traceId) -> () -> runnerFactory.run(taskType, traceId, markedSubtask);
            execute(markedSubtask.getTaskId(), runFunc);
            return;
        }

        long executedSeconds = getSubtaskExecutedSeconds(entity);
        if (executedSeconds > (entity.getTimeout() + TIMEOUT_OFFSET)) {
            SubtaskInstanceOverview markedSubtask = markSubtask(entity);
            if (markedSubtask == null) {
                return;
            }
            String logContent = String.format("Subtask timeout, name=%s, executedSeconds=%s, timeout=%s",
                    entity.getName(), executedSeconds, entity.getTimeout());
            timeout(markedSubtask, logContent);
        }
    }

    private SubtaskInstanceOverview markSubtask(SubtaskInstanceOverview subtask) {
        return transactionExec(t -> {
            try {
                subtask.setExecutor(getExecutor());
                subtask.setState(SubtaskState.FAILED);
                return subtaskAccessor.setFailed(subtask);
            } catch (ConcurrencyFailureException | PessimisticLockException e) {
                log.info("Running task locked by other instance, id={}, name={}",
                        subtask.getId(), subtask.getName());
            }
            return null;
        });
    }

}
