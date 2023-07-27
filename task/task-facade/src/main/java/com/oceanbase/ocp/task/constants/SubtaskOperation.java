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
package com.oceanbase.ocp.task.constants;

import lombok.Getter;

public enum SubtaskOperation {

    /**
     * Normal execute.
     */
    EXECUTE("execute"),
    /**
     * Retry after subtask failed.
     */
    RETRY("retry"),
    /**
     * Rollback after subtask failed.
     */
    ROLLBACK("rollback"),
    /**
     * Skip failed subtask.
     */
    SKIP("skip"),
    /**
     * Skip rollback failed subtask, used in rollback scene.
     */
    ROLLBACK_SKIP("rollback_skip"),
    /**
     * Cancel one running subtask, just set thread interrupt sign, subtask may not
     * stopped.
     */
    CANCEL("cancel");

    @Getter
    private final String value;

    SubtaskOperation(String value) {
        this.value = value;
    }

}
