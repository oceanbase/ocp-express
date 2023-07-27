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

package com.oceanbase.ocp.bootstrap.db.table;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.db.SqlQuerier;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableLoader {

    private final SqlQuerier sqlQuerier;
    private final TableParser tableParser;

    public TableLoader(SqlQuerier sqlQuerier) {
        this.sqlQuerier = sqlQuerier;
        this.tableParser = new OBTableParser();
    }

    public TableLoader(DataSource dataSource) {
        this(sql -> SQLUtils.queryRows(dataSource, sql));
    }

    public Map<String, TableDefinition> loadAllTables() throws SQLException {
        log.info("loading all existed table info");
        List<String> allTableNames = allTableNames();
        Map<String, TableDefinition> ret = new LinkedHashMap<>(allTableNames.size());
        allTableNames.stream().parallel().forEach(tableName -> {
            try {
                TableDefinition tableDefinition = load(tableName);
                synchronized (ret) {
                    ret.put(tableName, tableDefinition);
                }
            } catch (SQLException e) {
                log.warn("load table definition failed for table {}, error: {}", tableName, e);
            }
        });
        log.info("all existed table info loaded. totally {} tables", ret.size());
        return ret;
    }

    public TableDefinition load(String name) throws SQLException {
        String ddl = loadDDL(name);
        if (ddl == null) {
            return null;
        }
        return tableParser.parseCreateTable(ddl);
    }

    public List<String> allTableNames() throws SQLException {
        List<Row> rows = sqlQuerier.query("show tables");
        return rows.stream().map(row -> (String) row.getFirstField()).collect(Collectors.toList());
    }

    public String loadDDL(String name) throws SQLException {
        if (!SQLUtils.isValidName(name)) {
            throw new IllegalArgumentException("invalid table name " + name);
        }
        List<Row> rows = sqlQuerier.query("show create table `" + name + "`");
        if (rows.isEmpty()) {
            return null;
        }
        return (String) rows.get(0).get("Create Table");
    }
}
