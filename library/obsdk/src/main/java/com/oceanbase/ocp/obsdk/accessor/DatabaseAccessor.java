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

package com.oceanbase.ocp.obsdk.accessor;

import java.util.List;

import com.oceanbase.ocp.obsdk.accessor.database.model.AlterDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.CreateDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;

/**
 * Database management in MySQL tenants.
 */
public interface DatabaseAccessor {

    /**
     * Create a database.
     *
     * @param input create database param
     * @return the created {@link ObDatabase}
     */
    ObDatabase createDatabase(CreateDatabaseInput input);

    /**
     * Modify a database's property.
     *
     * @param input alter database param
     * @return the modified {@link ObDatabase}
     */
    ObDatabase alterDatabase(AlterDatabaseInput input);

    /**
     * Drop a database by name.
     *
     * @param dbName name of the database
     */
    void dropDatabase(String dbName);

    /**
     * List databases.
     * <p>
     * Note: only used for database management in MySQL tenants here. To query
     * objects in Oracle tenants, use {@link ObjectAccessor#listDatabases()}
     * instead.
     *
     * @return list of {@link ObDatabase}
     */
    List<ObDatabase> listDatabases();
}
