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

package com.oceanbase.ocp.executor.executor;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import com.oceanbase.ocp.executor.config.Configuration;
import com.oceanbase.ocp.executor.internal.connector.ConnectProperties;
import com.oceanbase.ocp.executor.internal.constant.AgentApiPath;
import com.oceanbase.ocp.executor.internal.template.HttpTemplate;
import com.oceanbase.ocp.executor.internal.util.PingUtils;
import com.oceanbase.ocp.executor.model.agent.AgentStatus;
import com.oceanbase.ocp.executor.model.agent.request.RestartAgentRequest;
import com.oceanbase.ocp.executor.model.compute.AgentQueryLogResult;
import com.oceanbase.ocp.executor.model.compute.HostInfoResult;
import com.oceanbase.ocp.executor.model.compute.request.AgentDownloadLogRequest;
import com.oceanbase.ocp.executor.model.compute.request.AgentQueryLogRequest;
import com.oceanbase.ocp.executor.model.file.GetRealPathRequest;
import com.oceanbase.ocp.executor.model.file.RealPath;
import com.oceanbase.ocp.executor.model.monitor.HttpConfig;
import com.oceanbase.ocp.executor.model.task.AsyncTaskResult;
import com.oceanbase.ocp.executor.model.task.TaskStatus;
import com.oceanbase.ocp.executor.model.task.request.GetTaskStatusRequest;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentExecutor {

    private final HttpTemplate httpTemplate;

    @Getter
    private final ConnectProperties connectProperties;

    public AgentExecutor(ConnectProperties connectProperties, Configuration configuration) {
        this.connectProperties = connectProperties;
        this.httpTemplate = new HttpTemplate(connectProperties, configuration);
    }

    /**
     * Get host time.
     */
    public OffsetDateTime getHostTime() {
        return httpTemplate.get(AgentApiPath.GET_AGENT_TIME, OffsetDateTime.class).getData();
    }

    /**
     * Get clock diff between express server and observer.
     */
    public Optional<Long> getClockDiffMillis() {
        Optional<Long> optional = PingUtils.getAvgRttByHttp(this, 5, 3);
        if (!optional.isPresent()) {
            log.warn("failed to get rtt by agent executor ,address:{},port:{}", connectProperties.getHostAddress(),
                    connectProperties.getHttpPort());
            return Optional.empty();
        }
        long hostTime = getHostTime().toInstant().toEpochMilli();
        return Optional.of(OffsetDateTime.now().toInstant().toEpochMilli() - (hostTime - optional.get()));
    }

    /**
     * Get the real path of symbolic link.
     */
    public RealPath getRealPath(GetRealPathRequest request) {
        return httpTemplate.post(AgentApiPath.GET_REAL_PATH, RealPath.class, request).getData();
    }

    /**
     * Restart ob agent.
     */
    public AsyncTaskResult restartAgent(RestartAgentRequest request) {
        return httpTemplate.post(AgentApiPath.RESTART_AGENT, AsyncTaskResult.class, request).getData();
    }

    /**
     * Get ob agent status.
     */
    public AgentStatus agentStatus() {
        return httpTemplate.get(AgentApiPath.AGENT_STATUS, AgentStatus.class).getData();
    }

    /**
     * Get agent async task status.
     */
    public TaskStatus getTaskStatus(GetTaskStatusRequest request) {
        return httpTemplate.post(AgentApiPath.GET_TASK_STATUS, TaskStatus.class, request).getData();
    }

    /**
     * Query agent log.
     */
    public AgentQueryLogResult queryLog(AgentQueryLogRequest request) {
        return httpTemplate.post(AgentApiPath.QUERY_LOG, AgentQueryLogResult.class, request).getData();
    }

    /**
     * Download log with specified arguments.
     */
    public Response downloadLog(AgentDownloadLogRequest request, HttpConfig config) {
        return httpTemplate.postRaw(AgentApiPath.DOWNLOAD_LOG, request, config);
    }

    /**
     * Get ob server host info.
     */
    public HostInfoResult getRemoteHostInfo() {
        return httpTemplate.post(AgentApiPath.GET_HOST_INFO, HostInfoResult.class, Collections.emptyMap()).getData();
    }

}
