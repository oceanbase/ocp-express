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

package com.oceanbase.ocp.executor.model.task;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.JsonNode;

import com.oceanbase.ocp.common.util.LogContentUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatus {

    private static final String ROLLBACK_TASK_NAME = "rollback";

    private static final String TASK_START_STATUS = "start";

    private static final String TASK_SUCCESS_STATUS = "ok";

    private static final String TASK_FAILED_STATUS = "fail";

    private static final List<String> TERMINAL_STATUS = Arrays.asList(TASK_SUCCESS_STATUS, TASK_FAILED_STATUS);

    /**
     * Is agent task finished.
     */
    private boolean finished;

    /**
     * Is agent task successful.
     */
    private boolean ok;

    /**
     * Task result.
     */
    private JsonNode result;

    /**
     * Error message.
     */
    private String err;

    /**
     * Task progress, contains all subtask status.
     */
    private List<SubTaskStatus> progress;

    /** mask err */
    public String getErr() {
        return LogContentUtils.mask(err);
    }

    public boolean isSuccess() {
        return finished && ok;
    }

    public boolean isFailed() {
        return (finished && !ok) || isRollbackStarted();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("ok", ok)
                .append("result", result)
                .append("err", LogContentUtils.mask(err))
                .append("progress", progress)
                .toString();
    }

    private boolean isRollbackStarted() {
        return CollectionUtils.isNotEmpty(progress)
                && progress.stream().anyMatch(status -> ROLLBACK_TASK_NAME.equalsIgnoreCase(status.getName())
                        && TASK_START_STATUS.equalsIgnoreCase(status.getStatus()));
    }

    public boolean isRollbackFinished() {
        if (!isFinished()) {
            return false;
        }
        if (CollectionUtils.isEmpty(progress)) {
            return true;
        }
        return progress.stream().anyMatch(pro -> ROLLBACK_TASK_NAME.equalsIgnoreCase(pro.getName())
                && (TERMINAL_STATUS.contains(pro.getStatus())));
    }

    public boolean isRollbackSuccess() {
        if (isRollbackFinished()) {
            if (CollectionUtils.isEmpty(progress)) {
                return true;
            }
            return progress.stream().anyMatch(pro -> ROLLBACK_TASK_NAME.equalsIgnoreCase(pro.getName())
                    && TASK_SUCCESS_STATUS.equalsIgnoreCase(pro.getStatus()));
        }
        return false;
    }

}
