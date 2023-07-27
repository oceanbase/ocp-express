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
package com.oceanbase.ocp.task.web;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.util.sql.SqlUtils;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.task.TaskInstanceManager;
import com.oceanbase.ocp.task.constants.TaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.dao.SubtaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.dao.SubtaskLogRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.engine.manager.SubtaskInstanceManager;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;
import com.oceanbase.ocp.task.entity.SubtaskLogEntity;
import com.oceanbase.ocp.task.entity.TaskInstanceOverview;
import com.oceanbase.ocp.task.model.BasicCreatorInfo;
import com.oceanbase.ocp.task.model.BasicTaskInstance;
import com.oceanbase.ocp.task.model.BasicTenantInfo;
import com.oceanbase.ocp.task.model.SubtaskInstance;
import com.oceanbase.ocp.task.model.SubtaskLog;
import com.oceanbase.ocp.task.model.TaskInstance;
import com.oceanbase.ocp.task.model.WrappedTaskInstance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskInstanceOverviewRepository taskInstanceOverviewRepository;
    @Autowired
    private SubtaskInstanceOverviewRepository subtaskInstanceOverviewRepository;
    @Autowired
    private TenantDaoManager tenantDaoManager;
    @Autowired
    private SubtaskLogRepository subtaskLogRepository;
    @Autowired
    private TaskInstanceManager taskInstanceManager;
    @Autowired
    private SubtaskInstanceManager subtaskInstanceManager;

    public Page<BasicTaskInstance> listBasicTaskInstances(TaskInstanceQueryParam param, Pageable pageable) {
        Specification<TaskInstanceOverview> specification = buildSpecification(param);
        Page<TaskInstanceOverview> entities = taskInstanceOverviewRepository.findAll(specification, pageable);
        return new PageImpl<>(mapToBasicTaskInstances(entities.getContent()), pageable, entities.getTotalElements());
    }

    public WrappedTaskInstance getTaskInstance(long taskInstanceId) {
        return wrap(taskInstanceManager.getTaskInstance(taskInstanceId));
    }

    public Resource getTaskDiagnosis(long taskInstanceId) throws IOException {
        return doGetTaskDiagnosis(taskInstanceId);
    }

    public SubtaskLog getSubtaskDetail(long taskInstanceId, long subtaskInstanceId) {
        List<SubtaskLogEntity> logEntities = subtaskLogRepository.findAllBySubtaskInstanceId(subtaskInstanceId);
        logEntities.sort(Comparator.comparing(SubtaskLogEntity::getId));
        String logContent = logEntities.stream()
                .map(SubtaskLogEntity::getLogContent)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .collect(Collectors.joining("\n"));
        return SubtaskLog.builder()
                .log(logContent)
                .build();
    }

    public WrappedTaskInstance retryTask(long taskInstanceId) {
        return wrap(taskInstanceManager.retryTask(taskInstanceId));
    }

    public void rollbackTask(long taskInstanceId) {
        taskInstanceManager.rollbackTask(taskInstanceId);
    }

    public SubtaskInstance retrySubtask(long taskInstanceId, long subtaskInstanceId) {
        return subtaskInstanceManager.retrySubtask(subtaskInstanceId);
    }

    public void skipSubtask(long taskInstanceId, long subtaskInstanceId) {
        subtaskInstanceManager.skipSubtask(subtaskInstanceId);
    }

    public void cancelSubtask(long taskInstanceId, long subtaskInstanceId) {
        subtaskInstanceManager.cancelSubtask(subtaskInstanceId);
    }

    private Specification<TaskInstanceOverview> buildSpecification(TaskInstanceQueryParam queryParam) {
        return (Root<TaskInstanceOverview> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
            if (StringUtils.isNotEmpty(queryParam.getKeyword())) {
                predicatesList.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("name")),
                        "%" + SqlUtils.escapeStringValue(queryParam.getKeyword().toUpperCase()) + "%"));
            }
            if (queryParam.getStatus() != null) {
                List<TaskState> states = Arrays.stream(queryParam.getStatus().split(","))
                        .map(TaskState::fromValue).collect(Collectors.toList());
                predicatesList.add(root.get("state").in(states));
            }
            predicatesList.add(criteriaBuilder.equal(root.get("type"), TaskType.MANUAL));

            Predicate[] predicates = new Predicate[predicatesList.size()];
            return criteriaBuilder.and(predicatesList.toArray(predicates));
        };
    }

    private List<BasicTaskInstance> mapToBasicTaskInstances(List<TaskInstanceOverview> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        // Get subtasks.
        List<Long> taskIds = entities.stream().map(TaskInstanceOverview::getId).collect(Collectors.toList());
        Map<Long, List<SubtaskInstanceOverview>> groupedSubtaskOverviews =
                subtaskInstanceOverviewRepository.findAllByTaskIdIn(taskIds)
                        .stream()
                        .collect(Collectors.groupingBy(SubtaskInstanceOverview::getTaskId));

        // Generate response data.
        return entities.stream().map(entity -> toBasicTaskInstance(entity, groupedSubtaskOverviews.get(entity.getId())))
                .collect(Collectors.toList());
    }

    private BasicTaskInstance toBasicTaskInstance(TaskInstanceOverview entity,
            List<SubtaskInstanceOverview> subtaskInstanceOverviews) {
        BasicTaskInstance instance = BasicTaskInstance.fromOverview(entity, subtaskInstanceOverviews);

        instance.setCreator(new BasicCreatorInfo(entity.getCreator()));
        if (entity.getObTenantId() != null) {
            instance.setTenantInfo(buildTenantInfo(entity.getObTenantId()));
        }
        return instance;
    }

    private Resource doGetTaskDiagnosis(Long taskInstanceId) throws IOException {
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);

        TaskInstance taskInstance = taskInstanceManager.getTaskInstance(taskInstanceId);
        List<SubtaskInstance> subtasks = topologySort(taskInstance.getSubtasks());
        new Thread(() -> {
            try (ZipOutputStream zos = new ZipOutputStream(pos)) {
                // write task info.
                zos.putNextEntry(new ZipEntry("task_info.txt"));
                for (SubtaskInstance subtask : subtasks) {
                    String subtaskInfo = String.format("Subtask: %s, State: %s, Dependencies: %s\n",
                            subtask.getId(), subtask.getStatus(), new ArrayList<>(subtask.getUpstreams()));
                    zos.write(subtaskInfo.getBytes());
                }

                Map<Long, List<SubtaskLogEntity>> subtaskInstanceIdLogs = subtaskLogRepository
                        .findAllBySubtaskInstanceIdIn(subtasks.stream()
                                .map(SubtaskInstance::getId).collect(Collectors.toSet()))
                        .stream()
                        .collect(Collectors.groupingBy(SubtaskLogEntity::getSubtaskInstanceId));

                // write subtask log.
                for (SubtaskInstance subtask : subtasks) {
                    int runTime = 0;
                    List<SubtaskLogEntity> logs = Optional.ofNullable(subtaskInstanceIdLogs.get(subtask.getId()))
                            .orElse(Collections.emptyList())
                            .stream()
                            .sorted(Comparator.comparingLong(SubtaskLogEntity::getId))
                            .collect(Collectors.toList());
                    for (SubtaskLogEntity log : logs) {
                        if (log.getRunTime() > runTime) {
                            zos.closeEntry();
                            runTime = log.getRunTime();
                            String logFileName = String.format("subtask_%s_%s.log", subtask.getId(), runTime);
                            zos.putNextEntry(new ZipEntry(logFileName));
                        }
                        zos.write(log.getLogContent());
                    }
                }
                zos.closeEntry();
            } catch (IOException ioe) {
                log.error("got io exception write diagnosis info", ioe);
            }
        }).start();
        return new InputStreamResource(pis);
    }

    private List<SubtaskInstance> topologySort(Set<SubtaskInstance> tasks) {
        Map<Long, SubtaskInstance> taskMap = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        List<SubtaskInstance> sortedTasks = new ArrayList<>();

        for (SubtaskInstance task : tasks) {
            taskMap.put(task.getId(), task);
        }
        while (taskMap.size() > 0) {
            Iterator<Map.Entry<Long, SubtaskInstance>> iter = taskMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Long, SubtaskInstance> entry = iter.next();
                Set<Long> dependencySet = new HashSet<>(entry.getValue().getUpstreams());
                if (visited.containsAll(dependencySet)) {
                    sortedTasks.add(entry.getValue());
                    visited.add(entry.getKey());
                    iter.remove();
                }
            }
        }
        return sortedTasks;
    }

    private WrappedTaskInstance wrap(TaskInstance taskInstance) {
        if (taskInstance == null) {
            return null;
        }

        WrappedTaskInstance.WrappedTaskInstanceBuilder builder = WrappedTaskInstance.builder()
                .id(taskInstance.getId())
                .name(taskInstance.getName())
                .taskDefinitionId(taskInstance.getTaskDefinitionId())
                .status(taskInstance.getStatus())
                .type(taskInstance.getType())
                .executor(taskInstance.getExecutor())
                .createTime(taskInstance.getCreateTime())
                .startTime(taskInstance.getStartTime())
                .finishTime(taskInstance.getFinishTime())
                .subtasks(taskInstance.getSubtasks())
                .operation(taskInstance.getOperation())
                .prohibitRollback(taskInstance.getProhibitRollback())
                .creator(new BasicCreatorInfo(taskInstance.getCreator()));

        if (taskInstance.getObTenantId() != null) {
            builder.tenantInfo(buildTenantInfo(taskInstance.getObTenantId()));
        }
        return builder.build();
    }

    private BasicTenantInfo buildTenantInfo(long obTenantId) {
        Optional<ObTenantEntity> optional = tenantDaoManager.getTenant(obTenantId);
        return optional.map(
                tenantEntity -> BasicTenantInfo.builder().obTenantId(obTenantId).name(tenantEntity.getName()).build())
                .orElse(null);
    }

}
