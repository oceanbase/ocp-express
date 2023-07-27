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
package com.oceanbase.ocp.task.model;

import java.time.OffsetDateTime;
import java.util.Set;

import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.constants.TaskType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrappedTaskInstance {

    private Long id;

    private String name;

    private Long taskDefinitionId;

    private TaskState status;

    private TaskType type;

    private BasicCreatorInfo creator;

    private BasicTenantInfo tenantInfo;

    private String executor;

    private OffsetDateTime createTime;

    private OffsetDateTime startTime;

    private OffsetDateTime finishTime;

    private Set<SubtaskInstance> subtasks;

    private TaskOperation operation;

    private boolean prohibitRollback;

}
