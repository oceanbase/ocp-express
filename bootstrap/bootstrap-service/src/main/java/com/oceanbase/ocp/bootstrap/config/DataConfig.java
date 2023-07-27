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

package com.oceanbase.ocp.bootstrap.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.oceanbase.ocp.bootstrap.core.def.DataDefinition;
import com.oceanbase.ocp.bootstrap.core.def.Migration;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;

import lombok.ToString;

@ToString
public class DataConfig {

    final String dataSourceName;
    final Map<String, TableDefinition> tableDefinitions;
    final Map<String, DataDefinition> dataDefinitions;
    final Map<String, Migration> migrations;

    public DataConfig(String dataSourceName, Map<String, TableDefinition> tableDefinitions,
            Map<String, DataDefinition> dataDefinitions,
            Map<String, Migration> migrations) {
        this.dataSourceName = dataSourceName;
        this.tableDefinitions = tableDefinitions;
        this.dataDefinitions = dataDefinitions;
        this.migrations = migrations;
    }

    public DataConfig(String dataSourceName) {
        this(dataSourceName, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public TableDefinition getTableDefinition(String tableName) {
        return tableDefinitions.get(tableName);
    }

    public DataDefinition getDataDefinition(String name) {
        return dataDefinitions.get(name);
    }

    public Migration getMigration(String name) {
        return migrations.get(name);
    }

    @Nonnull
    public Collection<TableDefinition> tableDefinitions() {
        return Collections.unmodifiableCollection(tableDefinitions.values());
    }

    public Collection<DataDefinition> dataDefinitions() {
        return Collections.unmodifiableCollection(dataDefinitions.values());
    }

    public Collection<Migration> migrations() {
        return Collections.unmodifiableCollection(migrations.values());
    }
}
