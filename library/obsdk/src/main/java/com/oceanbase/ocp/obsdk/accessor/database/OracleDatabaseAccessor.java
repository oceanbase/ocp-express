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

package com.oceanbase.ocp.obsdk.accessor.database;

import java.util.List;

import com.oceanbase.ocp.obsdk.accessor.DatabaseAccessor;
import com.oceanbase.ocp.obsdk.accessor.database.model.AlterDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.CreateDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

public class OracleDatabaseAccessor implements DatabaseAccessor {

    private final ObConnectTemplate connectTemplate;

    public OracleDatabaseAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public ObDatabase createDatabase(CreateDatabaseInput input) {
        throw new UnsupportedOperationException("database not supported in Oracle mode");
    }

    @Override
    public ObDatabase alterDatabase(AlterDatabaseInput input) {
        throw new UnsupportedOperationException("database not supported in Oracle mode");
    }

    @Override
    public void dropDatabase(String dbName) {
        throw new UnsupportedOperationException("database not supported in Oracle mode");
    }

    @Override
    public List<ObDatabase> listDatabases() {
        throw new UnsupportedOperationException("database not supported in Oracle mode");
    }
}
