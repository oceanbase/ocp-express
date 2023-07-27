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

import org.springframework.transaction.support.TransactionTemplate;

import com.oceanbase.ocp.task.dao.SubtaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.dao.TaskDefinitionRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceRepository;
import com.oceanbase.ocp.task.dao.TaskTemplateRepository;
import com.oceanbase.ocp.task.entity.TaskInstanceOverview;
import com.oceanbase.ocp.task.hook.TaskHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCoordinatorConfig {

    private TaskDefinitionRepository taskDefinitionRepository;
    private TaskTemplateRepository taskTemplateRepository;

    private TaskInstanceOverviewRepository taskOverviewRepo;
    private TaskInstanceRepository taskInstanceRepository;
    private SubtaskInstanceOverviewRepository subtaskOverviewRepo;

    private TransactionTemplate transactionTemplate;

    @Builder.Default
    private List<TaskHook<TaskInstanceOverview>> postTaskHooks = new ArrayList<>();

    private int defaultSubtaskConcurrency;
}
