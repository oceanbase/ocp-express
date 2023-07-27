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

package com.oceanbase.ocp.obsdk.operator.stats.model;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import lombok.Builder;
import lombok.Data;

@Data
public class ObUnitPartitionStats {

    private String serverIp;
    private long obTenantId;

    private long totalReplicaCount;
    private long leaderReplicaCount;
    private long followerReplicaCount;
    private long fullReplicaCount;
    private long logonlyReplicaCount;
    private long readonlyReplicaCount;

    private long dataSize;
    private long requiredSize;

    public static ObUnitPartitionStats sum(Collection<ObUnitPartitionStats> statsList) {
        ObUnitPartitionStats result = new ObUnitPartitionStats();
        result.totalReplicaCount = 0L;
        result.leaderReplicaCount = 0L;
        result.followerReplicaCount = 0L;
        result.fullReplicaCount = 0L;
        result.logonlyReplicaCount = 0L;
        result.readonlyReplicaCount = 0L;
        result.dataSize = 0L;
        result.requiredSize = 0L;
        for (ObUnitPartitionStats stats : CollectionUtils.emptyIfNull(statsList)) {
            result.add(stats);
        }
        return result;
    }

    public ObUnitPartitionStats add(ObUnitPartitionStats stats) {
        Validate.notNull(stats, "parameter stats require not null");
        this.totalReplicaCount += stats.totalReplicaCount;
        this.leaderReplicaCount += stats.leaderReplicaCount;
        this.followerReplicaCount += stats.followerReplicaCount;
        this.fullReplicaCount += stats.fullReplicaCount;
        this.logonlyReplicaCount += stats.logonlyReplicaCount;
        this.readonlyReplicaCount += stats.readonlyReplicaCount;
        this.dataSize += stats.dataSize;
        this.requiredSize += stats.requiredSize;
        return this;
    }

    public GroupByKey getGroupByKey() {
        return GroupByKey.builder().obTenantId(obTenantId).serverIp(serverIp).build();
    }

    @Builder
    @Data
    public static final class GroupByKey implements Serializable {

        private static final long serialVersionUID = -8218501220571693679L;
        private String serverIp;
        private long obTenantId;
    }
}
