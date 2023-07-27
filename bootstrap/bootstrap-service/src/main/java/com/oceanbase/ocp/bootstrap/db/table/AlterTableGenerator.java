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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Field;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Index;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlterTableGenerator {

    public AlterTables generate(TableDiff tableDiff) {
        log.info("generate alter table for table diff {}", tableDiff.getNewTable().getName());
        List<String> addedColumns = new ArrayList<>();
        List<String> changedColumns = new ArrayList<>();
        List<String> deletedColumns = new ArrayList<>();

        if (tableDiff.getNewTable() == null) {
            log.warn("generate AlterTables tableDiff.TableDef is null");
            return null;
        }

        String tableName = tableDiff.getNewTable().getName();
        if (tableDiff.getRenamedFields() != null) {
            for (Field addedField : tableDiff.getRenamedFields()) {
                String addColumnSql = genRenameColumn(tableName, addedField);
                addedColumns.add(addColumnSql);
            }
        }

        if (tableDiff.getAddedFields() != null) {
            for (Field addedField : tableDiff.getAddedFields()) {
                String addColumnSql = genAlterAddColumn(tableName, addedField);
                addedColumns.add(addColumnSql);
            }
        }

        if (tableDiff.getChangedFields() != null) {
            for (Field changedField : tableDiff.getChangedFields()) {
                Field oldField = tableDiff.getOldTable().getField(changedField.getName());
                if (isCompatible(oldField, changedField)) {
                    String changColumnSql = genAlterChangeColumn(tableName, changedField);
                    changedColumns.add(changColumnSql);
                } else {
                    log.error("field change of {}.{} is not compatible", tableDiff.getNewTable().getName(),
                            changedField.getName());
                }
            }
        }

        if (tableDiff.getDeletedFields() != null) {
            for (Field deletedField : tableDiff.getDeletedFields()) {
                String dropColumnSql = genAlterDropColumn(tableName, deletedField.getName());
                deletedColumns.add(dropColumnSql);
            }
        }
        List<String> addedIndices = new ArrayList<>();
        List<String> addedIndicesDelayed = new ArrayList<>();
        List<String> changedIndices = new ArrayList<>();
        List<String> changedIndicesDelayed = new ArrayList<>();
        List<String> deletedIndices = new ArrayList<>();
        List<String> deletedIndicesDelayed = new ArrayList<>();

        if (tableDiff.getAddedIndices() != null) {
            for (Index addedIndex : tableDiff.getAddedIndices()) {
                String addIndexSql = genAlterAddIndex(tableName, addedIndex);
                if (addedIndex.isDelayed()) {
                    addedIndicesDelayed.add(addIndexSql);
                } else {
                    addedIndices.add(addIndexSql);
                }
            }
        }
        if (tableDiff.getChangedIndices() != null) {
            for (Index changedIndex : tableDiff.getChangedIndices()) {
                List<String> changeIndexSql = genAlterChangeIndex(tableName, changedIndex);
                if (changedIndex.isDelayed()) {
                    changedIndicesDelayed.addAll(changeIndexSql);
                } else {
                    changedIndices.addAll(changeIndexSql);
                }
            }
        }

        if (tableDiff.getDeletedIndices() != null) {
            for (Index deletedIndex : tableDiff.getDeletedIndices()) {
                String deleteIndexSql = genAlterDropIndex(tableName, deletedIndex.getName());
                if (deletedIndex.isDelayed()) {
                    deletedIndicesDelayed.add(deleteIndexSql);
                } else {
                    deletedIndices.add(deleteIndexSql);
                }
            }
        }

        List<String> changedTableDefs = new ArrayList<>();
        if (tableDiff.getChangedDefinitions() != null) {
            for (TableDiff.Element changedTableDef : tableDiff.getChangedDefinitions()) {
                String changedTableDefSql = genAlterTableDef(tableDiff.getNewTable(), tableName, changedTableDef);
                changedTableDefs.add(changedTableDefSql);
            }
        }

        AlterTables alterTables = new AlterTables();
        alterTables.setAddedColumns(addedColumns);
        alterTables.setChangedColumns(changedColumns);
        alterTables.setDeletedColumns(deletedColumns);
        alterTables.setAddedIndices(addedIndices);
        alterTables.setAddedIndicesDelayed(addedIndicesDelayed);
        alterTables.setChangedIndices(changedIndices);
        alterTables.setChangedIndicesDelayed(changedIndicesDelayed);
        alterTables.setDeletedIndices(deletedIndices);
        alterTables.setDeletedIndicesDelayed(deletedIndicesDelayed);
        alterTables.setChangedTableDefs(changedTableDefs);
        return alterTables;
    }

    static boolean isCompatible(Field oldField, Field newField) {
        if (oldField == null) {
            return true;
        }
        if (oldField.isNullable() && !newField.isNullable()) {
            return false;
        }
        if (!oldField.isAutoIncrement() && newField.isAutoIncrement()) {
            return false;
        }
        return newField.getType().canChangeFrom(oldField.getType());
    }

    private void genColumnAppendix(StringBuilder sb, Field field) {
        sb.append(field.getType());
        if (!field.isNullable()) {
            sb.append(" NOT NULL");
        }

        if (field.getDefaultValue() != null) {
            sb.append(" DEFAULT ").append(SQLUtils.valueToString(field.getDefaultValue()));
        }

        if (field.getOnUpdate() != null) {
            sb.append(" ON UPDATE ").append(field.getOnUpdate());
        }

        if (field.isAutoIncrement()) {
            sb.append(" AUTO_INCREMENT");
        }

        if (field.getComment() != null) {
            sb.append(" COMMENT ").append("'").append(SQLUtils.escape(field.getComment())).append("'");
        }
    }

    private String genAlterAddColumn(String tableName, Field field) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(SQLUtils.wrapBackQuotes(tableName))
                .append(" ADD COLUMN ")
                .append(SQLUtils.wrapBackQuotes(field.getName()))
                .append(" ");
        genColumnAppendix(sb, field);
        return sb.toString();
    }

    private String genRenameColumn(String tableName, Field field) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(SQLUtils.wrapBackQuotes(tableName))
                .append(" CHANGE COLUMN ")
                .append(SQLUtils.wrapBackQuotes(field.getRenamedFrom()))
                .append(" ")
                .append(SQLUtils.wrapBackQuotes(field.getName()))
                .append(" ");
        genColumnAppendix(sb, field);
        return sb.toString();
    }

    private String genAlterChangeColumn(String tableName, Field field) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(SQLUtils.wrapBackQuotes(tableName))
                .append(" MODIFY COLUMN ")
                .append(SQLUtils.wrapBackQuotes(field.getName()))
                .append(" ");
        genColumnAppendix(sb, field);
        return sb.toString();
    }

    private String genAlterDropColumn(String tableName, String columnName) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(SQLUtils.wrapBackQuotes(tableName))
                .append(" DROP COLUMN ")
                .append(SQLUtils.wrapBackQuotes(columnName));
        return sb.toString();
    }

    private String genAlterAddIndex(String tableName, Index index) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(SQLUtils.wrapBackQuotes(tableName))
                .append(" ADD ");
        if (index.isUnique()) {
            sb.append("UNIQUE ");
        }
        sb.append("KEY ");
        sb.append(SQLUtils.wrapBackQuotes(index.getName())).append(" ");
        sb.append("(");

        List<String> indexFields = index.getFields();
        for (int i = 0; i < indexFields.size(); i++) {
            sb.append(SQLUtils.wrapBackQuotes(indexFields.get(i)));
            if (i != indexFields.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        sb.append(index.isLocal() ? " LOCAL" : " GLOBAL");
        return sb.toString();
    }

    private List<String> genAlterChangeIndex(String tableName, Index index) {
        return Arrays.asList(
                genAlterDropIndex(tableName, index.getName()),
                genAlterAddIndex(tableName, index));
    }

    private String genAlterDropIndex(String tableName, String indexName) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(SQLUtils.wrapBackQuotes(tableName))
                .append(" DROP KEY ")
                .append(SQLUtils.wrapBackQuotes(indexName));
        return sb.toString();
    }

    private String genAlterTableDef(TableDefinition tableDef, String tableName, TableDiff.Element changedElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(SQLUtils.wrapBackQuotes(tableName));
        switch (changedElement) {
            case AUTO_INCREMENT: {
                if (tableDef.getAutoIncrement() != null) {
                    sb.append(" SET AUTO_INCREMENT = ")
                            .append(tableDef.getAutoIncrement());
                }
                break;
            }
            case COMMENT: {
                sb.append(" SET COMMENT = ").append("'");
                if (tableDef.getComment() != null) {
                    sb.append(tableDef.getComment());
                }
                sb.append("'");
            }
            default:
        }

        return sb.toString();
    }

    public String generateRenameTable(String from, String to) {
        return "ALTER TABLE `" + from + "` RENAME `" + to + "`";
    }

    public String generateDropTable(String name) {
        return "DROP TABLE IF EXISTS `" + name + "`";
    }
}
