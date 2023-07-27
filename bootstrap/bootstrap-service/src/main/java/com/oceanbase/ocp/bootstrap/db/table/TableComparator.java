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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.TableMember;

import lombok.Data;

public class TableComparator {

    @Data
    public static class MemberWideDiff<T> {

        List<T> renamedList;
        List<T> deletedList;
        List<T> changedList;
        List<T> addedList;
    }

    public TableDiff compare(TableDefinition table1, TableDefinition table2) {
        TableDiff tableDiff = new TableDiff();
        tableDiff.setOldTable(table1);
        tableDiff.setNewTable(table2);
        MemberWideDiff<TableDefinition.Field> fieldDiff = getFieldDiff(table1, table2);
        tableDiff.setRenamedFields(fieldDiff.getRenamedList());
        tableDiff.setAddedFields(fieldDiff.getAddedList());
        tableDiff.setChangedFields(fieldDiff.getChangedList());
        tableDiff.setDeletedFields(fieldDiff.getDeletedList());

        MemberWideDiff<TableDefinition.Index> indexDiff = getIndexDiff(table1, table2);
        tableDiff.setRenamedIndices(indexDiff.getRenamedList());
        tableDiff.setAddedIndices(indexDiff.getAddedList());
        tableDiff.setChangedIndices(indexDiff.getChangedList());
        tableDiff.setDeletedIndices(indexDiff.getDeletedList());

        tableDiff.setChangedDefinitions(getTableWideDiff(table1, table2));
        return tableDiff;
    }

    private MemberWideDiff<TableDefinition.Field> getFieldDiff(TableDefinition table1, TableDefinition table2) {
        Map<String, TableDefinition.Field> table1FieldMap = new HashMap<>();
        for (TableDefinition.Field field : table1.getFields()) {
            table1FieldMap.put(field.getName(), field);
        }
        Map<String, TableDefinition.Field> table2FieldMap = new HashMap<>();
        for (TableDefinition.Field field : table2.getFields()) {
            table2FieldMap.put(field.getName(), field);
        }
        return getMemberWideDiff(table1FieldMap, table2FieldMap);
    }

    private MemberWideDiff<TableDefinition.Index> getIndexDiff(TableDefinition table1, TableDefinition table2) {
        Map<String, TableDefinition.Index> table1IndexMap = new HashMap<>();
        for (TableDefinition.Index index : table1.getIndexes()) {
            table1IndexMap.put(index.getName(), index);
        }
        Map<String, TableDefinition.Index> table2IndexMap = new HashMap<>();
        for (TableDefinition.Index index : table2.getIndexes()) {
            table2IndexMap.put(index.getName(), index);
        }
        return getMemberWideDiff(table1IndexMap, table2IndexMap);
    }

    private List<TableDiff.Element> getTableWideDiff(TableDefinition table1, TableDefinition table2) {

        List<TableDiff.Element> changedDefinitions = new ArrayList<>();

        String table1Comment = table1.getComment();
        String table2Comment = table2.getComment();
        if (table2Comment != null) {
            if (table1Comment == null || !table1Comment.equals(table2Comment)) {
                changedDefinitions.add(TableDiff.Element.COMMENT);
            }
        } else {
            if (table1Comment != null) {
                changedDefinitions.add(TableDiff.Element.COMMENT);
            }
        }

        Long table1AutoIncrement = table1.getAutoIncrement();
        Long table2AutoIncrement = table2.getAutoIncrement();
        if (table2AutoIncrement != null) {
            if (table1AutoIncrement != null) {
                if (table2AutoIncrement > table1AutoIncrement) {
                    changedDefinitions.add(TableDiff.Element.AUTO_INCREMENT);
                }
            } else {
                changedDefinitions.add(TableDiff.Element.AUTO_INCREMENT);
            }
        }

        return changedDefinitions;
    }


    private <T extends TableMember> MemberWideDiff<T> getMemberWideDiff(Map<String, T> map1, Map<String, T> map2) {
        List<T> renamedMembers = new ArrayList<>();
        List<T> deletedMembers = new ArrayList<>();
        List<T> changedMembers = new ArrayList<>();
        List<T> addedMembers = new ArrayList<>();
        for (Map.Entry<String, T> entry : map2.entrySet()) {
            T table2Member = entry.getValue();
            T table1Member = map1.get(entry.getKey());
            if (table2Member.isDrop()) {
                if (table1Member != null) {
                    deletedMembers.add(table2Member);
                }
                continue;
            }
            String renamedFrom = table2Member.getRenamedFrom();
            if (table1Member == null) {
                if (renamedFrom != null && map1.get(renamedFrom) != null) {
                    renamedMembers.add(table2Member);
                } else {
                    addedMembers.add(table2Member);
                }
                continue;
            }
            if (!table2Member.equals(table1Member)) {
                changedMembers.add(table2Member);
            }
        }

        MemberWideDiff<T> memberWideDiff = new MemberWideDiff<>();
        memberWideDiff.setRenamedList(renamedMembers);
        memberWideDiff.setAddedList(addedMembers);
        memberWideDiff.setChangedList(changedMembers);
        memberWideDiff.setDeletedList(deletedMembers);
        return memberWideDiff;
    }
}
