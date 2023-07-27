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
package com.oceanbase.ocp.partitioning.service;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import com.oceanbase.ocp.common.database.ConnectUserPart;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.ObAccessors;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTablePartition;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ConnectionMode;
import com.oceanbase.ocp.partitioning.PartitionRolloverProperties;
import com.oceanbase.ocp.partitioning.model.Partition;
import com.oceanbase.ocp.partitioning.policy.PartitionRolloverPolicy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionMetadataService {

    @Autowired
    private PartitionRolloverProperties properties;

    public ObAccessor createObAccessor(PartitionRolloverPolicy.Scope scope) {
        Validate.notNull(scope, "Scope require non-null");
        if (scope == PartitionRolloverPolicy.Scope.Metadb) {
            return ObAccessors.newObAccessor(dbConnectionProperties());
        } else {
            throw new IllegalStateException("Unreachable");
        }
    }

    public ConnectProperties dbConnectionProperties() {
        PartitionRolloverProperties.DbProperties p = properties.getDbProperties();
        ConnectUserPart part = ConnectUserPart.of(p.getUsername());
        ConnectionMode mode = isEmpty(part.cluster) ? ConnectionMode.DIRECT : ConnectionMode.PROXY;
        return ConnectProperties.builder()
                .connectionMode(mode)
                .address(p.getHost())
                .port(Integer.parseInt(p.getPort()))
                .username(part.username)
                .tenantName(part.tenant)
                .clusterName(part.cluster)
                .password(p.getPassword())
                .database(p.getDatabase())
                .build();
    }

    public List<Partition> listPartitions(ObAccessor accessor, String tableName) {
        List<ObTablePartition> partitions = accessor.object().listTablePartition(tableName);
        if (partitions == null || partitions.size() == 0) {
            return Collections.emptyList();
        }
        return partitions.stream()
                .map(it -> Partition.builder().name(it.getName()).build())
                .collect(Collectors.toList());
    }
}
