package com.oceanbase.ocp.bootstrap.db.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import com.oceanbase.ocp.bootstrap.config.env.ELEnv;
import com.oceanbase.ocp.bootstrap.config.env.JavaxELEnv;
import com.oceanbase.ocp.bootstrap.core.def.DataDefinition;
import com.oceanbase.ocp.bootstrap.core.def.Migration;
import com.oceanbase.ocp.bootstrap.core.def.Row;

public class DataSqlGeneratorTest {

    @Test
    public void generate() {
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        DataDefinition dataDefinition = new DataDefinition("test", "test1");
        dataDefinition.setRows(Arrays.asList(
                new Row(ImmutableMap.of("id", 1, "name", "alice", "class", 10)),
                new Row(ImmutableMap.of("id", 2, "name", "bob", "class", 11)),
                new Row(ImmutableMap.of("id", 3, "name", "cathy", "class", 10))));
        dataDefinition.setDelete(Arrays.asList(
                new Row(ImmutableMap.of("id", 4)),
                new Row(ImmutableMap.of("id", 5))));
        List<String> sqlList = dataSqlGenerator.generateInsert(dataDefinition);
        assertEquals(3, sqlList.size());
        sqlList = dataSqlGenerator.generateDelete(dataDefinition);
        assertEquals(2, sqlList.size());
    }

    @Test
    public void generateForRow() {
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        DataDefinition dataDefinition = new DataDefinition("test", "test1");
        Row row = new Row(ImmutableMap.of("id", 1, "name", "alice", "class", 10));
        String sql = dataSqlGenerator.generateForInsertRow(dataDefinition.getTableName(), row,
                dataDefinition.getOnDuplicateUpdate());
        assertEquals("INSERT IGNORE INTO `test1`(`id`,`name`,`class`) VALUES (1,'alice',10)",
                sql);

        dataDefinition.setOnDuplicateUpdate(Collections.singletonList("class"));
        sql = dataSqlGenerator.generateForInsertRow(dataDefinition.getTableName(), row,
                dataDefinition.getOnDuplicateUpdate());
        assertEquals(
                "INSERT INTO `test1`(`id`,`name`,`class`) VALUES (1,'alice',10) ON DUPLICATE KEY UPDATE `class`=10",
                sql);
    }


    @Test
    public void generateForDelete() {
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        DataDefinition dataDefinition = new DataDefinition("test", "test1");
        Row row = new Row(ImmutableMap.of("id", 1, "name", "alice"));
        String sql = dataSqlGenerator.generateForDeleteRow(dataDefinition.getTableName(), row);
        assertEquals("DELETE FROM `test1` WHERE `id`=1 AND `name`='alice'", sql);
        row = new Row(ImmutableMap.of("id", 1));
        sql = dataSqlGenerator.generateForDeleteRow(dataDefinition.getTableName(), row);
        assertEquals("DELETE FROM `test1` WHERE `id`=1", sql);
    }

    @Test
    public void generateForUpdate() {
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        Row row = new Row(ImmutableMap.of("id", 1, "name", "alice"));
        String sql = dataSqlGenerator.generateForUpdateRow("test", row, Collections.singletonList("id"));
        assertEquals("UPDATE `test` SET `name`='alice' WHERE `id`=1", sql);
    }

    @Test
    public void dataToSql() {
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        Migration migration = new Migration("test");
        migration.setTargetTable("t1");
        List<Row> rows = Arrays.asList(
                new Row(ImmutableMap.of("id", 1, "name", "aa")),
                new Row(ImmutableMap.of("id", 2, "name", "bb")));
        List<String> sqls = dataSqlGenerator.dataToSql(migration, rows.stream());
        assertEquals(2, sqls.size());
        assertTrue(sqls.get(0).startsWith("INSERT "));
        migration.setUpdateBy(Collections.singletonList("id"));
        sqls = dataSqlGenerator.dataToSql(migration, rows.stream());
        assertEquals(2, sqls.size());
        assertTrue(sqls.get(0).startsWith("UPDATE "));

        migration.setUpdateBy(null);
        migration.setDeleteBy(Collections.singletonList("id"));
        sqls = dataSqlGenerator.dataToSql(migration, rows.stream());
        assertEquals(2, sqls.size());
        assertTrue(sqls.get(0).startsWith("DELETE "));
    }

    @Test
    public void processRows() {
        ELEnv elEnv = new JavaxELEnv();
        DataSqlGenerator dataSqlGenerator = new DataSqlGenerator();
        Migration migration = new Migration("test");
        migration.setTargetTable("t1");
        migration.setWith(Collections.singletonMap("t", "'p:'"));
        migration.setExpr("data.stream().map(r -> { 'id':r.id, 'v': concat(t, r.name) }).toList()");

        List<Row> rows = Arrays.asList(
                new Row(ImmutableMap.of("id", 1, "name", "aa")),
                new Row(ImmutableMap.of("id", 2, "name", "bb")));

        Stream<Row> rowStream = dataSqlGenerator.processRows(migration, elEnv, rows);
        List<Row> out = rowStream.collect(Collectors.toList());
        assertEquals(2, out.size());
        assertEquals(1, out.get(0).get("id"));
        assertEquals("p:aa", out.get(0).get("v"));
    }
}
