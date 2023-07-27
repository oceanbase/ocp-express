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

package com.oceanbase.ocp.bootstrap.db;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.sql.DataSource;

public class DataSourceProvider {

    private final ConcurrentHashMap<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Supplier<DataSource>> dataSourceSuppliers =
            new ConcurrentHashMap<>();

    public DataSource getDataSource(DataSourceName dataSourceName) {
        return dataSources.computeIfAbsent(dataSourceName.dataSourceName(),
                k -> dataSourceSuppliers.getOrDefault(dataSourceName.dataSourceName(), () -> null).get());
    }

    public void setDataSourceSupplier(DataSourceName dataSourceName, Supplier<DataSource> supplier) {
        dataSourceSuppliers.put(dataSourceName.dataSourceName(), supplier);
    }
}
