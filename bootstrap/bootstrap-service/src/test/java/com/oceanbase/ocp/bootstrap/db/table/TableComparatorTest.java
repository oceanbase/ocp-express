package com.oceanbase.ocp.bootstrap.db.table;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.bootstrap.core.def.DataType;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;

import junit.framework.TestCase;

public class TableComparatorTest extends TestCase {

    public void testCompare() {
        TableComparator tableComparator = new TableComparator();
        TableDefinition table1 = new TableDefinition();
        // comment changed
        table1.setComment("this is table1");
        // autoIncrement changed
        table1.setAutoIncrement(1L);
        List<TableDefinition.Field> table1Fields = new ArrayList<>();
        // table1
        // f1 column changed
        TableDefinition.Field table1Field1 = new TableDefinition.Field();
        table1Field1.setName("f1");
        table1Field1.setType(DataType.fromString("int(64)"));
        table1Field1.setComment("f1 comment");
        table1Field1.setNullable(false);
        table1Field1.setDefaultValue(1);
        table1Field1.setAutoIncrement(false);
        table1Fields.add(table1Field1);
        table1.setFields(table1Fields);

        // f2 column unchanged
        TableDefinition.Field table1Field2 = new TableDefinition.Field();
        table1Field2.setName("f2");
        table1Field2.setType(DataType.fromString("varchar(64)"));
        table1Field2.setComment("f2 comment");
        table1Field2.setNullable(false);
        table1Field2.setDefaultValue("f2");
        table1Field2.setAutoIncrement(false);
        table1Fields.add(table1Field2);
        table1.setFields(table1Fields);

        // f4 column deleted
        TableDefinition.Field table1Field4 = new TableDefinition.Field();
        table1Field4.setName("f4");
        table1Field4.setType(DataType.fromString("varchar(64)"));
        table1Field4.setComment("f4 comment");
        table1Field4.setNullable(false);
        table1Field4.setDefaultValue("f4");
        table1Field4.setAutoIncrement(false);
        table1Fields.add(table1Field4);
        table1.setFields(table1Fields);

        List<TableDefinition.Index> table1Indices = new ArrayList<>();
        // i4 column modified
        TableDefinition.Index table1Index4 = new TableDefinition.Index();
        table1Index4.setName("i4");
        table1Index4.setFields(Lists.newArrayList("f1", "f2"));
        table1Index4.setDrop(false);
        table1Index4.setLocal(true);
        table1Index4.setUnique(true);
        table1Indices.add(table1Index4);
        table1.setIndexes(table1Indices);

        // table2
        // f1 column changed
        TableDefinition table2 = new TableDefinition();
        // comment changed
        table2.setComment("this is table2");
        // autoIncrement changed
        table2.setAutoIncrement(2L);
        List<TableDefinition.Field> table2Fields = new ArrayList<>();
        TableDefinition.Field table2Field1 = new TableDefinition.Field();
        table2Field1.setName("f1");
        table2Field1.setType(DataType.fromString("int(32)"));
        table2Field1.setComment("f1 comment");
        table2Field1.setNullable(false);
        table2Field1.setDefaultValue(1);
        table2Field1.setAutoIncrement(false);
        table2Fields.add(table2Field1);
        table2.setFields(table2Fields);

        // f2 column unchanged
        TableDefinition.Field table2Field2 = new TableDefinition.Field();
        table2Field2.setName("f2");
        table2Field2.setType(DataType.fromString("varchar(64)"));
        table2Field2.setComment("f2 comment");
        table2Field2.setNullable(false);
        table2Field2.setDefaultValue("f2");
        table2Field2.setAutoIncrement(false);
        table2Fields.add(table2Field2);
        table2.setFields(table2Fields);

        // f3 column added
        TableDefinition.Field table2Field3 = new TableDefinition.Field();
        table2Field3.setName("f3");
        table2Field3.setType(DataType.fromString("varchar(64)"));
        table2Field3.setComment("f3 comment");
        table2Field3.setNullable(false);
        table2Field3.setDefaultValue("f3");
        table2Field3.setAutoIncrement(false);
        table2Fields.add(table2Field3);
        table2.setFields(table2Fields);

        // f4 deleted
        TableDefinition.Field table2Field4 = new TableDefinition.Field();
        table2Field4.setName("f4");
        table2Field4.setType(DataType.fromString("varchar(64)"));
        table2Field4.setComment("f4 comment");
        table2Field4.setNullable(false);
        table2Field4.setDefaultValue("f4");
        table2Field4.setDrop(true);
        table2Field4.setAutoIncrement(false);
        table2Fields.add(table2Field4);
        table2.setFields(table2Fields);


        List<TableDefinition.Index> table2Indices = new ArrayList<>();
        // i4 changed
        TableDefinition.Index table2Index4 = new TableDefinition.Index();
        table2Index4.setName("i4");
        table2Index4.setFields(Lists.newArrayList("f1", "f2", "f3"));
        table2Index4.setDrop(false);
        table2Index4.setLocal(true);
        table2Index4.setUnique(true);
        table2Indices.add(table2Index4);
        table2.setIndexes(table2Indices);

        TableDiff tableDiff = tableComparator.compare(table1, table2);
        assertEquals(1, tableDiff.getChangedFields().size());
        assertEquals(1, tableDiff.getAddedFields().size());
        assertEquals(1, tableDiff.getDeletedFields().size());
        assertEquals(1, tableDiff.getChangedIndices().size());
        assertEquals(2, tableDiff.getChangedDefinitions().size());
    }
}
