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

package com.oceanbase.ocp.bootstrap.db.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oceanbase.ocp.bootstrap.config.env.ELEnv;
import com.oceanbase.ocp.bootstrap.config.env.JavaxELEnv;
import com.oceanbase.ocp.bootstrap.core.def.DataDefinition;
import com.oceanbase.ocp.bootstrap.core.def.Migration;
import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.core.def.SqlCompatible;
import com.oceanbase.ocp.bootstrap.db.SqlQuerier;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;

public class DataSqlGenerator {

    private final SqlCompatible sqlCompatible;

    public DataSqlGenerator() {
        this(SqlCompatible.OCEANBASE);
    }

    public DataSqlGenerator(SqlCompatible sqlCompatible) {
        this.sqlCompatible = sqlCompatible;
    }

    public List<String> generateInsert(DataDefinition dataDefinition) {
        if (dataDefinition.getRows() == null || dataDefinition.getRows().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ret = new ArrayList<>(dataDefinition.getRows().size());
        try {
            if (dataDefinition.getRows() != null) {
                for (Row row : dataDefinition.getRows()) {
                    ret.add(generateForInsertRow(dataDefinition.getTableName(), row,
                            dataDefinition.getOnDuplicateUpdate()));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "generate insert for dataDefinition " + dataDefinition.getName() + " failed", e);
        }
        return ret;
    }

    public List<String> generateDelete(DataDefinition dataDefinition) {
        if (dataDefinition.getDelete() == null || dataDefinition.getDelete().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ret = new ArrayList<>(dataDefinition.getDelete().size());
        try {
            if (dataDefinition.getDelete() != null) {
                for (Row row : dataDefinition.getDelete()) {
                    ret.add(generateForDeleteRow(dataDefinition.getTableName(), row));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "generate delete for dataDefinition " + dataDefinition.getName() + " failed", e);
        }
        return ret;
    }

    public List<String> generateForMigration(Migration migration, SqlQuerier querier) {
        ELEnv elEnv = new JavaxELEnv();
        String condition = migration.getCondition();
        if (condition != null && !condition.isEmpty()) {
            boolean matched = elEnv.eval(condition);
            if (!matched) {
                return Collections.emptyList();
            }
        }
        if (migration.getRawSqls() != null) {
            return migration.getRawSqls();
        }
        List<Row> rows;
        try {
            rows = querier.query(migration.getSourceSql());
        } catch (SQLException e) {
            if (migration.isIgnoreSourceSqlError()) {
                return Collections.emptyList();
            }
            throw new IllegalStateException("query source sql of migration " + migration.getName() + " failed", e);
        }
        Stream<Row> rowStream = processRows(migration, elEnv, rows);
        return dataToSql(migration, rowStream);
    }

    Stream<Row> processRows(Migration migration, ELEnv elEnv, List<Row> rows) {
        Stream<Row> rowStream;
        if (migration.getExpr() != null && !migration.getExpr().isEmpty()) {
            List<Map<String, Object>> data = rows.stream().map(Row::getData).collect(Collectors.toList());
            elEnv.set("data", data);
            if (migration.getWith() != null && !migration.getWith().isEmpty()) {
                migration.getWith().forEach((name, expr) -> {
                    try {
                        Object withValue = elEnv.eval(expr);
                        elEnv.set(name, withValue);
                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "execute with " + name + " of migration " + migration.getName() + " failed", e);
                    }
                });
            }
            try {
                data = elEnv.eval(migration.getExpr());
            } catch (Exception e) {
                throw new IllegalStateException("execute expr of migration " + migration.getName() + " failed", e);
            }
            if (data == null) {
                throw new IllegalStateException("result of expr of migration " + migration.getName() + " is null");
            }
            rowStream = data.stream().map(Row::new);
        } else {
            rowStream = rows.stream();
        }
        return rowStream;
    }

    List<String> dataToSql(Migration migration, Stream<Row> rowStream) {
        try {
            if (migration.getDeleteBy() != null && !migration.getDeleteBy().isEmpty()) {
                return rowStream.map(row -> {
                    Map<String, Object> delMap = new TreeMap<>();
                    for (String field : migration.getDeleteBy()) {
                        delMap.put(field, row.getData().get(field));
                    }
                    return new Row(delMap);
                })
                        .map(delRow -> generateForDeleteRow(migration.getTargetTable(), delRow))
                        .collect(Collectors.toList());
            } else if (migration.getUpdateBy() != null && !migration.getUpdateBy().isEmpty()) {
                return rowStream.map(r -> generateForUpdateRow(migration.getTargetTable(), r, migration.getUpdateBy()))
                        .collect(Collectors.toList());
            } else {
                return rowStream
                        .map(r -> generateForInsertRow(migration.getTargetTable(), r, migration.getOnDuplicateUpdate()))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new IllegalStateException("generate sql of migration " + migration.getName() + " failed", e);
        }
    }

    String generateForInsertRow(String tableName, Row row, List<String> onDuplicatedUpdate) {
        boolean useIgnore = sqlCompatible.haveCapability(SqlCompatible.Capability.INSERT_IGNORE);
        boolean hasOnDuplicatedUpdate = onDuplicatedUpdate != null && !onDuplicatedUpdate.isEmpty();
        StringBuilder sb = new StringBuilder("INSERT ");
        if (useIgnore && !hasOnDuplicatedUpdate) {
            sb.append("IGNORE ");
        }
        sb.append("INTO ");
        sb.append('`').append(tableName).append('`');
        sb.append('(');
        // field names
        Set<String> fieldNames = row.getData().keySet();
        boolean first = true;
        for (String fieldName : fieldNames) {
            if (!first) {
                sb.append(',');
            }
            sb.append('`').append(fieldName).append('`');
            first = false;
        }
        sb.append(')');
        sb.append(" VALUES (");

        first = true;
        for (String fieldName : fieldNames) {
            Object value = row.getData().get(fieldName);
            if (!first) {
                sb.append(',');
            }
            sb.append(SQLUtils.valueToString(value));
            first = false;
        }
        sb.append(')');
        if (hasOnDuplicatedUpdate) {
            sb.append(" ON DUPLICATE KEY UPDATE ");
            first = true;
            for (String fieldName : onDuplicatedUpdate) {
                Object value = row.getData().get(fieldName);
                if (value != null) {
                    if (!first) {
                        sb.append(',');
                    }
                    sb.append("`").append(fieldName).append("`=").append(SQLUtils.valueToString(value));
                    first = false;
                }
            }
        } else if (!useIgnore) {
            String firstFieldName = fieldNames.iterator().next();
            sb.append(" ON DUPLICATE KEY UPDATE `").append(firstFieldName).append("`=`").append(firstFieldName)
                    .append('`');
        }
        return sb.toString();
    }

    String generateForDeleteRow(String tableName, Row row) {
        if (row.getData().isEmpty()) {
            throw new IllegalArgumentException("delete row must not be empty");
        }
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append('`').append(tableName).append('`');
        sb.append(" WHERE ");
        Set<String> fieldNames = row.getData().keySet();
        int i = 0;
        for (String fieldName : fieldNames) {
            if (i > 0) {
                sb.append(" AND ");
            }
            Object value = row.getData().get(fieldName);
            sb.append("`").append(fieldName).append("`=").append(SQLUtils.valueToString(value));
            i++;
        }
        return sb.toString();
    }

    String generateForUpdateRow(String tableName, Row row, List<String> byFields) {
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append('`').append(tableName).append("` SET ");
        Map<String, Object> data = row.getData();
        int i = 0;
        for (String field : data.keySet()) {
            if (!byFields.contains(field)) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append('`').append(field).append("`=").append(SQLUtils.valueToString(data.get(field)));
                i++;
            }
        }
        sb.append(" WHERE ");
        i = 0;
        for (String field : data.keySet()) {
            if (byFields.contains(field)) {
                if (i > 0) {
                    sb.append(" AND ");
                }
                sb.append('`').append(field).append("`=").append(SQLUtils.valueToString(data.get(field)));
                i++;
            }
        }
        return sb.toString();
    }
}
