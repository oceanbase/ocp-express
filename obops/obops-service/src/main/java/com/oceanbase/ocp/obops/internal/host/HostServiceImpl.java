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
package com.oceanbase.ocp.obops.internal.host;

import org.springframework.stereotype.Service;

import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.executor.model.compute.HostInfoResult;
import com.oceanbase.ocp.obops.host.HostService;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.obops.host.model.HostInfo;

@Service
public class HostServiceImpl implements HostService {

    private final ObAgentService agentService;

    public HostServiceImpl(ObAgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    public HostInfo getHostInfo(String ip, int obSvrPort) {
        AgentExecutor exec = agentService.getAgentExecutor(ip, obSvrPort);
        HostInfo.HostInfoBuilder builder = HostInfo.builder();
        exec.getClockDiffMillis().ifPresent(builder::clockDiffMillis);
        HostInfoResult r = exec.getRemoteHostInfo();
        builder.hostname(r.getHostName())
                .os(r.getOs())
                .architecture(r.getArchitecture())
                .cpuCount(r.getCpuCount())
                .totalMemory(r.getTotalMemory());
        return builder.build();
    }

}
