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
package com.oceanbase.ocp.partitioning;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.partitioning.service.PartitionMetadataService;
import com.oceanbase.ocp.partitioning.service.PartitionRolloverService;

import lombok.extern.slf4j.Slf4j;

@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@ConditionalOnProperty(value = "ocp.partitioning.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties(PartitionRolloverProperties.class)
@Slf4j
public class PartitionRolloverAutoConfiguration implements InitializingBean {

    private ScheduledThreadPoolExecutor poolExecutor;

    @Bean
    public PartitionRolloverService partitionRolloverService() {
        return new PartitionRolloverService();
    }

    @Bean
    public PartitionMetadataService partitionMetadataService() {
        return new PartitionMetadataService();
    }

    @Autowired
    private PartitionRolloverProperties properties;

    @Override
    public void afterPropertiesSet() throws Exception {
        poolExecutor = new ScheduledThreadPoolExecutor(1, new OcpThreadFactory("partition-rollover-"));
        poolExecutor.scheduleWithFixedDelay(() -> partitionRolloverService().watchAll(),
                properties.getInitialWorkerDelay().toMillis() / 1000, 3_600, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        ExecutorUtils.shutdown(poolExecutor, 1);
    }

}
