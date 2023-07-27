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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.common.util.ClassUtils;
import com.oceanbase.ocp.task.constants.NodeType;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;
import com.oceanbase.ocp.task.util.TaskContextConverter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Setter
@Slf4j
@Table(name = "subtask_instance")
public class SubtaskInstanceOverview {

    private static ConcurrentHashMap<String, Class<?>> subTaskCache = new ConcurrentHashMap<>(128);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "series_id", nullable = false)
    private Long seriesId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "timeout", nullable = false)
    private Integer timeout;

    @Column(name = "state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private SubtaskState state;

    @Column(name = "executor")
    private String executor;

    @Column(name = "run_time")
    private Integer runTime;

    @Column(name = "context")
    @Convert(converter = TaskContextConverter.class)
    private Context context;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "node_type")
    @Enumerated(value = EnumType.STRING)
    private NodeType nodeType;

    @Column(name = "parallel_idx")
    private int parallelIdx;

    @Column(name = "operation")
    @Enumerated(value = EnumType.STRING)
    private SubtaskOperation operation;

    @Column(name = "create_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "update_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    @Version
    private Integer version;

    public String getContextString() {
        if (this.context == null) {
            return null;
        }
        return this.context.getContextString();
    }

    public Subtask getSubtask() {
        Subtask subtask = null;
        Class<?> taskClazz = getSubtaskClass();
        try {
            if (taskClazz != null) {
                subtask = ((Subtask) (taskClazz.newInstance()));
            }
        } catch (Exception e) {
            log.warn("Get subtask instance failed.", e);
        }
        return subtask;
    }

    private Class<?> getSubtaskClass() {
        if (this.getClassName() == null) {
            return null;
        }
        return subTaskCache.computeIfAbsent(this.getClassName(), className -> {
            try {
                Class<?> clazz = ClassUtils.loadClass(this.getClassName());
                Objects.requireNonNull(clazz);
                return clazz;
            } catch (Exception e) {
                log.error("failed to submit task, className=" + this.getClassName(), e);
                return null;
            }
        });
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("state", state)
                .add("operation", operation)
                .add("className", className)
                .add("seriesId", seriesId)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .toString();
    }

}
