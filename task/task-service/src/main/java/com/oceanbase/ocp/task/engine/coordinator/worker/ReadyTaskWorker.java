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
package com.oceanbase.ocp.task.engine.coordinator.worker;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Range;

import com.oceanbase.ocp.common.util.ClassUtils;
import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.ScheduleType;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskOperation;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.constants.TemplateType;
import com.oceanbase.ocp.task.dao.TaskDefinitionRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceRepository;
import com.oceanbase.ocp.task.dao.TaskTemplateRepository;
import com.oceanbase.ocp.task.engine.config.TaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.util.SpringScheduler;
import com.oceanbase.ocp.task.entity.SubtaskInstanceEntity;
import com.oceanbase.ocp.task.entity.TaskDefinitionEntity;
import com.oceanbase.ocp.task.entity.TaskInstanceEntity;
import com.oceanbase.ocp.task.entity.TaskTemplateEntity;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.runtime.Template;
import com.oceanbase.ocp.task.schedule.ISchedule;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadyTaskWorker extends AbstractTaskWorker {

    private static final String NAME = "ready_task_worker";

    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskTemplateRepository taskTemplateRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final String executor;

    public ReadyTaskWorker(TaskCoordinatorConfig c) {
        super(c);
        this.taskDefinitionRepository = c.getTaskDefinitionRepository();
        this.taskTemplateRepository = c.getTaskTemplateRepository();
        this.taskInstanceRepository = c.getTaskInstanceRepository();
        this.executor = HostUtils.getLocalIp();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String metricName() {
        return "ocp_task_ready_task_worker_duration";
    }

    @Override
    public void work() {
        List<TaskDefinitionEntity> allOnceTasks =
                taskDefinitionRepository.findAllByScheduleTypeAndEnabled(ScheduleType.ONCE, true);
        allOnceTasks.removeIf(entity -> entity.getLastRunTime() != null);
        loopScheduleTask(allOnceTasks, this::checkAndScheduleOnceTask);

        List<TaskDefinitionEntity> allScheduleTasks =
                taskDefinitionRepository.findAllByScheduleTypeAndEnabled(ScheduleType.SCHEDULE, true);
        loopScheduleTask(allScheduleTasks, this::checkAndScheduleCronTask);
    }

    public TaskInstanceEntity submitTask(TaskType taskType, Template template, Argument argument, String creator) {
        return submitTask(null, taskType, template, argument, creator);
    }

    private void loopScheduleTask(List<TaskDefinitionEntity> tasks, Consumer<TaskDefinitionEntity> consumer) {
        for (TaskDefinitionEntity entity : tasks) {
            try {
                transactionWithoutResult(status -> {
                    consumer.accept(entity);
                });
            } catch (Throwable throwable) {
                log.warn("Schedule ready-task failed, entity=" + entity.toString(), throwable);
            }
        }
    }

    private void checkAndScheduleOnceTask(TaskDefinitionEntity task) {
        String scheduleRule = task.getScheduleRule();
        try {
            OffsetDateTime scheduleTimePoint = OffsetDateTime.parse(scheduleRule);
            OffsetDateTime now = OffsetDateTime.now();
            if (scheduleTimePoint.isBefore(now)) {
                task.setLastRunTime(Date.from(now.toInstant()));
                doScheduleTask(task);
            }
        } catch (DateTimeParseException e) {
            log.warn("Parse schedule rule failed, task=" + task, e);
            // Disable task definition if the schedule rule is illegal.
            task.setEnabled(false);
            taskDefinitionRepository.saveAndFlush(task);
        }
    }

    private void checkAndScheduleCronTask(TaskDefinitionEntity task) {
        log.debug("Check and schedule ready tasks, entity={}", task);
        if (!isScheduleDurationArrived(task.getScheduleStartTime(), task.getScheduleEndTime(),
                task.getScheduleDurationUnit())) {
            return;
        }
        SpringScheduler scheduler =
                new SpringScheduler(task.getScheduleRule(), task.getLastRunTime(), task.getNextRunTime());
        if (scheduler.trySchedule()) {
            log.info("Schedule ready task, entity={}", task);
            task.setLastRunTime(scheduler.lastRunTime());
            task.setNextRunTime(scheduler.nextRunTime());
            doScheduleTask(task);
        }
        if (task.getNextRunTime() == null) {
            taskDefinitionRepository.updateRunTime(task.getId(), scheduler.lastRunTime(), scheduler.nextRunTime());
        }
    }

    private void doScheduleTask(TaskDefinitionEntity def) {
        try {
            TaskReflectMeta meta = getTaskMeta(def);
            if (!meta.ready) {
                log.info("Task is not ready for schedule, taskName={}", def.getName());
                return;
            }
            submitTask(def.getId(), meta.getTaskType(), meta.getTemplate(), meta.getArgument(), OcpConstants.SYSTEM);
        } catch (Exception e) {
            log.warn("Schedule cron task failed.", e);
        } finally {
            transactionWithoutResult(t -> {
                taskDefinitionRepository.updateRunTime(def.getId(), def.getLastRunTime(), def.getNextRunTime());
            });
        }
    }

    private TaskInstanceEntity submitTask(Long taskDefinitionId, TaskType taskType, Template template, Argument arg,
            String creator) {
        ExceptionUtils.unExpected(null != template, "Schedule template is null");
        TaskInstanceEntity entity = buildTaskInstance(taskDefinitionId, template, arg, taskType, creator);
        initSubtaskState(entity.getSubtasks(), arg.getConcurrency());
        log.info("Save task instance, entity={}", entity);
        return taskInstanceRepository.saveAndFlush(entity);
    }

    private void initSubtaskState(Collection<SubtaskInstanceEntity> subtasks, int concurrency) {
        int rdyCount = 0;
        for (SubtaskInstanceEntity subtask : subtasks) {
            if (concurrency > 0 && rdyCount >= concurrency) {
                break;
            }
            if (subtask.getState() != SubtaskState.PENDING) {
                continue;
            }
            Set<SubtaskInstanceEntity> dependencies = subtask.getUpstreams();
            if (dependencies.stream().allMatch(entity -> entity.getState() == SubtaskState.SUCCESSFUL)) {
                subtask.setState(SubtaskState.READY);
                ++rdyCount;
            }
        }
    }

    private TaskInstanceEntity buildTaskInstance(Long taskDefinitionId, Template template, Argument argument,
            TaskType taskType, String creator) {
        ExceptionUtils.unExpected(null != template, "Template is null");
        TaskInstanceEntity entity = new TaskInstanceEntity(template);
        if (argument == null) {
            argument = new Argument();
        }
        entity.generateSubtasks(argument);

        argument.put(ContextKey.PROHIBIT_ROLLBACK, template.isProhibitRollback());

        entity.setArgument(argument);
        entity.setStartTime(OffsetDateTime.now());
        Optional.ofNullable(argument.get(ContextKey.OB_TENANT_ID.getValue()))
                .map(Long::parseLong)
                .ifPresent(entity::setObTenantId);
        entity.setExecutor(executor);
        entity.setOperation(TaskOperation.EXECUTE);
        entity.setType(taskType);
        if (StringUtils.isBlank(TraceUtils.getTraceId())) {
            TraceUtils.trace();
        }
        entity.setTraceId(TraceUtils.getTraceId());
        entity.setCreator(creator);
        entity.setTaskDefinitionId(Optional.ofNullable(taskDefinitionId).orElse(-1L));
        return entity;
    }

    private boolean isScheduleDurationArrived(String startTime, String endTime, ChronoUnit unit) {
        if (startTime == null && endTime == null) {
            return true;
        }

        if (unit != ChronoUnit.DAYS) {
            log.warn("Unsupported time unit. unit={}", unit.name());
            return false;
        }
        OffsetDateTime now = OffsetDateTime.now();
        Function<String, OffsetDateTime> function =
                s -> OffsetDateTime.of(LocalDate.parse(s).atStartOfDay(), now.getOffset());

        Range<OffsetDateTime> range;
        if (StringUtils.isBlank(startTime)) {
            range = Range.lessThan(function.apply(endTime));
        } else if (StringUtils.isBlank(endTime)) {
            range = Range.atLeast(function.apply(startTime));
        } else {
            range = Range.closedOpen(function.apply(startTime), function.apply(endTime));
        }
        return range.contains(now);
    }

    private TaskReflectMeta getTaskMeta(TaskDefinitionEntity scheduledTask) {
        String templateName = scheduledTask.getTemplateName();
        Optional<TaskTemplateEntity> templateOpt = taskTemplateRepository.findByName(templateName);
        ExceptionUtils.illegalArgs(templateOpt.isPresent(), "Unrecognized template name", templateName);
        TaskTemplateEntity templateEntity = templateOpt.get();

        Template template = null;
        Argument arguments = null;
        TaskType taskType = null;
        boolean needSchedule = true;
        try {
            Class clazz = ClassUtils.loadClass(templateEntity.getAction());
            Objects.requireNonNull(clazz);
            if (Objects.requireNonNull(templateEntity.getType()) == TemplateType.SCHEDULE) {
                ISchedule schedule = ((ISchedule) (clazz.newInstance()));
                template = schedule.getTemplate();
                arguments = schedule.getArgument();
                taskType = TaskType.SYS_SCHEDULED;
                needSchedule = schedule.ready();
            } else {
                ExceptionUtils.illegalArgs(false, "Unrecognized template type", templateEntity.getType());
            }
        } catch (Exception e) {
            log.warn("failed to get template for class {}, got exception: {}", templateEntity.getAction(), e);
        }
        return TaskReflectMeta.of(template, arguments, taskType, needSchedule);
    }

    @Data
    private static class TaskReflectMeta {

        private Template template;
        private Argument argument;
        private TaskType taskType;
        private boolean ready = true;

        private static TaskReflectMeta of(Template template, Argument argument, TaskType taskType,
                boolean needSchedule) {
            TaskReflectMeta meta = new TaskReflectMeta();
            meta.setTemplate(template);
            meta.setArgument(argument);
            meta.setTaskType(taskType);
            meta.setReady(needSchedule);
            return meta;
        }
    }

}
