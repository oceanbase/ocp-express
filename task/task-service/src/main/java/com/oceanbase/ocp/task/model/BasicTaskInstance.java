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
import java.util.List;
import java.util.stream.Collectors;

import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;
import com.oceanbase.ocp.task.entity.TaskInstanceOverview;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BasicTaskInstance {

    private Long id;

    private String name;

    private BasicCreatorInfo creator;

    private BasicTenantInfo tenantInfo;

    private TaskState status;

    private OffsetDateTime startTime;

    private OffsetDateTime finishTime;

    private List<BasicSubtaskInstance> subtasks;

    public static BasicTaskInstance fromOverview(TaskInstanceOverview overview,
            List<SubtaskInstanceOverview> subtaskInstanceOverviews) {
        if (overview == null) {
            return null;
        }
        BasicTaskInstance instance = new BasicTaskInstance();
        instance.setId(overview.getId());
        instance.setName(overview.getName());
        instance.setStatus(overview.getState());
        instance.setStartTime(overview.getStartTime());
        instance.setFinishTime(overview.getEndTime());
        if (subtaskInstanceOverviews != null) {
            instance.setSubtasks(subtaskInstanceOverviews.stream()
                    .map(BasicSubtaskInstance::fromOverview)
                    .collect(Collectors.toList()));
        }
        return instance;
    }

}
