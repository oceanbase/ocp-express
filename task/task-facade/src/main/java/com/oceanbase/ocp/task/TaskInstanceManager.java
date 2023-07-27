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
package com.oceanbase.ocp.task;

import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.model.TaskInstance;
import com.oceanbase.ocp.task.runtime.Template;

public interface TaskInstanceManager {

    /**
     * Rollback task.
     *
     * @param taskInstanceId id
     */
    void rollbackTask(long taskInstanceId);

    /**
     * Retry task.
     *
     * @param taskInstanceId id
     * @return Instance info for task.
     */
    TaskInstance retryTask(long taskInstanceId);

    /**
     * Query instance info for task.
     *
     * @param taskInstanceId id
     * @return Instance info for task.
     */
    TaskInstance getTaskInstance(long taskInstanceId);

    /**
     * Submit manually operation task.
     *
     * @param template task template
     * @param argument task argument
     * @return Instance info for task.
     */
    TaskInstance submitManualTask(Template template, Argument argument);

}
