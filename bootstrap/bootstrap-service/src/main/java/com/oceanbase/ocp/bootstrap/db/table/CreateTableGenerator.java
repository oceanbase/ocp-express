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

import java.util.List;
import java.util.stream.Collectors;

import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Field;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Index;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateTableGenerator {

    public String generate(TableDefinition tableDefinition) {
        if (tableDefinition.isDrop()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append(SQLUtils.wrapBackQuotes(tableDefinition.getName()));
        sb.append("(\n");
        generateFields(sb, tableDefinition.getFields());

        if (tableDefinition.getPrimaryKey() != null && !tableDefinition.getPrimaryKey().getFields().isEmpty()) {
            sb.append(",\n\t");
            String primaryKeyStr = tableDefinition.getPrimaryKey().getFields().stream()
                    .map(SQLUtils::wrapBackQuotes).collect(Collectors.joining(", "));
            sb.append("PRIMARY KEY (").append(primaryKeyStr).append(")");
        }
        generateIndices(sb, tableDefinition.getIndexes());
        sb.append("\n)");
        if (tableDefinition.getAutoIncrement() != null) {
            sb.append(" AUTO_INCREMENT = ").append(tableDefinition.getAutoIncrement());
        }
        if (tableDefinition.getDefaultCharset() != null) {
            sb.append(" DEFAULT CHARSET = ").append(tableDefinition.getDefaultCharset());
        }
        if (tableDefinition.getComment() != null && !tableDefinition.getComment().isEmpty()) {
            sb.append(String.format(" COMMENT = '%s'", SQLUtils.escape(tableDefinition.getComment())));
        }
        sb.append("\n");
        generatePartitions(sb, tableDefinition.getPartition());
        return sb.toString();
    }

    void generateField(StringBuilder sb, TableDefinition.Field field) {
        if (field.isDrop()) {
            return;
        }
        sb.append('`').append(field.getName()).append("` ").append(field.getType());
        if (!field.isNullable()) {
            sb.append(" NOT NULL");
        }
        if (field.getDefaultValue() != null) {
            sb.append(" DEFAULT");
            Object defaultVal = field.getDefaultValue();
            sb.append(" ");
            sb.append(SQLUtils.valueToString(defaultVal));
        }

        if (field.getOnUpdate() != null) {
            sb.append(" ON UPDATE ").append(field.getOnUpdate());
        }

        if (field.isAutoIncrement()) {
            sb.append(" AUTO_INCREMENT");
        }

        if (field.getComment() != null && !field.getComment().isEmpty()) {
            sb.append(" COMMENT '").append(SQLUtils.escape(field.getComment())).append("'");
        }
    }

    void generateFields(StringBuilder sb, List<TableDefinition.Field> fields) {
        int i = 0;
        for (Field field : fields) {
            if (field.isDrop()) {
                continue;
            }
            if (i > 0) {
                sb.append(",\n");
            }
            sb.append("\t");
            generateField(sb, field);
            i++;
        }
    }

    void generateIndex(StringBuilder sb, TableDefinition.Index index) {
        if (index.isDrop()) {
            return;
        }
        if (index.isUnique()) {
            sb.append("UNIQUE KEY ");
        } else {
            sb.append("KEY ");
        }

        String keyStr = index.getFields().stream()
                .map(SQLUtils::wrapBackQuotes)
                .collect(Collectors.joining(", "));
        sb.append(String.format("%s(%s)", index.getName(), keyStr));
        sb.append(index.isLocal() ? " LOCAL" : " GLOBAL");
    }

    void generateIndices(StringBuilder sb, List<TableDefinition.Index> indices) {
        if (indices == null) {
            return;
        }
        for (Index index : indices) {
            if (index.isDrop()) {
                continue;
            }
            sb.append(",\n\t");
            generateIndex(sb, index);
        }
    }

    void genPartitionRangeElements(StringBuilder sb, List<TableDefinition.Partition.RangeElement> rangeElements,
            boolean isSub) {
        String partitionKeyWord = isSub ? "SUBPARTITION" : "PARTITION";
        sb.append("(");
        for (int i = 0; i < rangeElements.size(); i++) {
            TableDefinition.Partition.RangeElement rangeElement = rangeElements.get(i);
            sb.append(String.format("%s %s VALUES LESS THAN(%s)", partitionKeyWord, rangeElement.getName(),
                    rangeElement.getValue()));
            if (i != rangeElements.size() - 1) {
                sb.append(",").append("\n");
            }
        }
        sb.append(")\n");
    }

    void generatePartitions(StringBuilder sb, TableDefinition.Partition partition) {
        if (partition == null) {
            return;
        }

        if (partition.getFields() != null) {
            String columns = partition.getFields().stream()
                    .map(SQLUtils::wrapBackQuotes).collect(Collectors.joining(", "));
            sb.append("PARTITION BY RANGE COLUMNS(").append(columns).append(")").append("\n");
        }
        if (partition.getByExpr() != null) {
            sb.append("PARTITION BY RANGE (").append(partition.getByExpr()).append(")").append("\n");
        }
        TableDefinition.Partition subPartition = partition.getSubPartition();
        if (subPartition != null) {
            switch (subPartition.getType()) {
                case "RANGE": {
                    String subColumns = subPartition.getFields().stream()
                            .map(SQLUtils::wrapBackQuotes).collect(Collectors.joining(", "));
                    sb.append("SUBPARTITION BY RANGE COLUMNS(").append(subColumns).append(")");
                    sb.append(" SUBPARTITION TEMPLATE\n");
                    genPartitionRangeElements(sb, partition.getSubPartition().getRangeElements(), true);
                    break;
                }
                case "HASH": {
                    sb.append("SUBPARTITION BY HASH (").append(subPartition.getByExpr()).append(")");
                    Integer hashPartitionCount = subPartition.getHashPartitionCount();
                    if (hashPartitionCount != null) {
                        sb.append(" SUBPARTITIONS ").append(hashPartitionCount).append("\n");
                    }
                    break;
                }
                default:
            }
        }
        genPartitionRangeElements(sb, partition.getRangeElements(), false);
    }

}
