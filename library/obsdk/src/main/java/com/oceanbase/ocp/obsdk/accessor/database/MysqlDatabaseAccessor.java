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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.obsdk.accessor.DatabaseAccessor;
import com.oceanbase.ocp.obsdk.accessor.database.model.AlterDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.CreateDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.util.DatabaseUtils;

public class MysqlDatabaseAccessor implements DatabaseAccessor {

    private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS `%s`";
    private static final String ALTER_DATABASE = "ALTER DATABASE `%s` SET";
    private static final String DROP_DATABASE = "DROP DATABASE IF EXISTS `%s`";

    private final ObConnectTemplate connectTemplate;

    public MysqlDatabaseAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public ObDatabase createDatabase(CreateDatabaseInput input) {
        Validate.notNull(input, "input can not be null");
        Validate.notNull(input.getName(), "name of database can not be null");
        String sql = String.format(CREATE_DATABASE, input.getName());
        List<Object> param = new ArrayList<>();
        if (StringUtils.isNotEmpty(input.getCollation())) {
            sql += " DEFAULT COLLATE = ?";
            param.add(input.getCollation());
        }
        if (input.getReadonly() != null) {
            if (input.getReadonly()) {
                sql += " READ ONLY";
            } else {
                sql += " READ WRITE";
            }
        }
        connectTemplate.update(sql, param.toArray());
        return DatabaseUtils.getDatabase(connectTemplate, input.getName());
    }

    @Override
    public ObDatabase alterDatabase(AlterDatabaseInput input) {
        Validate.notNull(input, "input can not be null");
        Validate.notNull(input.getName(), "name of database can not be null");
        if (StringUtils.isEmpty(input.getCollation()) && input.getReadonly() == null) {
            throw new IllegalArgumentException("input is empty");
        }
        String sql = String.format(ALTER_DATABASE, input.getName());
        List<Object> param = new ArrayList<>();
        if (StringUtils.isNotEmpty(input.getCollation())) {
            sql += " DEFAULT COLLATE = ?";
            param.add(input.getCollation());
        }
        if (input.getReadonly() != null) {
            if (input.getReadonly()) {
                sql += " READ ONLY";
            } else {
                sql += " READ WRITE";
            }
        }
        connectTemplate.update(sql, param.toArray());
        return DatabaseUtils.getDatabase(connectTemplate, input.getName());
    }

    @Override
    public void dropDatabase(String dbName) {
        Validate.notNull(dbName, "input dbName can not be null");
        connectTemplate.update(String.format(DROP_DATABASE, dbName));
    }

    @Override
    public List<ObDatabase> listDatabases() {
        return DatabaseUtils.listDatabases(connectTemplate);
    }
}
