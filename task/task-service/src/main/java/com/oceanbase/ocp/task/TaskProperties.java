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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ocp.task")
public class TaskProperties {

    @Value("${logging.file.name}")
    @Getter(value = AccessLevel.PRIVATE)
    private String loggingFileName;

    @Value("${subtaskExecutor.coreSize:32}")
    private int subtaskExecutorCorePoolSize;
    @Value("${subtaskExecutor.max.size:128}")
    private int subtaskExecutorMaxPoolSize;

    @Value("${manualSubtaskExecutor.coreSize:16}")
    private int manualSubtaskExecutorCorePoolSize;
    @Value("${manualSubtaskExecutor.maxSize:128}")
    private int manualSubtaskExecutorMaxPoolSize;

    @Value("${subtask.defaultConcurrency:10}")
    private int defaultSubTaskConcurrency;

    public String getSubtaskLogPath() {
        return loggingFileName.substring(0, loggingFileName.lastIndexOf("/")).concat("/task");
    }

}
