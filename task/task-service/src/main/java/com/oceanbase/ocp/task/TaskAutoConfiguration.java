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
package com.oceanbase.ocp.task;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.oceanbase.ocp.core.security.AuthenticationFacade;
import com.oceanbase.ocp.obsdk.ObSdkContext;
import com.oceanbase.ocp.task.dao.SubtaskAccessor;
import com.oceanbase.ocp.task.dao.SubtaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.dao.SubtaskInstanceRepository;
import com.oceanbase.ocp.task.dao.SubtaskLogRepo;
import com.oceanbase.ocp.task.dao.TaskDefinitionRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceOverviewRepository;
import com.oceanbase.ocp.task.dao.TaskInstanceRepository;
import com.oceanbase.ocp.task.dao.TaskTemplateRepository;
import com.oceanbase.ocp.task.engine.config.SubtaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.config.TaskCoordinatorConfig;
import com.oceanbase.ocp.task.engine.coordinator.SubtaskCoordinator;
import com.oceanbase.ocp.task.engine.coordinator.TaskCoordinator;
import com.oceanbase.ocp.task.engine.manager.SubtaskInstanceManager;
import com.oceanbase.ocp.task.engine.manager.SubtaskInstanceManagerImpl;
import com.oceanbase.ocp.task.engine.manager.TaskInstanceManagerImpl;
import com.oceanbase.ocp.task.engine.util.ThreadPrintStream;
import com.oceanbase.ocp.task.hook.SubtaskHook;

import io.micrometer.core.instrument.MeterRegistry;

@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@ConditionalOnProperty(value = "ocp.task.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@DependsOn("beanUtils")
@EnableConfigurationProperties(TaskProperties.class)
public class TaskAutoConfiguration {

    @Autowired
    private TaskDefinitionRepository taskDefinitionRepository;
    @Autowired
    private TaskTemplateRepository taskTemplateRepository;
    @Autowired
    private TaskInstanceRepository taskInstanceRepository;
    @Autowired
    private SubtaskInstanceRepository subtaskInstanceRepository;
    @Autowired
    private SubtaskLogRepo subtaskLogRepo;
    @Autowired
    private SubtaskInstanceOverviewRepository subtaskOverviewRepo;
    @Autowired
    private TaskInstanceOverviewRepository taskOverviewRepo;
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    private MeterRegistry meterRegistry;

    @Resource(name = "transactionManager")
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private TaskProperties taskProperties;

    @Bean
    public TaskCoordinator taskCoordinator() {
        TaskCoordinatorConfig config = TaskCoordinatorConfig.builder()
                .taskDefinitionRepository(taskDefinitionRepository)
                .taskTemplateRepository(taskTemplateRepository)
                .taskInstanceRepository(taskInstanceRepository)
                .subtaskOverviewRepo(subtaskOverviewRepo)
                .taskOverviewRepo(taskOverviewRepo)
                .defaultSubtaskConcurrency(taskProperties.getDefaultSubTaskConcurrency())
                .transactionTemplate(new TransactionTemplate(platformTransactionManager))
                .build();
        return new TaskCoordinator(config).addMeterRegistry(meterRegistry);
    }

    @Bean
    public SubtaskAccessor subtaskAccessor() {
        return new SubtaskAccessor(subtaskOverviewRepo, subtaskLogRepo);
    }

    @Bean
    public SubtaskCoordinator subtaskCoordinator() {
        SubtaskCoordinatorConfig config = SubtaskCoordinatorConfig.builder()
                .taskFunc(taskId -> taskOverviewRepo.findById(taskId).orElse(null))
                .subtaskRepo(subtaskInstanceRepository)
                .subtaskAccessor(subtaskAccessor())
                .subtaskLogRepo(subtaskLogRepo)
                .transactionTemplate(new TransactionTemplate(platformTransactionManager))
                .subtaskLogPath(taskProperties.getSubtaskLogPath())
                .subtaskExecutorCorePoolSize(taskProperties.getSubtaskExecutorCorePoolSize())
                .subtaskExecutorMaxPoolSize(taskProperties.getSubtaskExecutorMaxPoolSize())
                .manualSubtaskExecutorCorePoolSize(taskProperties.getManualSubtaskExecutorCorePoolSize())
                .manualSubtaskExecutorMaxPoolSize(taskProperties.getManualSubtaskExecutorMaxPoolSize())
                .postSubtaskHooks(makePostSubtaskHooks())
                .build();
        ThreadPrintStream.replaceSystemOut();
        return new SubtaskCoordinator(config).addMeterRegistry(meterRegistry);
    }

    @Bean
    public TaskInstanceManager taskInstanceManager(TaskCoordinator taskCoordinator) {
        Supplier<String> supplier = () -> authenticationFacade.currentUserName();
        return new TaskInstanceManagerImpl(taskInstanceRepository, taskCoordinator, supplier);
    }

    @Bean
    public SubtaskInstanceManager subtaskInstanceManager() {
        return new SubtaskInstanceManagerImpl(subtaskInstanceRepository);
    }

    private List<SubtaskHook<Long>> makePostSubtaskHooks() {
        return Collections.singletonList(new SubtaskHook<>(1, entity -> {
            SecurityContextHolder.clearContext();
            ObSdkContext.clear();
        }));
    }

}
