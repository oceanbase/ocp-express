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

import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.dao.SubtaskAccessor;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FailedSubtaskWorker extends AbstractSubtaskWorker {

    private final SubtaskAccessor subtaskAccessor;

    public FailedSubtaskWorker(SubtaskCoordinatorConfig c) {
        super(c);
        this.subtaskAccessor = c.getSubtaskAccessor();
    }

    @Override
    public String name() {
        return "failed_subtask_worker";
    }

    @Override
    public String metricName() {
        return "ocp_task_failed_subtask_worker_duration";
    }

    @Override
    public void work() {
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        Set<Long> subtaskIds = getSubtaskIds(SubtaskState.FAILED, startTime);
        if (subtaskIds == null || subtaskIds.isEmpty()) {
            return;
        }
        for (Long subtaskId : subtaskIds) {
            try {
                markFailedTasks(subtaskId);
            } catch (Throwable throwable) {
                log.warn("Handle failed task error.", throwable);
            }
        }
    }

    private void markFailedTasks(long subtaskId) {
        SubtaskInstanceOverview entity = getAndCheckSubtaskState(subtaskId, SubtaskState.FAILED);
        // If subtask is canceled, just skip, wait for CancelingSubtaskWorker handle.
        if (entity == null || entity.getOperation() == SubtaskOperation.CANCEL) {
            return;
        }
        handleRetry(entity);
    }

    private void handleRetry(SubtaskInstanceOverview subtask) {
        // Only running or retrying subtask need to check timeout.
        if (subtask.getOperation() != SubtaskOperation.EXECUTE && subtask.getOperation() != SubtaskOperation.RETRY) {
            return;
        }
        int runTime = subtask.getRunTime();
        int retryCount = Optional.of(subtask)
                .map(SubtaskInstanceOverview::getSubtask)
                .map(Subtask::getRetryCount)
                .orElse(0);
        if (runTime <= retryCount) {
            log.info("Retry subtask, entity={}", subtask);
            subtask.setRunTime(runTime + 1);
            subtask.setOperation(SubtaskOperation.RETRY);
            subtaskAccessor.setReady(subtask);
        }
    }

}
