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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.runner.RunnerFactory;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TimeoutSubtaskWorker extends AbstractSubtaskWorker {

    private final RunnerFactory runnerFactory;

    protected TimeoutSubtaskWorker(SubtaskCoordinatorConfig c, RunnerFactory runnerFactory) {
        super(c);
        this.runnerFactory = runnerFactory;
    }

    protected void timeout(SubtaskInstanceOverview entity, String logContent) {
        entity.setOperation(SubtaskOperation.CANCEL);
        Consumer<SubtaskInstanceOverview> consumer = i -> {
            log.warn(logContent);
        };
        execute(TaskType.SCHEDULED, () -> runnerFactory.executeAndPrintLog(entity, consumer));
    }

    protected long getSubtaskExecutedSeconds(SubtaskInstanceOverview entity) {
        String startTime = Optional.of(entity)
                .map(SubtaskInstanceOverview::getContext)
                .map(context -> context.get(ContextKey.LATEST_EXECUTION_START_TIME))
                .orElse(null);
        OffsetDateTime startedAt;
        if (startTime == null || startTime.isEmpty()) {
            startedAt = entity.getStartTime();
        } else {
            startedAt = OffsetDateTime.parse(entity.getContext().get(ContextKey.LATEST_EXECUTION_START_TIME));
        }
        return Duration.between(startedAt, OffsetDateTime.now()).getSeconds();
    }

}
