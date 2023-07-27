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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskConstants;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.dao.SubtaskAccessor;
import com.oceanbase.ocp.task.dao.SubtaskInstanceRepository;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.coordinator.worker.Worker;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;
import com.oceanbase.ocp.task.entity.TaskInstanceOverview;
import com.oceanbase.ocp.task.model.Context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSubtaskWorker implements Worker {

    private static final String EXECUTOR = HostUtils.getLocalIp();

    private final SubtaskInstanceRepository subtaskRepo;
    private final SubtaskAccessor subtaskAccessor;
    private final TransactionTemplate transactionTemplate;

    private static ThreadPoolExecutor runnerExecutor;
    private static ThreadPoolExecutor manualRunnerExecutor;

    private final Function<Long, TaskInstanceOverview> taskFunc;

    protected AbstractSubtaskWorker(SubtaskCoordinatorConfig c) {
        this.taskFunc = c.getTaskFunc();
        this.subtaskRepo = c.getSubtaskRepo();
        this.subtaskAccessor = c.getSubtaskAccessor();
        this.transactionTemplate = c.getTransactionTemplate();
    }

    public Set<Long> getSubtaskIds(SubtaskState state, OffsetDateTime updateTime) {
        Set<Long> allIds;
        if (updateTime == null) {
            allIds = subtaskAccessor.findAllIdByState(state);
        } else {
            allIds = subtaskAccessor.findAllByStateAndUpdateTimeGreaterThan(state, updateTime);
        }
        return allIds;
    }

    public static void setRunnerExecutor(ThreadPoolExecutor runnerExecutor) {
        AbstractSubtaskWorker.runnerExecutor = runnerExecutor;
    }

    public static void setManualRunnerExecutor(ThreadPoolExecutor manualRunnerExecutor) {
        AbstractSubtaskWorker.manualRunnerExecutor = manualRunnerExecutor;
    }

    public String getExecutor() {
        return EXECUTOR;
    }

    public void transactionWithoutResult(Consumer<TransactionStatus> action) {
        transactionTemplate.executeWithoutResult(action);
    }

    public void execute(TaskType type, Runnable runnable) {
        getThreadPool(type).execute(runnable);
    }

    public SubtaskInstanceOverview transactionExec(TransactionCallback<SubtaskInstanceOverview> callback) {
        return transactionTemplate.execute(callback);
    }

    public boolean existIdleWorker(long subtaskId) {
        // Just approximation.
        boolean scheduleRunnerFree = runnerExecutor.getCorePoolSize() - runnerExecutor.getActiveCount() > 0;
        boolean manualRunnerFree = manualRunnerExecutor.getCorePoolSize() - manualRunnerExecutor.getActiveCount() > 0;
        if (scheduleRunnerFree && manualRunnerFree) {
            return true;
        }
        log.info("Thread pool active count, manual={}, schedule={}",
                manualRunnerExecutor.getActiveCount(), runnerExecutor.getActiveCount());
        TaskType taskType = subtaskAccessor.getTaskTypeBySubtaskId(subtaskId);
        if (taskType == TaskType.MANUAL) {
            return manualRunnerExecutor.getCorePoolSize() - manualRunnerExecutor.getActiveCount() > 0;
        }
        return runnerExecutor.getCorePoolSize() - runnerExecutor.getActiveCount() > 0;
    }

    public Future<SubtaskInstanceOverview> submit(long taskId,
            BiFunction<TaskType, String, Callable<SubtaskInstanceOverview>> runFunc) {
        TaskInstanceOverview taskOverview = getTaskOverview(taskId);
        TaskType taskType = taskOverview.getType();
        String traceId = taskOverview.getTraceId();
        return getThreadPool(taskType).submit(runFunc.apply(taskType, traceId));
    }

    public void execute(Long taskId, BiFunction<TaskType, String, Runnable> runFunc) {
        TaskInstanceOverview taskOverview = getTaskOverview(taskId);
        TaskType taskType = taskOverview.getType();
        String traceId = taskOverview.getTraceId();
        getThreadPool(taskType).execute(runFunc.apply(taskType, traceId));
    }

    public void prepareContext(SubtaskInstanceOverview overview) {
        if (overview.getContext() != null) {
            log.debug("already generated context, no need to prepare context");
            return;
        }
        SubtaskInstanceEntity entity = subtaskRepo.findById(overview.getId())
                .orElseThrow(() -> new RuntimeException("Subtask not exist, id=" + overview.getId()));
        Context c = new Context();
        if (entity.getUpstreams().size() == 0) {
            log.debug("root node, use dag init context as context");
            c.merge(entity.getTaskInstance().getArgument());
        } else {
            log.debug("normal node, merge context from upstream tasks");
            for (SubtaskInstanceEntity t : entity.getUpstreams()) {
                c.merge(t.getContext());
            }
        }
        if (entity.getParallelIdx() != TaskConstants.NO_PARALLEL_IDX) {
            c.setParallelIdx(entity.getParallelIdx());
        }
        overview.setContext(c);
    }

    private TaskInstanceOverview getTaskOverview(long taskId) {
        TaskInstanceOverview task = taskFunc.apply(taskId);
        if (task == null) {
            throw new RuntimeException("Task not exist, taskId=" + taskId);
        }
        return task;
    }

    private ThreadPoolExecutor getThreadPool(TaskType type) {
        ThreadPoolExecutor runner;
        switch (type) {
            case MANUAL:
                runner = manualRunnerExecutor;
                break;
            case SCHEDULED:
            case SYS_SCHEDULED:
            default:
                runner = runnerExecutor;
        }
        if (runner == null) {
            throw new RuntimeException("Runner not init, try later, type=" + type);
        }
        return runner;
    }

    SubtaskInstanceOverview findById(long subtaskId) {
        return subtaskAccessor.findById(subtaskId).orElse(null);
    }

    SubtaskInstanceOverview getAndCheckSubtaskState(long subtaskInstanceId, SubtaskState expectedState) {
        Optional<SubtaskInstanceOverview> entityOpt = subtaskAccessor.findById(subtaskInstanceId);
        if (!entityOpt.isPresent()) {
            throw new RuntimeException("Subtask not exist, subtaskId=" + subtaskInstanceId);
        }
        if (entityOpt.get().getState() != expectedState) {
            return null;
        }
        return entityOpt.get();
    }

}
