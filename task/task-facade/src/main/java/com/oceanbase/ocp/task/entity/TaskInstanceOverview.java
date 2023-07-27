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
package com.oceanbase.ocp.task.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.util.TaskArgumentConverter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Entity
@Table(name = "task_instance")
public class TaskInstanceOverview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "task_definition_id", nullable = false)
    private Long taskDefinitionId;

    @Column(name = "state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskState state;

    @Column(name = "type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskType type;

    @Column(name = "ob_tenant_id")
    private Long obTenantId;

    @Column(name = "creator")
    private String creator;

    @Column(name = "executor")
    private String executor;

    @Column(name = "context")
    @Convert(converter = TaskArgumentConverter.class)
    private Argument argument;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "operation")
    @Enumerated(value = EnumType.STRING)
    private TaskOperation operation;

    @Column(name = "trace_id")
    private String traceId;

}
