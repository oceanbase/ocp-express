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

package com.oceanbase.ocp.obops.cluster.model;

import java.time.OffsetDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.oceanbase.ocp.common.util.time.TimeUtils;
import com.oceanbase.ocp.core.ob.constant.ObServerStatus;
import com.oceanbase.ocp.obops.resource.model.ServerResourceStats;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerInnerStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Server {

    private Long id;

    private String ip;

    private Integer port;

    private Integer sqlPort;

    private String version;

    private String architecture;

    private Boolean withRootserver;

    private ObServerStatus status = ObServerStatus.RUNNING;

    private ObServerInnerStatus innerStatus;

    @Size(max = 64)
    private String zoneName;

    private OffsetDateTime startTime;

    private OffsetDateTime stopTime;

    private ServerResourceStats stats;

    private PerformanceStats performanceStats;

    @Valid
    private List<String> availableOperations;

    private String dataPath;

    private String logPath;

    @JsonIgnore
    public String getAddress() {
        if (ip == null || port == null) {
            return null;
        }
        return ip + ":" + port;
    }

    public static Server fromObServer(ObServer obServer) {
        return Server.builder()
                .id(obServer.getId())
                .ip(obServer.getSvrIp())
                .innerStatus(ObServerInnerStatus.fromValue(obServer.getStatus()))
                .sqlPort(obServer.getInnerPort())
                .port(obServer.getSvrPort())
                .startTime(TimeUtils.usToMicroUtc(obServer.getStartServiceTime()))
                .stopTime(TimeUtils.usToMicroUtc(obServer.getStopTime()))
                .withRootserver(obServer.getWithRootserver())
                .version(obServer.getBuildVersion())
                .build();
    }
}
