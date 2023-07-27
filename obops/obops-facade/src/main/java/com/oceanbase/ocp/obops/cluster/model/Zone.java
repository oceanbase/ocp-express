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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.oceanbase.ocp.core.ob.constant.ObZoneInnerStatus;
import com.oceanbase.ocp.core.ob.constant.ObZoneStatus;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;

import lombok.Data;

@Data
public class Zone {

    @Size(min = 2, max = 32)
    private String name;

    @NotNull
    @Size(min = 2, max = 32)
    private String idcName;

    @Size(min = 2, max = 32)
    private String regionName;

    @NotNull
    @Valid
    private List<Server> servers = new ArrayList<>();

    @Valid
    private ObZoneStatus status = ObZoneStatus.RUNNING;

    @Valid
    private ObZoneInnerStatus innerStatus;

    private Long clusterId;

    private Long obClusterId;

    private RootServer rootServer;

    private Integer serverCount;

    private Integer hostCount;

    private PerformanceStats performanceStats;

    @Valid
    private List<String> availableOperations;

    public static Zone fromObZone(ObZone obZone) {
        Zone zone = new Zone();
        zone.setName(obZone.getZone());
        zone.setIdcName(obZone.getIdc());
        zone.setInnerStatus(ObZoneInnerStatus.fromValue(obZone.getStatus()));
        zone.setRegionName(obZone.getRegion());
        return zone;
    }
}
