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

package com.oceanbase.ocp.task.schedule;

import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Template;

public interface ISchedule {

    /**
     * Get schedule task name, default value is simple class name.
     *
     * @return name of schedule
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * get template of task
     *
     * @return {@link Template}
     */
    Template getTemplate();

    /**
     * get task context
     *
     * @return {@link Context}
     */
    default Argument getArgument() {
        return new Argument();
    }

    /**
     * if is singleton task: <br>
     * - true: only one task will run <br>
     * - false: every ocp will run the task
     *
     * @return
     */
    boolean isSingleton();

    /**
     * check is this schedule task need run or not.
     *
     * @return true if task is ready and need schedule
     */
    default boolean ready() {
        return true;
    }

}
