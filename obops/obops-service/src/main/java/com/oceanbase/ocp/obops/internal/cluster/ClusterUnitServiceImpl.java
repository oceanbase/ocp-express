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

package com.oceanbase.ocp.obops.internal.cluster;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.obops.cluster.ClusterUnitService;
import com.oceanbase.ocp.obops.cluster.model.ClusterUnitSpecLimit;
import com.oceanbase.ocp.obops.internal.parameter.ObParameterService;
import com.oceanbase.ocp.obops.internal.parameter.model.ObParameterValue;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClusterUnitServiceImpl implements ClusterUnitService {

    private static final int MIN_CPU = 1;

    private static final int MIN_MEMORY = 1;

    private static final String MIN_MEMORY_PARAMETER = "__min_full_resource_pool_memory";

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private ObParameterService obParameterService;

    @Override
    public ClusterUnitSpecLimit getClusterUnitSpecLimit() {

        Optional<ObParameterValue> minMemoryParameter =
                obParameterService.getHiddenClusterParameter(MIN_MEMORY_PARAMETER);

        Integer cpuLimit = MIN_CPU;

        Integer memoryLimit = minMemoryParameter
                .map(ObParameterValue::getOneValue)
                .map(Long::parseLong)
                .map(this::bytesToGigaBytes)
                .map(m -> Math.max(m, MIN_MEMORY))
                .orElse(MIN_MEMORY);

        return ClusterUnitSpecLimit.builder()
                .cpuLowerLimit(cpuLimit)
                .memoryLowerLimit(memoryLimit)
                .clusterName(managedCluster.clusterName())
                .obClusterId(managedCluster.obClusterId())
                .build();
    }

    private int bytesToGigaBytes(long bytes) {
        return (int) (bytes / 1024 / 1024 / 1024);
    }
}
