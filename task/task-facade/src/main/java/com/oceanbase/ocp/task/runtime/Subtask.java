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
package com.oceanbase.ocp.task.runtime;

import com.oceanbase.ocp.task.model.Context;

public interface Subtask {

    /**
     * Get subtask name.
     *
     * @return taskName
     */
    String getName();

    /**
     * Get subtask retry count.
     *
     * @return retry count, default value is zero
     */
    default int getRetryCount() {
        return 0;
    }

    /**
     * Get subtask timeout seconds.
     *
     * @return timeout seconds
     */
    int getTimeout();

    /**
     * Execute subtask.
     *
     * @param c context
     * @return context
     */
    Context run(Context c);

    /**
     * Retry subtask.
     *
     * @param c context
     * @return context
     */
    default Context retry(Context c) {
        return run(rollback(c));
    }

    /**
     * Rollback subtask.
     *
     * @param c context
     * @return context
     */
    Context rollback(Context c);

    /**
     * Cancel execution of subtask. Send interrupt signal to thread, may not be able
     * to stop the running thread bound with subtask.
     */
    void cancel();

    /**
     * Does subtask support rollback, default is supported.
     *
     * @return boolean, true = forbidden, false = allow.
     */
    default boolean prohibitRollback() {
        return false;
    }

}
