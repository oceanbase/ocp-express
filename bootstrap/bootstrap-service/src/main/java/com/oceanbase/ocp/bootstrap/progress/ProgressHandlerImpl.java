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

package com.oceanbase.ocp.bootstrap.progress;

import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.core.ProgressHandler;
import com.oceanbase.ocp.bootstrap.core.Stage;
import com.oceanbase.ocp.bootstrap.progress.Progress.BeanInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgressHandlerImpl implements ProgressHandler {

    private final Progress progress;
    private final ProgressWriter progressWriter;

    public ProgressHandlerImpl(Progress progress, ProgressWriter progressWriter) {
        this.progress = progress;
        this.progressWriter = progressWriter;
    }

    private static final ProgressHandlerImpl NOP =
            new ProgressHandlerImpl(new Progress(), ProgressWriter.nopProgressWriter());

    public static ProgressHandlerImpl nopProgressHandler() {
        return NOP;
    }

    public void beginBean(String name, String className) {
        this.progress.beanProgress.begin(new BeanInfo(name, className));
    }

    public void endBean(String name, String className) {
        this.progress.beanProgress.end(new BeanInfo(name, className));
    }

    public void onApplicationFailed(Throwable throwable) {
        this.progress.applicationError = throwable;
    }

    public void onApplicationReady() {
        this.progress.applicationReady = true;
    }

    public void setAction(Action action) {
        progress.action = action;
    }

    @Override
    public void beginStage(String dataSourceName, Stage stage, int count) {
        progressWriter.begin(dataSourceName, stage.name(), "-", Integer.toString(count));
        progress.getAllInstallUpgradeProgress().beginStage(dataSourceName, stage, count);
    }

    @Override
    public void endStage(String dataSourceName, Stage stage) {
        progressWriter.end(dataSourceName, stage.name(), "-", "", null);
        progress.getAllInstallUpgradeProgress().endStage(dataSourceName, stage);
    }

    @Override
    public void beginTask(String dataSourceName, Stage stage, String taskType, String name, String message) {
        progressWriter.begin(dataSourceName, stage.name(), taskType + ":" + name, message);
        progress.getAllInstallUpgradeProgress().beginTask(dataSourceName, stage, taskType + ":" + name);
    }

    @Override
    public void endTask(String dataSourceName, Stage stage, String taskType, String name, Throwable e) {
        progressWriter.end(dataSourceName, stage.name(), taskType + ":" + name, "", e);
        progress.getAllInstallUpgradeProgress().endTask(dataSourceName, stage, taskType + ":" + name, e);
    }

    @Override
    public void beginAction(String dataSourceName, Action action) {
        progressWriter.begin(dataSourceName, action.name(), "-", "");
    }

    @Override
    public void endAction(String dataSourceName, Action action, Throwable e) {
        progressWriter.end(dataSourceName, action.name(), "-", "", e);
        progress.getAllInstallUpgradeProgress().end(dataSourceName, e);
    }

}
