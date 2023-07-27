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
import java.util.function.Supplier;

import org.springframework.transaction.support.TransactionTemplate;

import com.oceanbase.ocp.task.dao.SubtaskInstanceRepository;
import com.oceanbase.ocp.task.dao.SubtaskLogRepo;
import com.oceanbase.ocp.task.dao.TaskDefinitionRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceRepository;
import com.oceanbase.ocp.task.dao.TaskTemplateRepository;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;
import com.oceanbase.ocp.task.entity.TaskInstanceEntity;
import com.oceanbase.ocp.task.hook.TaskHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEngineConfig {

    private TaskDefinitionRepository taskDefinitionRepository;
    private TaskTemplateRepository taskTemplateRepository;
    private TaskInstanceRepository taskInstanceRepository;
    private SubtaskInstanceRepository subtaskInstanceRepository;
    private SubtaskLogRepo subtaskLogRepo;

    private TransactionTemplate transactionTemplate;

    private Supplier<String> creatorSupplier;

    private String subtaskLogPath;

    private int subtaskExecutorCorePoolSize;
    private int subtaskExecutorMaxPoolSize;

    private int manualSubtaskExecutorCorePoolSize;
    private int manualSubtaskExecutorMaxPoolSize;

    @Builder.Default
    private List<TaskHook<TaskInstanceEntity>> postTaskHooks = new ArrayList<>();
    @Builder.Default
    private List<TaskHook<SubtaskInstanceEntity>> preSubtaskHooks = new ArrayList<>();
    @Builder.Default
    private List<TaskHook<SubtaskInstanceEntity>> postSubtaskHooks = new ArrayList<>();
}
