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

import com.fasterxml.jackson.annotation.JsonProperty;

import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BasicSubtaskInstance {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private SubtaskState status;

    @JsonProperty("operation")
    private SubtaskOperation operation;

    public static BasicSubtaskInstance fromOverview(SubtaskInstanceOverview overview) {
        if (overview == null) {
            return null;
        }
        BasicSubtaskInstance instance = new BasicSubtaskInstance();
        instance.setId(overview.getId());
        instance.setName(overview.getName());
        instance.setDescription(overview.getName());
        instance.setStatus(overview.getState());
        instance.setOperation(overview.getOperation());
        return instance;
    }

}
