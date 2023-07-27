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

import com.oceanbase.ocp.core.constants.ObAgentOperation;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.obops.host.model.ObAgentDetail;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.util.ContextUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareRestartObAgentTask extends AgentSubtask {

    @Override
    public String getName() {
        return "Prepare restart OB agent";
    }

    @Override
    public int getTimeout() {
        return 60;
    }

    @Override
    public void cancel() {}

    @Override
    public Context run(Context context) {
        log.info("[PrepareRestartOBAgentTask] begin");

        Long obAgentId = ContextUtils.getContextLongValue(context, ContextKey.OB_AGENT_ID);

        ObAgentService service = BeanUtils.getBean(ObAgentService.class);

        Optional<ObAgentDetail> agentDetail = service.findById(obAgentId);
        ExceptionUtils.notFound(agentDetail.isPresent(), "ob-agent", "id=" + obAgentId);
        ObAgentDetail detail = agentDetail.get();

        ExceptionUtils.unExpected(detail.getOperation() == ObAgentOperation.EXECUTE,
                ErrorCodes.COMPUTE_HOST_AGENT_STATUS_NOT_VALID,
                detail.getId(), detail.getMgrPort());

        // update status
        service.updateOperation(obAgentId, ObAgentOperation.RESTARTING);

        log.info("[PrepareRestartOBAgentTask] end");
        return context;
    }

    @Override
    public Context rollback(Context context) {
        log.info("[PrepareRestartOBAgentTask] rollback begin");

        Long obAgentId = ContextUtils.getContextLongValue(context, ContextKey.OB_AGENT_ID);
        ObAgentService service = BeanUtils.getBean(ObAgentService.class);
        service.updateOperation(obAgentId, ObAgentOperation.EXECUTE);

        log.info("[PrepareRestartOBAgentTask] rollback end");
        return context;
    }
}
