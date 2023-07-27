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

package com.oceanbase.ocp.obops.internal.host.util;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.common.pattern.Retry;
import com.oceanbase.ocp.common.util.MapUtils;
import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.executor.model.agent.request.RestartAgentRequest;
import com.oceanbase.ocp.executor.model.task.AsyncTaskResult;
import com.oceanbase.ocp.executor.model.task.TaskStatus;
import com.oceanbase.ocp.executor.model.task.request.GetTaskStatusRequest;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.util.ContextUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentAsyncTaskHelper {

    public static void restartObAgent(AgentExecutor executor, RestartAgentRequest request) {
        ExceptionUtils.require(StringUtils.isNotEmpty(request.getTaskToken()),
                ErrorCodes.COMPUTE_HOST_AGENT_TASK_TOKEN_NOT_SET);
        AsyncTaskResult result = executor.restartAgent(request);
        Duration timeout = Duration.ofMinutes(5);
        waitForExecuteFinish(executor, result.getTaskToken(), 3, timeout);
    }

    public static void waitForExecuteFinish(AgentExecutor executor, String taskToken, int intervalSeconds,
            Duration timeout) {
        TaskStatus taskStatus = wait0(executor, taskToken, intervalSeconds, timeout);
        checkSuccess(taskToken, taskStatus);
    }

    public static void rollback(AgentExecutor executor, Context ctx, int intervalSeconds, int retryTimes) {
        String taskToken = getTokenFromContext(ctx);
        if (StringUtils.isEmpty(taskToken)) {
            log.warn("agent taskToken not found in context, context:{}, do nothing on rollback", ctx);
            return;
        }
        // wait for rollback finish
        waitForRollbackFinish(executor, taskToken, intervalSeconds, retryTimes);
    }

    public static void putTokenToContext(String taskToken, Context ctx) {
        Long subTaskInstanceId = ContextUtils.getSubTaskInstanceId(ctx);
        String jsonMap = ctx.get(ContextKey.AGENT_ASYNC_TASK_TOKEN_MAP);
        Map<Long, String> tokenMap = new HashMap<>(2);
        if (!StringUtils.isEmpty(jsonMap)) {
            Map<Long, String> map = MapUtils.toMap(jsonMap, Long.class, String.class);
            assert map != null;
            tokenMap.putAll(map);
        }
        tokenMap.put(subTaskInstanceId, taskToken);
        ctx.put(ContextKey.AGENT_ASYNC_TASK_TOKEN_MAP, JsonUtils.toJsonString(tokenMap));
    }

    public static String getTokenFromContext(Context ctx) {
        Long subTaskInstanceId = ContextUtils.getSubTaskInstanceId(ctx);
        String jsonMap = ctx.get(ContextKey.AGENT_ASYNC_TASK_TOKEN_MAP);
        if (StringUtils.isEmpty(jsonMap)) {
            return null;
        }
        Map<Long, String> tokenMap = MapUtils.toMap(jsonMap, Long.class, String.class);
        assert tokenMap != null;
        return tokenMap.get(subTaskInstanceId);
    }

    private static void checkSuccess(String taskToken, TaskStatus taskStatus) {
        if (!taskStatus.isSuccess()) {
            log.error("failed to execute async task, task token :{}, err: {}", taskToken, taskStatus.getErr());
            ExceptionUtils.require(taskStatus.isSuccess(), ErrorCodes.COMMON_UNEXPECTED, "agent task is failed",
                    taskStatus.getResult());
        }
    }

    private static TaskStatus wait0(AgentExecutor executor, String taskToken, int intervalSeconds,
            Duration timeout) {
        GetTaskStatusRequest request = GetTaskStatusRequest.builder().taskToken(taskToken).build();
        return Retry.executeUntilWithTimeout(() -> {
            try {
                TaskStatus status = executor.getTaskStatus(request);
                log.info("try to request task result(EXECUTE), result:{}", status);
                return status;
            } catch (Exception ex) {
                log.warn("Failed to execute agent task, reason:{}, cause:{}", ex.getMessage(), ex.getCause());
                return TaskStatus.builder().ok(false).finished(false).build();
            }
        }, status -> status.isSuccess() || status.isFailed(), intervalSeconds, timeout);
    }

    private static void waitForRollbackFinish(AgentExecutor executor, String taskToken, int intervalSeconds,
            int retryTimes) {
        GetTaskStatusRequest request = GetTaskStatusRequest.builder().taskToken(taskToken).build();
        TaskStatus taskStatus = Retry.executeUntilWithLimit(() -> {
            TaskStatus status = executor.getTaskStatus(request);
            log.info("try to request task result(ROLLBACK), result:{}", status);
            ExceptionUtils.require(!status.isSuccess(), ErrorCodes.COMMON_UNEXPECTED, "already success",
                    "this task is already success, please set it success and continue!");
            return status;
        }, TaskStatus::isRollbackFinished, intervalSeconds, retryTimes);
        ExceptionUtils.require(taskStatus.isRollbackSuccess(), ErrorCodes.COMMON_UNEXPECTED,
                "agent task rollback failed", taskStatus.getErr());
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

}
