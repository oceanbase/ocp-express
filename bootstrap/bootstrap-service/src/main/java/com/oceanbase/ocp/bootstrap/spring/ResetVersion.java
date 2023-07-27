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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.oceanbase.ocp.bootstrap.Bootstrap;
import com.oceanbase.ocp.bootstrap.config.env.Version;
import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.db.DataSourceName;
import com.oceanbase.ocp.bootstrap.db.property.ConfigPropertyManager;
import com.oceanbase.ocp.bootstrap.spi.AfterAllTableInitializationHook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResetVersion implements AfterAllTableInitializationHook {

    private final Map<String, Boolean> neededDataSources;

    public ResetVersion() {
        log.info("ResetVersion created");
        this.neededDataSources = new ConcurrentHashMap<>();
        this.neededDataSources.put(DataSourceName.SPRING.dataSourceName(), true);
    }

    @Override
    public void initialized(Action action, String dataSourceName) {
        log.info("dataSource {} is initialized for {}", dataSourceName, action);
        neededDataSources.remove(dataSourceName);
        if (neededDataSources.isEmpty()) {
            resetVersion();
            unlockBootstrap();
        }
    }

    void resetVersion() {
        Version ocpVersion = Bootstrap.getInstance().getOcpVersion();
        log.info("all data sources are initialized. reset ocp version to {}", ocpVersion);
        DataSource dataSource = Bootstrap.getInstance().getDataSourceProvider().getDataSource(DataSourceName.SPRING);
        ConfigPropertyManager configPropertyManager = new ConfigPropertyManager(dataSource);
        configPropertyManager.updateOcpVersion(ocpVersion);
        configPropertyManager.updateOcpFullVersion(ocpVersion);
    }

    void unlockBootstrap() {
        BootstrapLock.getInstance().unlock();
    }
}
