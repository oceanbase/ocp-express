package com.oceanbase.ocp.bootstrap.db.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.bootstrap.core.def.DataType;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Field;

public class AlterTableGeneratorTest {

    @Test
    public void testGenerate() {
        AlterTableGenerator alterTableGenerator = new AlterTableGenerator();
        TableDiff tableDiff = new TableDiff();
        TableDefinition.Field addedField = new TableDefinition.Field();
        addedField.setName("field1");
        addedField.setType(DataType.fromString("int(64)"));
        addedField.setNullable(false);
        addedField.setAutoIncrement(false);
        addedField.setDefaultValue(1);
        addedField.setComment("test added field1");
        List<TableDefinition.Field> addedFieldList = new ArrayList<>();
        addedFieldList.add(addedField);
        tableDiff.setAddedFields(addedFieldList);

        TableDefinition.Field changedField = new TableDefinition.Field();
        changedField.setName("field2");
        changedField.setType(DataType.fromString("varchar(64)"));
        changedField.setNullable(false);
        changedField.setAutoIncrement(false);
        changedField.setDefaultValue("default value");
        changedField.setComment("test changed field2");
        List<TableDefinition.Field> changedFieldList = new ArrayList<>();
        changedFieldList.add(changedField);
        tableDiff.setChangedFields(changedFieldList);

        TableDefinition.Field deletedField = new TableDefinition.Field();
        deletedField.setName("field3");
        deletedField.setType(DataType.fromString("varchar(128)"));
        deletedField.setNullable(false);
        deletedField.setAutoIncrement(false);
        deletedField.setDefaultValue("default value3");
        deletedField.setComment("test deleted field3");
        deletedField.setDrop(true);
        List<TableDefinition.Field> deletedFieldList = new ArrayList<>();
        deletedFieldList.add(deletedField);
        tableDiff.setDeletedFields(deletedFieldList);

        TableDefinition.Index addedUniqueIndex = new TableDefinition.Index();
        addedUniqueIndex.setName("index1");
        addedUniqueIndex.setFields(Lists.newArrayList("field1", "field2"));
        addedUniqueIndex.setLocal(false);
        addedUniqueIndex.setDelayed(false);
        addedUniqueIndex.setUnique(true);
        List<TableDefinition.Index> addedIndexList = new ArrayList<>();
        addedIndexList.add(addedUniqueIndex);

        TableDefinition.Index addedIndexDelayed = new TableDefinition.Index();
        addedIndexDelayed.setName("index2");
        addedIndexDelayed.setFields(Lists.newArrayList("field3"));
        addedIndexDelayed.setLocal(false);
        addedIndexDelayed.setDelayed(true);
        addedIndexDelayed.setUnique(false);
        addedIndexList.add(addedIndexDelayed);
        tableDiff.setAddedIndices(addedIndexList);

        TableDefinition.Index deletedIndex = new TableDefinition.Index();
        deletedIndex.setName("index3");
        deletedIndex.setFields(Lists.newArrayList("field3"));
        deletedIndex.setLocal(true);
        deletedIndex.setDelayed(false);
        deletedIndex.setUnique(false);
        List<TableDefinition.Index> deletedIndexList = new ArrayList<>();
        deletedIndexList.add(deletedIndex);

        TableDefinition.Index deletedIndexDelayed = new TableDefinition.Index();
        deletedIndexDelayed.setName("index4");
        deletedIndexDelayed.setFields(Lists.newArrayList("field4"));
        deletedIndexDelayed.setLocal(true);
        deletedIndexDelayed.setDelayed(true);
        deletedIndexDelayed.setUnique(false);
        deletedIndexList.add(deletedIndexDelayed);
        tableDiff.setDeletedIndices(deletedIndexList);


        TableDefinition tableDefinition = new TableDefinition();
        tableDefinition.setName("table1");
        tableDefinition.setComment("comment1");
        tableDefinition.setAutoIncrement(100L);
        tableDefinition.setDefaultCharset("utf-8");
        tableDiff.setNewTable(tableDefinition);
        tableDiff.setOldTable(tableDefinition);

        List<TableDiff.Element> changedDefinitions =
                Lists.newArrayList(TableDiff.Element.COMMENT, TableDiff.Element.AUTO_INCREMENT);
        tableDiff.setChangedDefinitions(changedDefinitions);
        AlterTables alterTables = alterTableGenerator.generate(tableDiff);

        assertEquals(1, alterTables.getAddedColumns().size());
        assertEquals("ALTER TABLE `table1` ADD COLUMN `field1` int(64) NOT NULL DEFAULT 1 COMMENT 'test added field1'",
                alterTables.getAddedColumns().get(0));
        assertEquals(1, alterTables.getChangedColumns().size());
        assertEquals(
                "ALTER TABLE `table1` MODIFY COLUMN `field2` varchar(64) NOT NULL DEFAULT 'default value' COMMENT 'test changed field2'",
                alterTables.getChangedColumns().get(0));
        assertEquals(1, alterTables.getDeletedColumns().size());
        assertEquals("ALTER TABLE `table1` DROP COLUMN `field3`", alterTables.getDeletedColumns().get(0));
        assertEquals(1, alterTables.getAddedIndices().size());
        assertEquals("ALTER TABLE `table1` ADD UNIQUE KEY `index1` (`field1`,`field2`) GLOBAL",
                alterTables.getAddedIndices().get(0));
        assertEquals(1, alterTables.getAddedIndicesDelayed().size());
        assertEquals("ALTER TABLE `table1` ADD KEY `index2` (`field3`) GLOBAL",
                alterTables.getAddedIndicesDelayed().get(0));
        assertEquals(1, alterTables.getDeletedIndices().size());
        assertEquals("ALTER TABLE `table1` DROP KEY `index3`", alterTables.getDeletedIndices().get(0));
        assertEquals(1, alterTables.getDeletedIndicesDelayed().size());
        assertEquals("ALTER TABLE `table1` DROP KEY `index4`", alterTables.getDeletedIndicesDelayed().get(0));
        assertEquals(2, alterTables.getChangedTableDefs().size());
        assertEquals("ALTER TABLE `table1` SET COMMENT = 'comment1'", alterTables.getChangedTableDefs().get(0));
        assertEquals("ALTER TABLE `table1` SET AUTO_INCREMENT = 100", alterTables.getChangedTableDefs().get(1));
    }

