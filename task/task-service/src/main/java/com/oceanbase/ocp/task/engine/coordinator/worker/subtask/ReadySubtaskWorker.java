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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import org.hibernate.PessimisticLockException;
import org.springframework.dao.ConcurrencyFailureException;

import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.dao.SubtaskAccessor;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.runner.RunnerFactory;
import com.oceanbase.ocp.task.engine.util.AsyncTimeout;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadySubtaskWorker extends TimeoutSubtaskWorker {

    private final SubtaskAccessor subtaskAccessor;
    private final RunnerFactory runnerFactory;

    public ReadySubtaskWorker(SubtaskCoordinatorConfig c, RunnerFactory runnerFactory) {
        super(c, runnerFactory);
        this.subtaskAccessor = c.getSubtaskAccessor();
        this.runnerFactory = runnerFactory;
    }

    @Override
    public String name() {
        return "ready_subtask_worker";
    }

    @Override
    public String metricName() {
        return "ocp_task_ready_subtask_worker_duration";
    }

    @Override
    public void work() {
        Set<Long> subtaskIds = getSubtaskIds(SubtaskState.READY, null);
        if (subtaskIds == null || subtaskIds.isEmpty()) {
            return;
        }
        for (Long subtaskId : subtaskIds) {
            try {
                handleReadySubtask(subtaskId);
            } catch (Throwable e) {
                log.warn("Schedule task failed, taskId={}, exception={}", subtaskId, e);
            }
        }
    }

    private void handleReadySubtask(long subtaskId) {
        if (!existIdleWorker(subtaskId)) {
            return;
        }
        SubtaskInstanceOverview entity = lockAndMarkSubtask(subtaskId);
        if (entity == null) {
            return;
        }
        Future<SubtaskInstanceOverview> future = submitTask(entity);

        // Registry timeout callback.
        registerSubtaskTimeoutCallback(entity, future);
    }

    private SubtaskInstanceOverview lockAndMarkSubtask(long subtaskId) {
        return transactionExec(t -> {
            try {
                SubtaskInstanceOverview subtask = getAndCheckSubtaskState(subtaskId, SubtaskState.READY);
                if (subtask == null) {
                    return null;
                }
                prepareContext(subtask);
                subtask.getContext().put(ContextKey.TASK_INSTANCE_ID,
                        String.valueOf(subtask.getTaskId()));
                subtask.getContext().put(ContextKey.LATEST_EXECUTION_START_TIME.getValue(),
                        OffsetDateTime.now().toString());
                subtask.setExecutor(getExecutor());
                return subtaskAccessor.setRunning(subtask);
            } catch (ConcurrencyFailureException | PessimisticLockException ignore) {
                log.info("Ready task locked by other instance, id={}", subtaskId);
                return null;
            }
        });
    }

    private Future<SubtaskInstanceOverview> submitTask(SubtaskInstanceOverview subtask) {
        log.info("Submit ready task, entity={}", subtask);
        BiFunction<TaskType, String, Callable<SubtaskInstanceOverview>> runFunc = (taskType, traceId) -> () -> {
            try {
                SubtaskInstanceOverview run = runnerFactory.run(taskType, traceId, subtask);
                if (!Thread.currentThread().isInterrupted()) {
                    return subtaskAccessor.setExecuteEnd(run);
                } else {
                    log.warn("Thread was interrupted, possibly due to execution timeout. start={}, timeout={}",
                            subtask.getStartTime(), subtask.getTimeout());
                    return subtaskAccessor.setFailed(run);
                }
            } catch (Throwable t1) {
                log.warn("Task execute failed.", t1);
                subtaskAccessor.setFailed(subtask);
            } finally {
                // Remove timeout callback whether SUCCESS or FAILED.
                AsyncTimeout.cancelScheduledTimeout(subtask.getId());
            }
            return subtask;
        };
        return submit(subtask.getTaskId(), runFunc);
    }

    private void registerSubtaskTimeoutCallback(SubtaskInstanceOverview overview,
            Future<SubtaskInstanceOverview> future) {
        new AsyncTimeout(overview.getId(), future, overview.getTimeout(), TimeUnit.SECONDS) {

            @Override
            public void callback() {
                if (future == null || future.isDone() || future.isCancelled()) {
                    return;
                }
                AtomicBoolean marked = new AtomicBoolean(false);

                transactionWithoutResult(status -> {
                    try {
                        SubtaskInstanceOverview subtask =
                                getAndCheckSubtaskState(overview.getId(), SubtaskState.RUNNING);
                        if (subtask != null) {
                            subtaskAccessor.setFailed(subtask);
                            marked.set(true);
                        }
                    } catch (ConcurrencyFailureException | PessimisticLockException e) {
                        log.info("Another task handled timeout event.");
                    } catch (Exception e) {
                        log.warn("Unexpected exception", e);
                    }
                });
                if (marked.get()) {
                    future.cancel(true);
                    long executedSeconds = getSubtaskExecutedSeconds(overview);
                    String logContent =
                            String.format("Receive timeout callback, id=%s, name=%s, elapsed=%s, timeout=%s",
                                    overview.getId(), overview.getName(), executedSeconds, overview.getTimeout());
                    timeout(overview, logContent);
                }
            }
        }.enter();
    }

}
