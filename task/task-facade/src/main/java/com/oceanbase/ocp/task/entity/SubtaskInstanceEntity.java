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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.common.util.ClassUtils;
import com.oceanbase.ocp.task.constants.NodeType;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskConstants;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;
import com.oceanbase.ocp.task.util.TaskContextConverter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Entity
@Table(name = "subtask_instance")
public class SubtaskInstanceEntity {

    private static ConcurrentHashMap<String, Class<?>> subTaskCache = new ConcurrentHashMap<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private TaskInstanceEntity taskInstance;

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

    @Column(name = "create_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @JoinTable(name = "subtask_dependencies",
            joinColumns = {@JoinColumn(name = "child_id", referencedColumnName = "id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false)})
    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<SubtaskInstanceEntity> upstreams;

    @ManyToMany(mappedBy = "upstreams", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<SubtaskInstanceEntity> downstreams;

    @Column(name = "node_type")
    @Enumerated(value = EnumType.STRING)
    private NodeType nodeType;

    @Transient
    private Subtask subtask;

    @Column(name = "parallel_idx")
    private int parallelIdx;

    @Column(name = "operation")
    @Enumerated(value = EnumType.STRING)
    private SubtaskOperation operation;

    @Version
    private Integer version;

    @Column(name = "update_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    public SubtaskInstanceEntity() {
        this.downstreams = new HashSet<>();
        this.upstreams = new HashSet<>();
    }

    public SubtaskInstanceEntity(Subtask subtask) {
        this.subtask = subtask;
        this.name = this.subtask.getName();
        this.className = this.subtask.getClass().getName();
        this.state = SubtaskState.PENDING;
        this.timeout = subtask.getTimeout();
        this.parallelIdx = TaskConstants.NO_PARALLEL_IDX;
        this.upstreams = new HashSet<>();
        this.downstreams = new HashSet<>();
        this.runTime = 1;
        this.operation = SubtaskOperation.EXECUTE;
        this.nodeType = NodeType.JAVA_TASK;
    }

    public void addUpstream(SubtaskInstanceEntity t) {
        if (this.upstreams.contains(t)) {
            return;
        }
        this.upstreams.add(t);
        t.addDownstream(this);
    }

    public void addDownstream(SubtaskInstanceEntity t) {
        if (this.downstreams.contains(t)) {
            return;
        }
        this.downstreams.add(t);
        t.addUpstream(this);
    }

    public boolean isSuccessful() {
        return this.state == SubtaskState.SUCCESSFUL;
    }

    public boolean isFailed() {
        return this.state == SubtaskState.FAILED;
    }

    public boolean isPending() {
        return this.state == SubtaskState.PENDING;
    }

    public boolean isRunning() {
        return this.state == SubtaskState.RUNNING;
    }

    public boolean isReady() {
        return this.state == SubtaskState.READY;
    }

    public void setState(SubtaskState state) {
        log.info("Set state for subtask: {}, current state: {}, new state: {}", this.id, this.state, state);
        this.state = state;
    }

    public Subtask getSubtaskInstance() {
        Subtask subtask = this.subtask;
        if (subtask != null) {
            return subtask;
        }
        Class<?> taskClazz = getSubtaskClass();
        try {
            if (taskClazz != null) {
                subtask = ((Subtask) (taskClazz.newInstance()));
            }
        } catch (Exception e) {
            log.warn("Get subtask instance failed.", e);
        }
        this.subtask = subtask;
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

    @JsonIgnore
    public boolean prohibitRollback() {
        Subtask task = getSubtaskInstance();
        return task != null && task.prohibitRollback();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubtaskInstanceEntity that = (SubtaskInstanceEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(this.getSeriesId(), that.seriesId)
                && Objects.equals(this.context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, seriesId, context);
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
