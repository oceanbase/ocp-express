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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.task.constants.SplitMethod;
import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Node;
import com.oceanbase.ocp.task.runtime.Template;
import com.oceanbase.ocp.task.util.TaskArgumentConverter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Entity
@Table(name = "task_instance")
@NamedEntityGraph(name = "task.all",
        attributeNodes = {@NamedAttributeNode(value = "subtasks", subgraph = "subtasks.streams")},
        subgraphs = {@NamedSubgraph(name = "subtasks.streams",
                attributeNodes = {@NamedAttributeNode("upstreams"), @NamedAttributeNode("downstreams")})})
public class TaskInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "task_definition_id", nullable = false)
    private long taskDefinitionId;

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

    @Column(name = "create_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taskInstance", fetch = FetchType.EAGER)
    private Set<SubtaskInstanceEntity> subtasks;

    @Transient
    private Template template;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "operation")
    @Enumerated(value = EnumType.STRING)
    private TaskOperation operation;

    public TaskInstanceEntity() {
        this.subtasks = new HashSet<>();
    }

    public TaskInstanceEntity(Template t) {
        this.template = t;
        this.name = t.getName();
        this.state = TaskState.RUNNING;
        this.subtasks = new HashSet<>();
    }

    public void generateSubtasks(Context c) {
        Set<Node> nodes = this.template.getNodes();
        Map<Node, LinkedList<SubtaskInstanceEntity>> nodeTasksMap = new HashMap<>();

        long seriesId = 0L;
        for (Node node : nodes) {
            nodeTasksMap.put(node, new LinkedList<>());
            String splitKey = node.getSplitKey();
            if (!splitKey.isEmpty()) {
                if (c.getListMap() == null || c.getListMap().get(splitKey) == null
                        || c.getListMap().get(splitKey).size() == 0) {
                    log.info("Split list is empty, name={}, splitKey={}, context={}",
                            getName(), splitKey, c.getContextString());
                    throw new RuntimeException("Illegal split list, the list is empty, splitKey=" + splitKey);
                }
                for (int idx = 0; idx < c.getListMap().get(splitKey).size(); idx++) {
                    SubtaskInstanceEntity subtaskInstance = new SubtaskInstanceEntity(node.getSubtask());
                    subtaskInstance.setParallelIdx(idx);
                    subtaskInstance.setTaskInstance(this);
                    subtaskInstance.setSeriesId(seriesId++);
                    if (node.getSplitMethod() == SplitMethod.SERIAL && nodeTasksMap.get(node).size() > 0) {
                        subtaskInstance.addUpstream(nodeTasksMap.get(node).getLast());
                    }
                    nodeTasksMap.get(node).add(subtaskInstance);
                    this.subtasks.add(subtaskInstance);
                }
            } else {
                SubtaskInstanceEntity subtaskInstance = new SubtaskInstanceEntity(node.getSubtask());
                subtaskInstance.setTaskInstance(this);
                subtaskInstance.setSeriesId(seriesId++);
                nodeTasksMap.get(node).add(subtaskInstance);
                this.subtasks.add(subtaskInstance);
            }
            for (Node nu : node.getUpstreams()) {
                if (nodeTasksMap.containsKey(nu)) {
                    for (SubtaskInstanceEntity tCurrent : (node.getSplitMethod() == SplitMethod.SERIAL
                            ? Collections.singletonList(nodeTasksMap.get(node).getFirst())
                            : nodeTasksMap.get(node))) {
                        for (SubtaskInstanceEntity tUpstream : (nu.getSplitMethod() == SplitMethod.SERIAL
                                ? Collections.singletonList(nodeTasksMap.get(nu).getLast())
                                : nodeTasksMap.get(nu))) {
                            tCurrent.addUpstream(tUpstream);
                        }
                    }
                }
            }
            for (Node nd : node.getDownstreams()) {
                if (nodeTasksMap.containsKey(nd)) {
                    for (SubtaskInstanceEntity tCurrent : (node.getSplitMethod() == SplitMethod.SERIAL
                            ? Collections.singletonList(nodeTasksMap.get(node).getLast())
                            : nodeTasksMap.get(node))) {
                        for (SubtaskInstanceEntity tDownstream : (nd.getSplitMethod() == SplitMethod.SERIAL
                                ? Collections.singletonList(nodeTasksMap.get(nd).getFirst())
                                : nodeTasksMap.get(nd))) {
                            tCurrent.addDownstream(tDownstream);
                        }
                    }
                }
            }
        }
        log.info("generate tasks, templateName={}, nodeCount={}, taskCount={}", template.getName(), nodes.size(),
                subtasks.size());
    }

    public Set<SubtaskInstanceEntity> getSubtasks() {
        return this.subtasks;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("taskDefinitionId", taskDefinitionId)
                .add("state", state)
                .add("operation", operation)
                .toString();
    }

}
