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

import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;

import lombok.Data;

@Data
public class TableDiff {

    private TableDefinition newTable;
    private TableDefinition oldTable;


    public enum Element {
        COMMENT, AUTO_INCREMENT
    }

    private List<TableDefinition.Field> renamedFields;
    private List<TableDefinition.Field> addedFields;
    private List<TableDefinition.Field> changedFields;
    private List<TableDefinition.Field> deletedFields;

    private List<TableDefinition.Index> renamedIndices;
    private List<TableDefinition.Index> addedIndices;
    private List<TableDefinition.Index> changedIndices;
    private List<TableDefinition.Index> deletedIndices;

    private List<Element> changedDefinitions;

    private static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public boolean isEmpty() {
        return isEmpty(addedFields) && isEmpty(changedFields) && isEmpty(deletedFields) &&
                isEmpty(addedIndices) && isEmpty(changedIndices) && isEmpty(deletedIndices);
    }
}
