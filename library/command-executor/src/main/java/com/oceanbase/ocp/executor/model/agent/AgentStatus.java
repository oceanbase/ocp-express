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

package com.oceanbase.ocp.executor.model.agent;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentStatus {

    /**
     * Agent daemon process state.
     */
    private AgentState state;

    /**
     * Is agent process ready.
     */
    private boolean ready;

    /**
     * Agent process version.
     */
    private String version;

    /**
     * Process pid.
     */
    private long pid;

    /**
     * Unix socket path.
     */
    private String socket;

    /**
     * ob_mgragent: Agent management process info. <br>
     * ob_monagent: Agent monitor process info.
     */
    private Map<String, AgentServiceStatus> services;
}
