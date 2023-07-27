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

package com.oceanbase.ocp.obops.resource.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClusterUnitViewOfServer {

    private String serverIp;

    private Integer serverPort;

    private String hostType;

    private String zone;

    private String region;

    private Long totalMemorySizeByte;

    private Long memorySizeAssignedByte;

    private Double memoryAssignedPercent;

    private Double totalCpuCount;

    private Double cpuCountAssigned;

    private Double cpuAssignedPercent;

    private Long totalDiskSizeByte;

    private Long diskUsedByte;

    private Double diskUsedPercent;

    private Long unitCount;

    private Long unusedUnitCount;

    private List<ClusterUnitViewOfUnit> unitInfos;
}
