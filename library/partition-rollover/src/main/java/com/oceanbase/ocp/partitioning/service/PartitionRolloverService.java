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

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.base.Joiner;

import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.object.model.AlterTableInput;
import com.oceanbase.ocp.partitioning.PartitionRolloverProperties;
import com.oceanbase.ocp.partitioning.model.Partition;
import com.oceanbase.ocp.partitioning.policy.PartitionRolloverPolicy;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionRolloverService implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private PartitionRolloverProperties properties;

    @Autowired
    private PartitionMetadataService metadataService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<String, PartitionRolloverPolicy> policies = getPolicies();
        if (policies.size() == 0) {
            return;
        }
        policies.forEach((k, v) -> {
            validate(k, v);
            log.info("Loading policy, scope={}, tableName={}", v.getScope(), v.getTableName());
        });
        validateDuplication(policies.values());
    }

    /**
     * Check if there are duplicate policies for same table.
     */
    private void validateDuplication(Collection<PartitionRolloverPolicy> policies) {
        Map<PartitionRolloverPolicy.Scope, List<PartitionRolloverPolicy>> groups =
                policies.stream().collect(Collectors.groupingBy(PartitionRolloverPolicy::getScope));
        groups.forEach((k, v) -> v.stream().collect(Collectors.toMap(
                it -> it.getTableName().toLowerCase(),
                it -> it,
                (a, b) -> {
                    throw new IllegalStateException(String.format("Duplicate for table '%s'", a.getTableName()));
                })));
    }

    private Map<String, PartitionRolloverPolicy> getPolicies() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBeansOfType(PartitionRolloverPolicy.class);
    }

    private void validate(String bean, PartitionRolloverPolicy policy) {
        Validate.notNull(policy.getScope(), "Scope not specified: beanName=%s", bean);
        Validate.notBlank(policy.getTableName(), "Table name not specified: beanName=%s", bean);
        Validate.notNull(policy.getPartitioningDefinition(), "Partitioning definition not specified: beanName=%s",
                bean);
    }

    public void watchAll() {
        Map<PartitionRolloverPolicy.Scope, List<PartitionRolloverPolicy>> policiesByScope =
                getPolicies().values().stream().collect(Collectors.groupingBy(PartitionRolloverPolicy::getScope));
        for (Map.Entry<PartitionRolloverPolicy.Scope, List<PartitionRolloverPolicy>> entry : policiesByScope
                .entrySet()) {
            watchGroup(entry.getKey(), entry.getValue());
        }
    }

    private void watchGroup(PartitionRolloverPolicy.Scope scope, List<PartitionRolloverPolicy> policies) {
        log.info("Start watching scope, scope={}", scope);
        try {
            ObAccessor accessor = metadataService.createObAccessor(scope);
            for (PartitionRolloverPolicy policy : policies) {
                try {
                    watchTable(accessor, policy);
                } catch (Exception e) {
                    log.warn("Watching table error, scope={}, table={}, exception={}",
                            scope, policy.getTableName(), e);
                }
            }
        } catch (Exception e) {
            log.error("Watching scope error, scope={}, exception={}", scope, e);
        } finally {
            log.info("Finish watching scope, scope=, scope={}", scope);
        }
    }

    private static final class ContextImpl implements PartitionRolloverPolicy.Context {

        private final Instant now;
        private final List<Partition> partitionList;
        private final Map<String, Partition> partitionMap;

        @Builder
        private ContextImpl(Instant now, List<Partition> partitionList) {
            this.now = now;
            this.partitionList = partitionList;
            this.partitionMap = partitionList.stream()
                    .collect(Collectors.toMap(it -> it.name, it -> it, (a, b) -> a, LinkedHashMap::new));
        }

        @Override
        public String toString() {
            return "Context{now=" + now + ", partitions=["
                    + Joiner.on(',').join(partitionList.stream().map(it -> it.name).collect(Collectors.toList()))
                    + "]}";
        }

        @Override
        public Instant now() {
            return now;
        }

        @Override
        public List<Partition> listPartitions() {
            return partitionList;
        }

        @Override
        public Partition getPartition(String name) {
            return partitionMap.get(name);
        }
    }

    private void watchTable(ObAccessor accessor, PartitionRolloverPolicy policy) {
        Instant now = accessor.info().now().toInstant();
        List<Partition> partitionList = metadataService.listPartitions(accessor, policy.getTableName());
        if (CollectionUtils.isEmpty(partitionList)) {
            log.info("Not a partition table or table not exists, scope={}, table={}", policy.getScope(),
                    policy.getTableName());
            return;
        }
        ContextImpl context = new ContextImpl(now, partitionList);
        final List<Partition> claimPartitionList;
        try {
            claimPartitionList = policy.claimPartitions(context);
        } catch (Exception e) {
            log.error("Policy claiming error, scope={}, table={}, context={}, exception={}",
                    policy.getScope(), policy.getTableName(),
                    context, e);
            return;
        }
        if (claimPartitionList == null || claimPartitionList.isEmpty()) {
            log.warn("Policy claims none partition, scope={}, table={}, context={}",
                    policy.getScope(), policy.getTableName(), context);
            return;
        }
        Map<String, Partition> claimPartitionMap = claimPartitionList.stream()
                .collect(Collectors.toMap(it -> it.name, it -> it, (a, b) -> a, LinkedHashMap::new));
        createPartitions(accessor, policy, context, claimPartitionMap);
        dropPartitions(accessor, policy, context, claimPartitionMap);
    }

    private void createPartitions(ObAccessor accessor, PartitionRolloverPolicy policy, ContextImpl context,
            Map<String, Partition> claimPartitions) {
        String maxPartitionName = context.partitionMap.values().stream()
                .map(partition -> partition.name)
                .filter(s -> !"DUMMY".equalsIgnoreCase(s))
                .max(String::compareTo)
                .orElse("");

        for (Partition claim : claimPartitions.values()) {
            if (context.partitionMap.containsKey(claim.name)) {
                continue;
            }
            if (maxPartitionName.compareTo(claim.name) >= 0) {
                log.info("Partition with greater boundary value already exist, skip create partition," +
                        " table={}, partition={}, greaterPartition={}, context={}",
                        policy.getTableName(), claim.name, maxPartitionName, context);
                continue;
            }
            log.info("Creating partition, scope={}, table={}, partition={}",
                    policy.getScope(), policy.getTableName(), claim.name);
            AlterTableInput.AddRangePartition add = AlterTableInput.AddRangePartition.builder()
                    .partition(claim.name)
                    .highValue(claim.partitionValues.get(0).toString())
                    .build();
            AlterTableInput input = AlterTableInput.builder()
                    .table(policy.getTableName())
                    .addPartition(add)
                    .build();
            try {
                accessor.object().alterTable(input);
            } catch (Exception e) {
                log.error("Create partition, scope={}, table={}, value={}, context={}, exception={}",
                        policy.getScope(), policy.getTableName(), claim.name, context, e);
            }
        }
    }

    private void dropPartitions(ObAccessor accessor, PartitionRolloverPolicy policy,
            ContextImpl context, Map<String, Partition> claimPartitions) {
        for (Partition partition : context.partitionList) {
            if (claimPartitions.containsKey(partition.name)) {
                continue;
            }
            log.info("Dropping partition, scope={}, table={}, partition={}",
                    policy.getScope(), policy.getTableName(), partition.name);
            try {
                AlterTableInput input = AlterTableInput.builder()
                        .table(policy.getTableName())
                        .dropPartition(partition.name)
                        .build();
                accessor.object().alterTable(input);
            } catch (Exception e) {
                log.error("Dropping partition, scope={}, table={}, partition={}, context={}, exception={}",
                        policy.getScope(), policy.getTableName(), partition.getName(), context, context);
            }
        }
    }

}
