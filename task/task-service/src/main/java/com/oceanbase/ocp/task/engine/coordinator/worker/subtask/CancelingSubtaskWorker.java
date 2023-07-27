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
import java.util.concurrent.Future;

import org.hibernate.PessimisticLockException;
import org.springframework.dao.ConcurrencyFailureException;

import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.dao.SubtaskAccessor;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.util.AsyncTimeout;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CancelingSubtaskWorker extends AbstractSubtaskWorker {

    private final SubtaskAccessor subtaskAccessor;

    public CancelingSubtaskWorker(SubtaskCoordinatorConfig c) {
        super(c);
        this.subtaskAccessor = c.getSubtaskAccessor();
    }

    @Override
    public String name() {
        return "cancel_subtask_worker";
    }

    @Override
    public String metricName() {
        return "ocp_task_cancel_subtask_worker_duration";
    }

    @Override
    public void work() {
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        Set<Long> subtaskIds = getSubtaskIds(SubtaskState.CANCELING, startTime);
        if (subtaskIds == null || subtaskIds.isEmpty()) {
            return;
        }
        for (Long subtaskId : subtaskIds) {
            try {
                cancelRunningSubtask(subtaskId);
                handleTaskCancel(subtaskId);
            } catch (Throwable throwable) {
                log.warn("Schedule canceling task failed, subtaskId={}, exception={}", subtaskId, throwable);
            }
        }
    }

    private void handleTaskCancel(long subtaskId) {
        log.info("Handle subtask cancel, subtaskInstanceId={}", subtaskId);
        SubtaskInstanceOverview subtask = getAndCheckSubtaskState(subtaskId, SubtaskState.CANCELING);
        if (subtask == null) {
            return;
        }
        transactionWithoutResult(status -> {
            try {
                subtask.setOperation(SubtaskOperation.CANCEL);
                subtaskAccessor.setRunning(subtask);
            } catch (ConcurrencyFailureException | PessimisticLockException e) {
                log.info("Another worker handled task cancel, subtask={}", subtask);
            }
        });
    }

    private void cancelRunningSubtask(long subtaskId) {
        SubtaskInstanceOverview subtask = findById(subtaskId);
        if (!getExecutor().equals(subtask.getExecutor())) {
            return;
        }
        Optional<AsyncTimeout> timeout = AsyncTimeout.getBySubtaskInstanceId(subtask.getId());
        if (timeout.isPresent()) {
            Future<SubtaskInstanceOverview> future = timeout.get().getFuture();
            if (future != null && !future.isDone() && !future.isCancelled()) {
                boolean cancel = future.cancel(true);
                log.info("Cancel running subtask, subtask={}, result={}", subtask, cancel);
            }
        }
    }

}
