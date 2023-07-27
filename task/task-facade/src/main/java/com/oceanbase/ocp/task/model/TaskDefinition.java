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
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.task.constants.ScheduleType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDefinition {

    private Long id;

    private String name;

    private Long creatorId;

    private ScheduleType scheduleType;

    private String scheduleRule;

    private String scheduleStartTime;

    private String scheduleEndTime;

    private ChronoUnit scheduleDurationUnit;

    private Date lastRunTime;

    private Date nextRunTime;

    private String templateName;

    private String comments;

    private Argument arguments;

    private Boolean enable;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("creatorId", creatorId)
                .add("templateName", templateName)
                .add("scheduleType", scheduleType)
                .add("scheduleRule", scheduleRule)
                .add("scheduleStartTime", scheduleStartTime)
                .add("scheduleEndTime", scheduleEndTime)
                .add("scheduleDurationUnit", scheduleDurationUnit)
                .add("nextRunTime", nextRunTime)
                .add("arguments", Optional.ofNullable(arguments).map(Context::getContextString).orElse(null))
                .toString();
    }

}
