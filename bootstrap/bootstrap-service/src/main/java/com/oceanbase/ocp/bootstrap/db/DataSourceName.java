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

public enum DataSourceName {
    /**
     * Name of dataSources.
     */
    UNKNOWN(""), SPRING("dataSource");

    private final String name;

    DataSourceName(String name) {
        this.name = name;
    }

    public String dataSourceName() {
        return name;
    }

    public static DataSourceName ofDataSourceName(String dataSourceName) {
        if ("dataSource".equals(dataSourceName)) {
            return SPRING;
        }
        return UNKNOWN;
    }
}
