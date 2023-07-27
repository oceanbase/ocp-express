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
package com.oceanbase.ocp.task.engine.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.context.MessageSource;

import com.oceanbase.ocp.common.util.EncodeUtils;
import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.common.util.time.TimeUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.exception.OcpException;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.NodeType;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.constants.TaskType;
import com.oceanbase.ocp.task.dao.SubtaskLogRepo;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.util.SubtaskOutputStream;
import com.oceanbase.ocp.task.engine.util.ThreadPrintStream;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;
import com.oceanbase.ocp.task.entity.SubtaskLogEntity;
import com.oceanbase.ocp.task.hook.SubtaskHook;
import com.oceanbase.ocp.task.model.Context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunnerFactory {

    /**
     * Separator of subtask log.
     */
    private static final String SEPARATOR_FORMAT = "############{%s}{%s}############";

    /**
     * Max length of one segment of subtask log, restricted by database column type.
     */
    private static final int MAX_CONTENT_LEN = 262144;

    private final Map<NodeType, List<SubtaskHook<Long>>> preHookMap = new ConcurrentHashMap<>();
    private final Map<NodeType, List<SubtaskHook<Long>>> postHookMap = new ConcurrentHashMap<>();

    private final SubtaskLogRepo subtaskLogRepo;
    private final String taskLogPath;
    private static final String LOG_DATE_FORMAT = "yyyyMMdd";

    public RunnerFactory(SubtaskCoordinatorConfig c) {
        this.taskLogPath = c.getSubtaskLogPath();
        this.subtaskLogRepo = c.getSubtaskLogRepo();
        this.addAllJavaPreHooks(c.getPreSubtaskHooks());
        this.addAllJavaPostHooks(c.getPostSubtaskHooks());
    }

    public synchronized void addAllJavaPreHooks(List<SubtaskHook<Long>> preTaskHooks) {
        if (CollectionUtils.isEmpty(preTaskHooks)) {
            return;
        }
        List<SubtaskHook<Long>> hooks =
                this.preHookMap.getOrDefault(NodeType.JAVA_TASK, new ArrayList<>());
        hooks.addAll(preTaskHooks);
        hooks.sort((o1, o2) -> o2.getOrder().compareTo(o1.getOrder()));
        this.preHookMap.put(NodeType.JAVA_TASK, hooks);
    }

    public synchronized void addAllJavaPostHooks(List<SubtaskHook<Long>> postTaskHooks) {
        if (CollectionUtils.isEmpty(postTaskHooks)) {
            return;
        }
        List<SubtaskHook<Long>> hooks = this.postHookMap.getOrDefault(NodeType.JAVA_TASK, new ArrayList<>());
        hooks.addAll(postTaskHooks);
        this.postHookMap.put(NodeType.JAVA_TASK, hooks);
    }

    public SubtaskInstanceOverview run(TaskType taskType, String traceId, SubtaskInstanceOverview subtask) {
        return redirectOutputIfNotSysSchedule(taskType, traceId, subtask, this::doRun);
    }

    private SubtaskInstanceOverview doRun(SubtaskInstanceOverview subtask) {
        addLogSeparator(subtask.getId(), subtask.getRunTime(), subtask.getOperation());
        NodeType nodeType = subtask.getNodeType();
        executeHooks(preHookMap.get(nodeType), subtask.getId());
        try {
            subtask.getContext().put(ContextKey.TASK_OPERATION, subtask.getOperation().getValue());
            subtask.getContext().put(ContextKey.SUB_TASK_INSTANCE_ID, String.valueOf(subtask.getId()));
            subtask = newRunner(nodeType).run(subtask);
        } catch (OcpException e) {
            subtask.setState(SubtaskState.FAILED);
            MessageSource messageSource = BeanUtils.getBean(MessageSource.class);
            String message = messageSource.getMessage(e.getErrorCode().getKey(), e.getArgs(), Locale.US);
            log.warn("Execute task failed, subtask={}, failedMessage={}", subtask, message, e);
        } catch (Throwable e) {
            subtask.setState(SubtaskState.FAILED);
            log.warn("Execute task failed, subtask=" + subtask, e);
        } finally {
            if (subtask.getContext() != null) {
                subtask.getContext().remove(ContextKey.TASK_OPERATION);
                subtask.getContext().remove(ContextKey.SUB_TASK_INSTANCE_ID);
            }
            executeHooks(postHookMap.get(nodeType), subtask.getId());
        }
        validateContextLength(subtask.getContext());
        return subtask;
    }

    private void validateContextLength(Context c) {
        Function<Context, Integer> lenFunc = context -> Optional.of(context).map(JsonUtils::toJsonString)
                .map(s -> s.getBytes(StandardCharsets.UTF_8))
                .map(EncodeUtils::base64EncodeToString)
                .map(s -> s.getBytes(StandardCharsets.UTF_8))
                .map(bytes -> bytes.length)
                .orElse(0);
        if (lenFunc.apply(c) > MAX_CONTENT_LEN) {
            log.warn("Context length is two large, persistence may fail.");
        }
    }

    private void addLogSeparator(long subtaskInstanceId, int runTime, SubtaskOperation operation) {
        String splitter = String.format(SEPARATOR_FORMAT, operation.name(), OffsetDateTime.now());
        SubtaskLogEntity entity = new SubtaskLogEntity();
        entity.setSubtaskInstanceId(subtaskInstanceId);
        entity.setRunTime(runTime);
        entity.setLogContent(splitter.getBytes(StandardCharsets.UTF_8));
        subtaskLogRepo.save(entity);
    }

    public void executeAndPrintLog(SubtaskInstanceOverview subtask, Consumer<SubtaskInstanceOverview> consumer) {
        Long taskId = subtask.getId();
        String logFileName =
                String.format("%s/%s/%s.log", taskLogPath, TimeUtils.getDateString(0, LOG_DATE_FORMAT), taskId);
        try (FileOutputStream fos = FileUtils.openOutputStream(new File(logFileName), true);
                PrintStream stream = new PrintStream(
                        new SubtaskOutputStream(subtask.getId(), subtask.getRunTime(), fos, subtaskLogRepo))) {
            ((ThreadPrintStream) System.out).setThreadOut(stream);
            consumer.accept(subtask);
        } catch (Exception e) {
            log.error("Set thread out failed, message={}", e.getMessage(), e);
            consumer.accept(subtask);
        }
    }

    private SubtaskInstanceOverview redirectOutputIfNotSysSchedule(TaskType taskType, String traceId,
            SubtaskInstanceOverview subtask, Function<SubtaskInstanceOverview, SubtaskInstanceOverview> function) {
        TraceUtils.span(Collections.singletonMap(TraceUtils.TRACE_ID, traceId));
        Long taskId = subtask.getId();
        if (TaskType.SYS_SCHEDULED.equals(taskType)) {
            log.debug("skip redirect output for system schedule");
            return function.apply(subtask);
        }
        String logFileName =
                String.format("%s/%s/%s.log", taskLogPath, TimeUtils.getDateString(0, LOG_DATE_FORMAT), taskId);
        log.debug("Redirect subtask, id={}, fileName={}", taskId, logFileName);
        try (FileOutputStream fos = FileUtils.openOutputStream(new File(logFileName), true);
                PrintStream stream = new PrintStream(
                        new SubtaskOutputStream(subtask.getId(), subtask.getRunTime(), fos, subtaskLogRepo))) {
            ((ThreadPrintStream) System.out).setThreadOut(stream);
            return function.apply(subtask);
        } catch (Exception e) {
            log.error("Set thread out failed, message={}", e.getMessage(), e);
            return function.apply(subtask);
        }
    }

    private void executeHooks(List<SubtaskHook<Long>> hooks, Long subtaskId) {
        if (CollectionUtils.isEmpty(hooks)) {
            return;
        }
        hooks.forEach(hook -> {
            try {
                hook.getConsumer().accept(subtaskId);
            } catch (Exception ex) {
                log.warn("Execute hook failed, ex:", ex);
            }
        });
    }

    private Runner newRunner(NodeType nodeType) {
        Runner runner = null;
        if (nodeType == NodeType.JAVA_TASK) {
            runner = new JavaSubtaskRunner();
        } else {
            ExceptionUtils.illegalArgs(false, nodeType);
        }
        return runner;
    }

}
