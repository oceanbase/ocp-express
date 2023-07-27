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
package com.oceanbase.ocp.task.engine.manager;

import com.oceanbase.ocp.task.model.SubtaskInstance;

public interface SubtaskInstanceManager {

    /**
     * Retry subtask.
     *
     * @param id subtaskId
     * @return instance info of subtask
     */
    SubtaskInstance retrySubtask(long id);

    /**
     * Skip subtask.
     *
     * @param id subtaskId
     */
    void skipSubtask(long id);

    /**
     * Cancel subtask.
     *
     * @param id subtaskId
     */
    void cancelSubtask(long id);

}
