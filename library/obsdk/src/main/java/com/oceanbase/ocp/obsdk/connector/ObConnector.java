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

package com.oceanbase.ocp.obsdk.connector;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.oceanbase.ocp.obsdk.config.DataSourceConfig;

/**
 * ob connector
 *
 */
public interface ObConnector {

    void init(DataSourceConfig config) throws SQLException;

    /**
     * close connector
     */
    void close();

    /**
     * get the data source from connector
     *
     * @return {@link DataSource}
     */
    DataSource dataSource();

    /**
     * Indicates whether the ob connector is alive
     *
     * @return <tt>true</tt> if the ob connector is alive, <tt>false</tt> if not
     */
    boolean isAlive();

    /**
     * Indicates whether the ob connector is active
     *
     * @return <tt>true</tt> if the ob connector is active, <tt>false</tt> if not
     */
    boolean isActive();

    /**
     * get the key of connector
     *
     * @return {@link ObConnectorKey}
     */
    ObConnectorKey key();
}
