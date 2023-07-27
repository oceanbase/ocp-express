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

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.SubtaskOperation;
import com.oceanbase.ocp.task.constants.SubtaskState;
import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;
import com.oceanbase.ocp.task.model.Context;
import com.oceanbase.ocp.task.runtime.Subtask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaSubtaskRunner implements Runner {

    @Override
    public SubtaskInstanceOverview run(SubtaskInstanceOverview o) {
        return doRun(o);
    }

    private SubtaskInstanceOverview doRun(SubtaskInstanceOverview o) {
        switch (o.getOperation()) {
            case EXECUTE:
                execute(o);
                break;
            case RETRY:
                retry(o);
                break;
            case ROLLBACK:
                rollback(o);
                break;
            case CANCEL:
                cancel(o);
                break;
            case ROLLBACK_SKIP:
                rollbackSkip(o);
                break;
            case SKIP:
            default:
                skip(o);
        }
        return o;
    }

    private void execute(SubtaskInstanceOverview o) {
        if (o.getOperation() == SubtaskOperation.SKIP) {
            return;
        }
        log.info("Run subtask, id={}, context={}, executor={}",
                o.getId(), o.getContext().getContextString(), HostUtils.getLocalIp());
        Subtask subtask = o.getSubtask();
        Context afterContext = subtask.run(o.getContext());
        if (subtask.prohibitRollback()) {
            afterContext.put(ContextKey.PROHIBIT_ROLLBACK, true);
        }
        o.setContext(afterContext);
        o.setState(SubtaskState.SUCCESSFUL);
    }

    private void retry(SubtaskInstanceOverview o) {
        if (o.getOperation() == SubtaskOperation.SKIP) {
            return;
        }
        log.info("Retry subtask, id={}, context={}, executor={}",
                o.getId(), o.getContextString(), HostUtils.getLocalIp());
        Context afterContext = o.getSubtask().retry(o.getContext());
        o.setContext(afterContext);
        o.setState(SubtaskState.SUCCESSFUL);
    }

    private void rollback(SubtaskInstanceOverview o) {
        log.warn("Rollback subtask, id={}, context={}, executor={}",
                o.getId(), o.getContextString(), HostUtils.getLocalIp());
        Context afterContext = o.getSubtask().rollback(o.getContext());
        o.setContext(afterContext);
        o.setState(SubtaskState.PENDING);
    }

    public void cancel(SubtaskInstanceOverview o) {
        if (o.getOperation() == SubtaskOperation.SKIP) {
            return;
        }
        log.warn("Cancel subtask, id={}, context={}, executor={}",
                o.getId(), o.getContextString(), HostUtils.getLocalIp());
        o.getSubtask().cancel();
        o.setState(SubtaskState.FAILED);
    }

    private void skip(SubtaskInstanceOverview o) {
        log.warn("Operation is {}, do nothing", o.getOperation());
        o.setState(SubtaskState.SUCCESSFUL);
    }

    private void rollbackSkip(SubtaskInstanceOverview o) {
        log.warn("Operation is {}, do nothing", o.getOperation());
        o.setState(SubtaskState.PENDING);
    }

}
