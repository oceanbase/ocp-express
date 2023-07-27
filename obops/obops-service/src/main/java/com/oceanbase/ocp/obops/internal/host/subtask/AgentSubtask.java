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

import java.util.Optional;

import com.oceanbase.ocp.core.executor.AgentExecutorFactory;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.obops.host.model.ObAgentDetail;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;
import com.oceanbase.ocp.task.util.ContextUtils;

public abstract class AgentSubtask implements Subtask {

    ObAgentDetail getDetail(Context ctx) {
        Long obAgentId = ContextUtils.getContextLongValue(ctx, ContextKey.OB_AGENT_ID);

        ObAgentService service = BeanUtils.getBean(ObAgentService.class);

        Optional<ObAgentDetail> agentDetail = service.findById(obAgentId);
        ExceptionUtils.notFound(agentDetail.isPresent(), "ob-agent", "id=" + obAgentId);

        return agentDetail.get();
    }

    AgentExecutor getAgentExecutor(Context ctx) {
        ObAgentDetail detail = getDetail(ctx);
        ExceptionUtils.illegalArgs(detail.isReady(), ErrorCodes.COMPUTE_HOST_AGENT_STATUS_NOT_VALID, "ob-agent",
                "id=" + ContextUtils.getContextLongValue(ctx, ContextKey.OB_AGENT_ID));
        return BeanUtils.getBean(AgentExecutorFactory.class).create(detail.getIp(), detail.getMgrPort());
    }


}
