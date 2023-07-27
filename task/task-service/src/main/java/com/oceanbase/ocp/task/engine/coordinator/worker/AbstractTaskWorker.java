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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.dao.SubtaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.engine.config.TaskCoordinatorConfig;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;

public abstract class AbstractTaskWorker implements Worker {

    private final TransactionTemplate transactionTemplate;
    private final SubtaskInstanceOverviewRepository subtaskOverviewRepo;

    /**
     * Maximum concurrency for one task.
     */
    private static int defaultConcurrency = 10;

    protected AbstractTaskWorker(TaskCoordinatorConfig c) {
        this.transactionTemplate = c.getTransactionTemplate();
        this.subtaskOverviewRepo = c.getSubtaskOverviewRepo();
    }

    public static void setDefaultConcurrency(int defaultConcurrency) {
        AbstractTaskWorker.defaultConcurrency = defaultConcurrency;
    }

    public void markSubtaskState(TaskOperation operation, Collection<SubtaskInstanceEntity> subtasks, int concurrency) {
        if (operation == TaskOperation.ROLLBACK) {
            doMarkRollbackSubtasks(subtasks, concurrency);
        } else {
            doMarkRunningSubtasks(subtasks, concurrency);
        }
    }

    public void transactionWithoutResult(Consumer<TransactionStatus> action) {
        transactionTemplate.executeWithoutResult(action);
    }

    private void doMarkRunningSubtasks(Collection<SubtaskInstanceEntity> subtasks, int concurrency) {
        int rdyOrRunningCount = (int) subtasks.stream()
                .filter(subtask -> subtask.isRunning() || subtask.isReady())
                .count();
        if (concurrency < 0) {
            concurrency = defaultConcurrency;
        }
        // If ready or running subtask exceed max concurrency, skip.
        if (concurrency > 0 && rdyOrRunningCount >= concurrency) {
            return;
        }
        Set<Long> readySubtasks = new HashSet<>();
        for (SubtaskInstanceEntity subtask : subtasks) {
            if (concurrency > 0 && rdyOrRunningCount >= concurrency) {
                break;
            }
            if (subtask.getState() != SubtaskState.PENDING) {
                continue;
            }
            Set<SubtaskInstanceEntity> dependencies = subtask.getUpstreams();
            if (dependencies.stream().allMatch(entity -> entity.getState() == SubtaskState.SUCCESSFUL)) {
                readySubtasks.add(subtask.getId());
                ++rdyOrRunningCount;
            }
        }
        if (!readySubtasks.isEmpty()) {
            transactionWithoutResult(t -> subtaskOverviewRepo.updateStateByIdIn(readySubtasks, SubtaskState.READY));
        }
    }

    private void doMarkRollbackSubtasks(Collection<SubtaskInstanceEntity> subtasks, int concurrency) {
        int rdyOrRunningCount = (int) subtasks.stream().filter(ins -> ins.isRunning() || ins.isReady()).count();
        if (concurrency < 0) {
            concurrency = defaultConcurrency;
        }
        if (concurrency > 0 && rdyOrRunningCount >= concurrency) {
            return;
        }
        Set<Long> readySubtasks = new HashSet<>();
        for (SubtaskInstanceEntity subtask : subtasks) {
            if (concurrency > 0 && rdyOrRunningCount >= concurrency) {
                break;
            }
            if (subtask.getState() == SubtaskState.PENDING || subtask.isRunning()) {
                continue;
            }
            Set<SubtaskInstanceEntity> dependencies = subtask.getDownstreams();
            // Reverse marking upstream nodes, if all downstream subtask rollback, then mark
            // current node ready.
            if (dependencies.stream().allMatch(entity -> entity.getState() == SubtaskState.PENDING)) {
                ++rdyOrRunningCount;
                readySubtasks.add(subtask.getId());
            }
        }
        if (!readySubtasks.isEmpty()) {
            transactionWithoutResult(t -> subtaskOverviewRepo.updateStateByIdIn(readySubtasks, SubtaskState.READY));
        }
    }

}