    @Test
    public void isCompatible() {
        Field field1 = new Field();
        Field field2 = new Field();

        field1.setType(DataType.fromString("int(10)"));
        field1.setNullable(true);
        field2.setType(DataType.fromString("int(10)"));
        field2.setNullable(true);
        assertTrue(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setNullable(true);
        field2.setType(DataType.fromString("int(9)"));
        field2.setNullable(true);
        assertTrue(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setNullable(true);
        field2.setType(DataType.fromString("int(9)"));
        field2.setNullable(true);
        assertTrue(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setNullable(true);
        field2.setType(DataType.fromString("int(10) unsigned"));
        field2.setNullable(true);
        assertFalse(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setNullable(true);
        field2.setType(DataType.fromString("int(10)"));
        field2.setNullable(false);
        assertFalse(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setNullable(false);
        field2.setType(DataType.fromString("int(9)"));
        field2.setNullable(true);
        assertTrue(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setNullable(true);
        field2.setType(DataType.fromString("bigint(20)"));
        field2.setNullable(true);
        assertTrue(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setAutoIncrement(true);
        field2.setType(DataType.fromString("int(10)"));
        field2.setAutoIncrement(false);
        assertTrue(AlterTableGenerator.isCompatible(field1, field2));

        field1.setType(DataType.fromString("int(10)"));
        field1.setAutoIncrement(false);
        field2.setType(DataType.fromString("int(10)"));
        field2.setAutoIncrement(true);
        assertFalse(AlterTableGenerator.isCompatible(field1, field2));
    }
}
