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

package com.oceanbase.ocp.task.engine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import com.oceanbase.ocp.task.engine.coordinator.SubtaskCoordinator;
import com.oceanbase.ocp.task.engine.coordinator.TaskCoordinator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConditionalOnProperty(value = "ocp.task.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class TaskEngineRunner implements ApplicationRunner {

    @Autowired
    private TaskCoordinator taskCoordinator;
    @Autowired
    private SubtaskCoordinator subtaskCoordinator;

    @Override
    @Async
    public void run(ApplicationArguments args) throws Exception {
        startupTaskCoordinator();
        startupSubtaskCoordinator();
    }

    private void startupSubtaskCoordinator() {
        subtaskCoordinator.startup();
    }

    private void startupTaskCoordinator() {
        taskCoordinator.startup();
    }

}
