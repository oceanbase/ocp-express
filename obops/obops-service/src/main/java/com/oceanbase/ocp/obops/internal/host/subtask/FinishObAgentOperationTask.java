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

import com.oceanbase.ocp.core.constants.ObAgentOperation;
import com.oceanbase.ocp.core.util.BeanUtils;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.util.ContextUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinishObAgentOperationTask extends AgentSubtask {

    @Override
    public String getName() {
        return "Finish OB agent operation";
    }

    @Override
    public int getTimeout() {
        return 60;
    }

    @Override
    public void cancel() {}

    @Override
    public Context run(Context context) {
        Long obAgentId = ContextUtils.getContextLongValue(context, ContextKey.OB_AGENT_ID);
        ObAgentService service = BeanUtils.getBean(ObAgentService.class);
        service.updateOperation(obAgentId, ObAgentOperation.EXECUTE);
        return context;
    }

    @Override
    public Context rollback(Context context) {
        log.info("[FinishOBAgentOperationTask] rollback do nothing");
        return context;
    }
}
