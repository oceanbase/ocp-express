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
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.task.constants.ScheduleType;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.util.TaskArgumentConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_definition")
public class TaskDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "schedule_type")
    @Enumerated(value = EnumType.STRING)
    private ScheduleType scheduleType;

    @Column(name = "schedule_rule")
    private String scheduleRule;

    @Column(name = "schedule_start_time")
    private String scheduleStartTime;

    @Column(name = "schedule_end_time")
    private String scheduleEndTime;

    @Column(name = "schedule_duration_unit")
    private ChronoUnit scheduleDurationUnit;

    @Column(name = "last_run_time")
    private Date lastRunTime;

    @Column(name = "next_run_time")
    private Date nextRunTime;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "comments")
    private String comments;

    @Column(name = "arguments")
    @Convert(converter = TaskArgumentConverter.class)
    private Argument arguments;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "create_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "update_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("templateName", templateName)
                .add("scheduleRule", scheduleRule)
                .add("lastRunTime", lastRunTime)
                .add("nextRunTime", nextRunTime)
                .toString();
    }

}
