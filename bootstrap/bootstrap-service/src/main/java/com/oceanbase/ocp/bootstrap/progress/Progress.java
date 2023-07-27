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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.core.Stage;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Progress {

    volatile Action action = Action.UNKNOWN;
    volatile boolean rootServletInitialized;
    volatile Throwable applicationError;
    volatile boolean applicationReady;

    final BeanProgress beanProgress = new BeanProgress();

    public boolean isApplicationReady() {
        return applicationReady;
    }

    public Action getAction() {
        return action;
    }

    public boolean isRootServletInitialized() {
        return rootServletInitialized;
    }

    public BeanProgress getBeanProgress() {
        return beanProgress;
    }

    public Throwable getApplicationError() {
        return applicationError;
    }

    public AllInstallUpgradeProgress getAllInstallUpgradeProgress() {
        return stageProgress;
    }

    public static class StageProgress {

        final int totalTasks;
        final AtomicInteger finishedTasks = new AtomicInteger();
        volatile boolean done = false;
        volatile Throwable throwable = null;

        volatile String currentTask;

        StageProgress(int totalTasks) {
            this.totalTasks = totalTasks;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public int getFinishedTasks() {
            return finishedTasks.get();
        }

        public boolean isDone() {
            return done;
        }

        public Throwable getError() {
            return throwable;
        }
    }

    public static class InstallUpgradeProgress {

        Map<Stage, StageProgress> stages = new ConcurrentHashMap<>();
        volatile Throwable error;
        volatile boolean done = false;

        public void beginStage(Stage stage, int count) {
            stages.computeIfAbsent(stage, k -> new StageProgress(count));
        }

        public void endStage(Stage stage) {
            StageProgress entry = stages.get(stage);
            entry.done = true;
        }

        public void beginTask(Stage stage, String name) {
            StageProgress entry = stages.get(stage);
            entry.currentTask = name;
        }

        public void endTask(Stage stage, String name, Throwable throwable) {
            StageProgress entry = stages.get(stage);
            entry.finishedTasks.incrementAndGet();
            entry.currentTask = null;
            entry.throwable = throwable;
        }

        public StageProgress getProgress(Stage stage) {
            return stages.get(stage);
        }

        public Collection<Stage> getStages() {
            return stages.keySet();
        }

        public Throwable getError() {
            return error;
        }

        public boolean isDone() {
            return done;
        }
    }

    public static class AllInstallUpgradeProgress {

        Map<String, InstallUpgradeProgress> entries = new ConcurrentHashMap<>();

        public void beginStage(String dataSource, Stage stage, int count) {
            InstallUpgradeProgress bySource = entries.computeIfAbsent(dataSource, k -> new InstallUpgradeProgress());
            bySource.beginStage(stage, count);
        }

        public void endStage(String dataSource, Stage stage) {
            entries.get(dataSource).endStage(stage);
        }

        public void beginTask(String dataSource, Stage stage, String name) {
            entries.get(dataSource).beginTask(stage, name);
        }

        public void endTask(String dataSource, Stage stage, String name, Throwable throwable) {
            entries.get(dataSource).endTask(stage, name, throwable);
        }

        public void end(String dataSource, Throwable throwable) {
            InstallUpgradeProgress progress = entries.get(dataSource);
            progress.error = throwable;
            progress.done = true;
        }

        public StageProgress getStageProgress(String dataSource, Stage stage) {
            return entries.get(dataSource).getProgress(stage);
        }

        public InstallUpgradeProgress getProgress(String dataSource) {
            return entries.get(dataSource);
        }

        public Collection<String> getDataSources() {
            return entries.keySet();
        }

        public Collection<Stage> getStages(String dataSource) {
            return entries.get(dataSource).getStages();
        }
    }

    private final AllInstallUpgradeProgress stageProgress = new AllInstallUpgradeProgress();

    @Getter
    @AllArgsConstructor
    public static class BeanInfo {

        private String name;
        private String className;
    }

    public static class BeanProgress {

        private final LinkedHashMap<String, BeanInfo> pendingBeans = new LinkedHashMap<>();
        private final ArrayList<BeanInfo> initialized = new ArrayList<>();

        public synchronized Collection<BeanInfo> getPending() {
            return pendingBeans.values();
        }

        public synchronized Collection<BeanInfo> getInitializedAfter(int i) {
            if (i == 0) {
                return initialized;
            }
            return initialized.subList(i, initialized.size());
        }

        public int getInitializedBeans() {
            return initialized.size();
        }

        synchronized void begin(BeanInfo beanInfo) {
            pendingBeans.put(beanInfo.getName(), beanInfo);
        }

        synchronized void end(BeanInfo beanInfo) {
            pendingBeans.remove(beanInfo.getName());
            initialized.add(beanInfo);
        }
    }
}
