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
package com.oceanbase.ocp.task.engine.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.transaction.support.TransactionTemplate;

import com.oceanbase.ocp.task.dao.SubtaskAccessor;
import com.oceanbase.ocp.task.dao.SubtaskInstanceRepository;
import com.oceanbase.ocp.task.dao.SubtaskLogRepo;
import com.oceanbase.ocp.task.entity.TaskInstanceOverview;
import com.oceanbase.ocp.task.hook.SubtaskHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskCoordinatorConfig {

    private SubtaskInstanceRepository subtaskRepo;
    private SubtaskAccessor subtaskAccessor;
    private SubtaskLogRepo subtaskLogRepo;

    private TransactionTemplate transactionTemplate;

    private String subtaskLogPath;

    private int subtaskExecutorCorePoolSize;
    private int subtaskExecutorMaxPoolSize;

    private int manualSubtaskExecutorCorePoolSize;
    private int manualSubtaskExecutorMaxPoolSize;

    @Builder.Default
    private List<SubtaskHook<Long>> preSubtaskHooks = new ArrayList<>();
    @Builder.Default
    private List<SubtaskHook<Long>> postSubtaskHooks = new ArrayList<>();

    private Function<Long, TaskInstanceOverview> taskFunc;

}
