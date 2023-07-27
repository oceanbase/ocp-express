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

package com.oceanbase.ocp.bootstrap.spring;

import java.util.function.Supplier;

import javax.sql.DataSource;

import com.oceanbase.ocp.bootstrap.Bootstrap;
import com.oceanbase.ocp.bootstrap.config.AllDataConfig;
import com.oceanbase.ocp.bootstrap.config.DataConfig;
import com.oceanbase.ocp.bootstrap.config.env.Version;
import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.db.DataSourceName;
import com.oceanbase.ocp.bootstrap.db.DataSourceProvider;
import com.oceanbase.ocp.bootstrap.db.DbInitializer;
import com.oceanbase.ocp.bootstrap.db.property.ConfigPropertyManager;
import com.oceanbase.ocp.bootstrap.progress.ProgressHandlerImpl;
import com.oceanbase.ocp.bootstrap.spi.AfterDataSourceCreationHook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBInitInterceptor implements AfterDataSourceCreationHook {

    private final Supplier<AllDataConfig> allDataConfigSupplier;
    private volatile AllDataConfig allDataConfig;
    private volatile Action action;

    public DBInitInterceptor() {
        this(Bootstrap.getInstance().getConfigLoader()::loadAll,
                Bootstrap.getInstance().getAction());
    }

    public DBInitInterceptor(Supplier<AllDataConfig> allDataConfigSupplier, Action action) {
        log.info("DBInitInterceptor created. action: {}", action);
        this.allDataConfigSupplier = allDataConfigSupplier;
        this.action = action;
    }

    @Override
    public void afterDataSourceCreation(String name, DataSource dataSource) {
        log.info("dataSource {} created", name);
        DataSourceProvider dataSourceProvider = Bootstrap.getInstance().getDataSourceProvider();
        dataSourceProvider.setDataSourceSupplier(DataSourceName.ofDataSourceName(name), () -> dataSource);
        Action realAction = this.action;
        ProgressHandlerImpl progressHandler = Bootstrap.getInstance().getProgressHandler();

        if (DataSourceName.SPRING.dataSourceName().equals(name)) {
            realAction = autoAction(realAction, oldOcpVersion(), Bootstrap.getInstance().getOcpVersion());
            this.action = realAction;
            progressHandler.setAction(action);
        }

        if (realAction == Action.SKIP) {
            log.info("action is SKIP. skipped for data source {}", name);
            return;
        }
        lockBootstrap();

        if (allDataConfig == null) {
            allDataConfig = allDataConfigSupplier.get();
        }

        DataConfig dataConfig = allDataConfig.get(name);
        if (dataConfig == null) {
            log.info("no data config for dataSource {}", name);
            return;
        }

        new DbInitializer(name, dataSource, progressHandler).initialize(realAction, dataConfig);
        log.info("dataSource {} initialized", name);
    }

    void lockBootstrap() {
        BootstrapLock lock = BootstrapLock.getInstance();
        if (lock.tryLock()) {
            return;
        }
        throw new IllegalStateException("lock BootstrapLock failed, breaking db init!");
    }

    Action autoAction(Action origin, Version oldOcpVersion, Version newOcpVersion) {
        if (!Action.UNKNOWN.equals(origin)) {
            return origin;
        }
        log.info("automatically choosing bootstrap action. old {} -> new {}", oldOcpVersion, newOcpVersion);
        if (oldOcpVersion == null) {
            log.info("meta db is empty, choose INSTALL");
            return Action.INSTALL;
        }
        if (newOcpVersion.after(oldOcpVersion)) {
            log.info("version not equals, choose UPGRADE");
            return Action.UPGRADE;
        }
        if (newOcpVersion.before(oldOcpVersion)) {
            log.warn("current ocp version is older than origin ocp version!");
        }
        log.info("version is equals, choose SKIP");
        return Action.SKIP;
    }

    private Version oldOcpVersion() {
        DataSource springDataSource =
                Bootstrap.getInstance().getDataSourceProvider().getDataSource(DataSourceName.SPRING);
        ConfigPropertyManager configPropertyManager = new ConfigPropertyManager(springDataSource);
        try {
            return configPropertyManager.ocpVersion();
        } catch (Exception e) {
            return null;
        }
    }
}
