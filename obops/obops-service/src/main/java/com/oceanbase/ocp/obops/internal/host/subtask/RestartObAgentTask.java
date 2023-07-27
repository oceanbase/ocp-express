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

package com.oceanbase.ocp.obops.internal.host.subtask;

import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.executor.model.agent.request.RestartAgentRequest;
import com.oceanbase.ocp.obops.internal.host.util.AgentAsyncTaskHelper;
import com.oceanbase.ocp.task.model.Context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestartObAgentTask extends AgentSubtask {

    @Override
    public String getName() {
        return "Restart OB agent";
    }

    @Override
    public int getTimeout() {
        return 600;
    }

    @Override
    public Context run(Context ctx) {
        log.info("Begin restart OB agent..");
        AgentExecutor executor = getAgentExecutor(ctx);

        RestartAgentRequest request = RestartAgentRequest.builder().build();
        String taskToken = AgentAsyncTaskHelper.generateToken();
        request.setTaskToken(taskToken);
        AgentAsyncTaskHelper.putTokenToContext(taskToken, ctx);
        AgentAsyncTaskHelper.restartObAgent(executor, request);
        log.info("Success to restart ob agent.");
        return ctx;
    }

    @Override
    public Context rollback(Context ctx) {
        String token = AgentAsyncTaskHelper.getTokenFromContext(ctx);
        if (StringUtils.isNotEmpty(token)) {
            log.info("find agent task token from context, token:{}, begin to rollback", token);
            AgentAsyncTaskHelper.rollback(getAgentExecutor(ctx), ctx, 3, 60);
        }
        return ctx;
    }

    @Override
    public void cancel() {

    }

}
