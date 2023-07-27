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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.oceanbase.ocp.task.constants.NodeType;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskInstance {

    private Long id;

    private Long seriesId;

    private String name;

    private String description;

    private Integer timeout;

    private SubtaskState status;

    private String executor;

    private Integer runTime;

    @JsonIgnore
    private Context context;

    private OffsetDateTime createTime;

    private OffsetDateTime startTime;

    private OffsetDateTime finishTime;

    private NodeType nodeType;

    private Integer parallelIdx;

    private SubtaskOperation operation;

    private List<Long> upstreams;

    private List<Long> downstreams;

    private boolean prohibitRollback;

    public static SubtaskInstance fromEntity(final SubtaskInstanceEntity entity) {
        if (entity == null) {
            return null;
        }
        List<Long> upstreams = Optional.ofNullable(entity.getUpstreams())
                .map(entities -> entities.stream()
                        .map(SubtaskInstanceEntity::getId)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        List<Long> downstreams = Optional.ofNullable(entity.getDownstreams())
                .map(entities -> entities.stream()
                        .map(SubtaskInstanceEntity::getId)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        return SubtaskInstance.builder()
                .id(entity.getId())
                .seriesId(entity.getSeriesId())
                .name(entity.getName())
                .description(entity.getName())
                .timeout(entity.getTimeout())
                .status(entity.getState())
                .executor(entity.getExecutor())
                .runTime(entity.getRunTime())
                .context(entity.getContext())
                .createTime(entity.getCreateTime())
                .startTime(entity.getStartTime())
                .finishTime(entity.getEndTime())
                .nodeType(entity.getNodeType())
                .parallelIdx(entity.getParallelIdx())
                .operation(entity.getOperation())
                .upstreams(upstreams)
                .downstreams(downstreams)
                .prohibitRollback(entity.prohibitRollback())
                .build();
    }

}
