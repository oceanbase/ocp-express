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

package com.oceanbase.ocp.bootstrap.db.property;

import java.util.Map;

import javax.sql.DataSource;

import com.oceanbase.ocp.bootstrap.Bootstrap;
import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.core.ProgressHandler;
import com.oceanbase.ocp.bootstrap.core.Stage;
import com.oceanbase.ocp.bootstrap.db.DataSourceName;
import com.oceanbase.ocp.bootstrap.spi.AfterDataInitializationHook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigPropertyInitializer implements AfterDataInitializationHook {

    @Override
    public void initialized(Action action, String dataSourceName) {
        DataSourceName spring = DataSourceName.SPRING;
        if (spring.dataSourceName().equals(dataSourceName)) {
            Bootstrap bootstrap = Bootstrap.getInstance();
            ProgressHandler progressHandler = bootstrap.getProgressHandler();
            DataSource dataSource =
                    bootstrap.getDataSourceProvider().getDataSource(spring);

            ConfigPropertyManager configPropertyManager = new ConfigPropertyManager(dataSource);
            Map<String, String> properties = bootstrap.getConfigProperties();
            log.info("set config properties from bootstrap params");
            progressHandler.beginStage(spring.dataSourceName(), Stage.CONFIG_PROPERTIES, properties.size());
            properties.forEach((key, value) -> {
                progressHandler.beginTask(spring.dataSourceName(), Stage.CONFIG_PROPERTIES, "property", key, value);
                configPropertyManager.updateProperty(key, value);
                progressHandler.endTask(spring.dataSourceName(), Stage.CONFIG_PROPERTIES, "property", key, null);
            });
            log.info("set server.port to {}", bootstrap.getPort());
            configPropertyManager.updateProperty("server.port", Integer.toString(bootstrap.getPort()));
            progressHandler.endStage(spring.dataSourceName(), Stage.CONFIG_PROPERTIES);
        }
    }
}
