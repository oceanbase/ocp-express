package com.oceanbase.ocp.bootstrap.db.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.bootstrap.core.def.Const;
import com.oceanbase.ocp.bootstrap.core.def.DataType;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Primary;

import junit.framework.TestCase;

public class CreateTableGeneratorTest extends TestCase {

    public void testGenerate() {
        CreateTableGenerator ctg = new CreateTableGenerator();
        TableDefinition td = new TableDefinition();
        List<TableDefinition.Field> fields = new ArrayList<>();
        TableDefinition.Field field1 = new TableDefinition.Field();
        field1.setName("col_a");
        field1.setType(DataType.fromString("int(64)"));
        field1.setNullable(false);
        field1.setDefaultValue(0);
        field1.setComment("test col_a");
        fields.add(field1);

        TableDefinition.Field field2 = new TableDefinition.Field();
        field2.setName("id");
        field2.setType(DataType.fromString("varchar(64)"));
        field2.setNullable(false);
        field2.setDefaultValue("def_va'l");
        field2.setComment("test id");
        fields.add(field2);

        TableDefinition.Field field3 = new TableDefinition.Field();
        field3.setName("update_time");
        field3.setType(DataType.fromString("datetime"));
        field3.setDefaultValue(Const.CURRENT_TIMESTAMP);
        field3.setOnUpdate(Const.CURRENT_TIMESTAMP);
        field3.setComment("修改时间");
        fields.add(field3);

        List<TableDefinition.Index> indices = new ArrayList<>();
        TableDefinition.Index index = new TableDefinition.Index();
        index.setFields(Arrays.asList("col_a"));
        index.setName("idx1");
        index.setUnique(true);
        index.setLocal(true);
        indices.add(index);

        td.setFields(fields);
        td.setAutoIncrement(0L);
        td.setDefaultCharset("utf8mb4");
        td.setName("test_table");
        td.setIndexes(indices);
        td.setPrimaryKey(new Primary(Arrays.asList("id", "col_a")));
        td.setComment("test table comment1");

        TableDefinition.Partition partition = new TableDefinition.Partition();
        partition.setType("RANGE");
        partition.setFields(Lists.newArrayList("col_a"));
        List<TableDefinition.Partition.RangeElement> rangeElements = new ArrayList<>();
        TableDefinition.Partition.RangeElement rangeElement1 = new TableDefinition.Partition.RangeElement();
        rangeElement1.setName("mp0");
        rangeElement1.setValue("2020");
        rangeElements.add(rangeElement1);
        TableDefinition.Partition.RangeElement rangeElement2 = new TableDefinition.Partition.RangeElement();
        rangeElement2.setName("mp1");
        rangeElement2.setValue("2021");
        rangeElements.add(rangeElement2);
        partition.setRangeElements(rangeElements);

        TableDefinition.Partition subPartition = new TableDefinition.Partition();
        subPartition.setType("RANGE");
        subPartition.setFields(Lists.newArrayList("col_b"));
        List<TableDefinition.Partition.RangeElement> subRangeElements = new ArrayList<>();
        TableDefinition.Partition.RangeElement subRangeElement1 = new TableDefinition.Partition.RangeElement();
        subRangeElement1.setName("sub_mp0");
        subRangeElement1.setValue("12020");
        subRangeElements.add(subRangeElement1);
        TableDefinition.Partition.RangeElement subRangeElement2 = new TableDefinition.Partition.RangeElement();
        subRangeElement2.setName("sub_mp1");
        subRangeElement2.setValue("12021");
        subRangeElements.add(subRangeElement2);
        subPartition.setRangeElements(subRangeElements);

        partition.setSubPartition(subPartition);
        td.setPartition(partition);

        String generatedSql = ctg.generate(td);

        String expectedSql = "CREATE TABLE IF NOT EXISTS `test_table`(\n" +
                "\t`col_a` int(64) NOT NULL DEFAULT 0 COMMENT 'test col_a',\n" +
                "\t`id` varchar(64) NOT NULL DEFAULT 'def_va\\'l' COMMENT 'test id',\n" +
                "\t`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',\n" +
                "\tPRIMARY KEY (`id`, `col_a`),\n" +
                "\tUNIQUE KEY idx1(`col_a`) LOCAL\n" +
                ") AUTO_INCREMENT = 0 DEFAULT CHARSET = utf8mb4 COMMENT = 'test table comment1'\n" +
                "PARTITION BY RANGE COLUMNS(`col_a`)\n" +
                "SUBPARTITION BY RANGE COLUMNS(`col_b`) SUBPARTITION TEMPLATE\n" +
                "(SUBPARTITION sub_mp0 VALUES LESS THAN(12020),\n" +
                "SUBPARTITION sub_mp1 VALUES LESS THAN(12021))\n" +
                "(PARTITION mp0 VALUES LESS THAN(2020),\n" +
                "PARTITION mp1 VALUES LESS THAN(2021))\n";
        Assert.assertEquals(expectedSql, generatedSql);

        TableDefinition.Partition subPartition1 = new TableDefinition.Partition();
        subPartition1.setType("HASH");
        subPartition1.setByExpr("expr1");
        subPartition1.setHashPartitionCount(10);
        partition.setSubPartition(subPartition1);
        partition.setByExpr("`end_interval_time`");
        partition.setFields(null);
        td.setPartition(partition);

        String generatedSql1 = ctg.generate(td);
        String expectedSql1 = "CREATE TABLE IF NOT EXISTS `test_table`(\n" +
                "\t`col_a` int(64) NOT NULL DEFAULT 0 COMMENT 'test col_a',\n" +
                "\t`id` varchar(64) NOT NULL DEFAULT 'def_va\\'l' COMMENT 'test id',\n" +
                "\t`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',\n" +
                "\tPRIMARY KEY (`id`, `col_a`),\n" +
                "\tUNIQUE KEY idx1(`col_a`) LOCAL\n" +
                ") AUTO_INCREMENT = 0 DEFAULT CHARSET = utf8mb4 COMMENT = 'test table comment1'\n" +
                "PARTITION BY RANGE (`end_interval_time`)\n" +
                "SUBPARTITION BY HASH (expr1) SUBPARTITIONS 10\n" +
                "(PARTITION mp0 VALUES LESS THAN(2020),\n" +
                "PARTITION mp1 VALUES LESS THAN(2021))\n";
        Assert.assertEquals(expectedSql1, generatedSql1);
    }

    public void testGenerateField() {
        TableDefinition.Field field = new TableDefinition.Field();
        field.setName("id");
        field.setType(DataType.fromString("varchar(64)"));
        field.setNullable(true);
        field.setDefaultValue("def_val");
        field.setComment("test 'id");

        CreateTableGenerator ctg = new CreateTableGenerator();
        StringBuilder sb = new StringBuilder();
        ctg.generateField(sb, field);
        String expectedSql = "`id` varchar(64) DEFAULT 'def_val' COMMENT 'test \\'id'";
        Assert.assertEquals(expectedSql, sb.toString());
    }

    public void testGenerateIndex() {
        TableDefinition.Index index = new TableDefinition.Index();
        index.setFields(Arrays.asList("col_a"));
        index.setName("idx1");
        index.setUnique(false);
        index.setLocal(false);

        CreateTableGenerator ctg = new CreateTableGenerator();
        StringBuilder sb = new StringBuilder();
        ctg.generateIndex(sb, index);
        String expectedSql = "KEY idx1(`col_a`) GLOBAL";
        Assert.assertEquals(expectedSql, sb.toString());
    }
}
