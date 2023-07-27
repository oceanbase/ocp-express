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
package com.oceanbase.ocp.obops.host.model;

import java.time.OffsetDateTime;
import java.util.List;

import com.oceanbase.ocp.core.constants.ObAgentOperation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ObAgentDetail {

    private long id;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;

    private String ip;

    private Integer mgrPort;

    private Integer monPort;

    private Integer obServerSvrPort;

    private ObAgentOperation operation;

    private List<ObAgentProcess> obAgentProcesses;

    private boolean ready;

    private String version;

}
