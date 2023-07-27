package com.oceanbase.ocp.bootstrap.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.doReturn;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableMap;

import com.oceanbase.ocp.bootstrap.config.DataConfig;
import com.oceanbase.ocp.bootstrap.config.YamlLoader;
import com.oceanbase.ocp.bootstrap.config.env.Version;
import com.oceanbase.ocp.bootstrap.core.Stage;
import com.oceanbase.ocp.bootstrap.core.def.DataDefinition;
import com.oceanbase.ocp.bootstrap.core.def.Migration;
import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.db.DbInitializer.SqlTask;
import com.oceanbase.ocp.bootstrap.progress.ProgressHandlerImpl;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SQLUtils.class)
public class DbInitializerTest {

    @Before
    public void setup() throws Exception {
        System.setProperty("OCP_EXPRESS_ADMIN_PASSWD", "WhatEver,,123");
        PowerMockito.spy(SQLUtils.class);
        doReturn(1).when(SQLUtils.class, "execute", eq(null), anyString(), any());
        doReturn(Collections.emptyList()).when(SQLUtils.class, "queryRows", eq(null), anyString(), any());
    }

    @Test
    public void handleRenameTables() {
        DbInitializer dbInitializer = new DbInitializer("test", null, null, ProgressHandlerImpl.nopProgressHandler());
        LinkedHashMap<String, TableDefinition> existTables = new LinkedHashMap<>();
        LinkedHashMap<String, TableDefinition> newTables = new LinkedHashMap<>();
        DataConfig dataConfig = new DataConfig("test", newTables, null, null);
        List<SqlTask> sqlTasks = dbInitializer.handleRenameTables(existTables, dataConfig);
        assertTrue(sqlTasks.isEmpty());

        TableDefinition table1 = new TableDefinition();
        table1.setName("a");
        TableDefinition table2 = new TableDefinition();
        table2.setName("b");
        table2.setRenamedFrom("a");
        existTables.put(table1.getName(), table1);
        newTables.put(table2.getName(), table2);
        sqlTasks = dbInitializer.handleRenameTables(existTables, dataConfig);
        assertEquals(1, sqlTasks.size());
        assertEquals(1, sqlTasks.get(0).getSqls().size());

        assertEquals("ALTER TABLE `a` RENAME `b`", sqlTasks.get(0).getSqls().get(0));
        assertFalse(existTables.containsKey("a"));
        assertTrue(existTables.containsKey("b"));
    }

    @Test
    public void handleRenameTablesRecur() {
        DbInitializer dbInitializer = new DbInitializer("test", null, null, ProgressHandlerImpl.nopProgressHandler());
        LinkedHashMap<String, TableDefinition> existTables = new LinkedHashMap<>();
        LinkedHashMap<String, TableDefinition> newTables = new LinkedHashMap<>();

        DataConfig dataConfig = new DataConfig("test", newTables, null, null);

        TableDefinition oldTable1 = new TableDefinition();
        oldTable1.setName("a");
        TableDefinition oldTable2 = new TableDefinition();
        oldTable2.setName("b");
        existTables.put(oldTable1.getName(), oldTable1);
        existTables.put(oldTable2.getName(), oldTable2);

        TableDefinition newTable1 = new TableDefinition();
        newTable1.setName("b");
        newTable1.setRenamedFrom("a");
        TableDefinition newTable2 = new TableDefinition();
        newTable2.setName("c");
        newTable2.setRenamedFrom("b");
        newTables.put(newTable1.getName(), newTable1);
        newTables.put(newTable2.getName(), newTable2);

        List<SqlTask> sqlTasks = dbInitializer.handleRenameTables(existTables, dataConfig);
        assertEquals(2, sqlTasks.size());
        assertEquals(1, sqlTasks.get(0).getSqls().size());
        assertEquals(1, sqlTasks.get(1).getSqls().size());

        assertEquals("ALTER TABLE `b` RENAME `c`", sqlTasks.get(0).getSqls().get(0));
        assertEquals("ALTER TABLE `a` RENAME `b`", sqlTasks.get(1).getSqls().get(0));

        assertFalse(existTables.containsKey("a"));
        assertTrue(existTables.containsKey("b"));
        assertTrue(existTables.containsKey("c"));
    }

    @Test
    public void install() {
        SqlQuerier querier = sql -> Collections.emptyList();

        List<String> executedSqls = new ArrayList<>();
        DbInitializer dbInitializer = new DbInitializer("test",
                executedSqls::addAll,
                querier,
                ProgressHandlerImpl.nopProgressHandler());
        String yaml = "data_source: metaDataSource\n"
                + "table_definitions:\n"
                + "  compute_region:\n"
                + "    fields:\n"
                + "      id: {type: bigint(20), nullable: false, auto_increment: true}\n"
                + "      name: {type: varchar(128), nullable: false, comment: 区域名称}\n"
                + "      create_time: {type: datetime, nullable: true, default_value: !const 'CURRENT_TIMESTAMP'}\n"
                + "      update_time: {type: datetime, nullable: true, default_value: !const 'CURRENT_TIMESTAMP', on_update: "
                + "!const 'CURRENT_TIMESTAMP' }\n"
                + "      description: {type: varchar(256), nullable: true, comment: 地域描述信息}\n"
                + "    indexes:\n"
                + "      uk_compute_region_name:\n"
                + "        fields: [name]\n"
                + "        unique: true\n"
                + "        local: false\n"
                + "    primary_key:\n"
                + "      fields: [id]\n"
                + "    comment: OCP计算资源模块-区域表\n"
                + "    default_charset: utf8mb4\n"
                + "data_definitions:\n"
                + "  compute_region:\n"
                + "    table_name: compute_region\n"
                + "    on_duplicate_update: null\n"
                + "    rows:\n"
                + "      - {id: 1, name: OCP_META_REGION, description: null}";
        LinkedHashMap<String, Object> map = new YamlLoader().load(yaml);
        // System.out.println(new Gson().toJson(((Map<String, Object>)
        // map.get("table_definitions")).get("compute_region")));
        TableDefinition tableDefinition = TableDefinition.fromConfig("compute_region",
                (Map<String, Object>) ((Map<String, Object>) map.get("table_definitions")).get("compute_region"));
        // System.out.println(new Gson().toJson(tableDefinition));
        DataDefinition dataDefinition = DataDefinition.fromConfig("compute_region",
                (Map<String, Object>) ((Map<String, Object>) map.get("data_definitions")).get("compute_region"));

        dbInitializer.install(new DataConfig("test", Collections.singletonMap(
                "compute_region", tableDefinition),
                Collections.singletonMap(
                        "compute_region", dataDefinition),
                Collections.emptyMap()));
        assertEquals(2, executedSqls.size());
        assertTrue(executedSqls.get(0).startsWith("CREATE TABLE"));
        assertTrue(executedSqls.get(1).startsWith("INSERT"));
        // System.out.println(executedSqls);
    }

    @Test
    public void upgrade() {
        List<String> executedSqls = new ArrayList<>();
        SqlQuerier querier = sql -> {
            sql = sql.toLowerCase();
            if (sql.startsWith("show create")) {
                return Collections.singletonList(new Row(ImmutableMap.of(
                        "Table", "compute_region",
                        "Create Table", "CREATE TABLE `compute_region` (\n"
                                + "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n"
                                + "  `name` varchar(128) NOT NULL COMMENT '区域名称',\n"
                                + "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,\n"
                                + "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
                                + "  `description` varchar(100) DEFAULT NULL COMMENT '地域描述信息',\n" // 改了一下 description
                                                                                                  // 的长度
                                + "  PRIMARY KEY (`id`),\n"
                                + "  UNIQUE KEY `uk_compute_region_name` (`name`) BLOCK_SIZE 16384 GLOBAL\n"
                                + ") AUTO_INCREMENT = 1000001 DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0'"
                                + " REPLICA_NUM = 1 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0 "
                                + "TABLEGROUP = 'ocp40x' COMMENT = 'OCP计算资源模块-区域表'")));
            } else {
                return Collections.singletonList(
                        new Row(Collections.singletonMap("Tables_in_test", "compute_region")));
            }
        };
        DbInitializer dbInitializer = new DbInitializer("test",
                executedSqls::addAll,
                querier,
                ProgressHandlerImpl.nopProgressHandler());
        String yaml = "data_source: metaDataSource\n"
                + "table_definitions:\n"
                + "  compute_region:\n"
                + "    fields:\n"
                + "      id: {type: bigint(20), nullable: false, auto_increment: true}\n"
                + "      name: {type: varchar(128), nullable: false, comment: 区域名称}\n"
                + "      create_time: {type: datetime, nullable: true, default_value: !const 'CURRENT_TIMESTAMP'}\n"
                + "      update_time: {type: datetime, nullable: true, default_value: !const 'CURRENT_TIMESTAMP', on_update: "
                + "!const 'CURRENT_TIMESTAMP' }\n"
                + "      description: {type: varchar(256), nullable: true, comment: 地域描述信息}\n"
                + "    indexes:\n"
                + "      uk_compute_region_name:\n"
                + "        fields: [name]\n"
                + "        unique: true\n"
                + "        local: false\n"
                + "    primary_key:\n"
                + "      fields: [id]\n"
                + "    comment: OCP计算资源模块-区域表\n"
                + "    default_charset: utf8mb4\n"
                + "data_definitions:\n"
                + "  compute_region:\n"
                + "    table_name: compute_region\n"
                + "    on_duplicate_update: null\n"
                + "    rows:\n"
                + "      - {id: 1, name: OCP_META_REGION, description: null}\n"
                + "migrations:\n"
                + "  test1:\n"
                + "    raw_sqls:\n"
                + "      - DELETE FROM compute_region WHERE id=1000\n";

        LinkedHashMap<String, Object> map = new YamlLoader().load(yaml);
        TableDefinition tableDefinition = TableDefinition.fromConfig("compute_region",
                (Map<String, Object>) ((Map<String, Object>) map.get("table_definitions")).get("compute_region"));
        DataDefinition dataDefinition = DataDefinition.fromConfig("test",
                (Map<String, Object>) ((Map<String, Object>) map.get("data_definitions")).get("compute_region"));
        Migration migration = Migration.fromConfig("test",
                (Map<String, Object>) ((Map<String, Object>) map.get("migrations")).get("test1"));

        dbInitializer.upgrade(new DataConfig("test",
                Collections.singletonMap("compute_region", tableDefinition),
                Collections.singletonMap("compute_region", dataDefinition),
                Collections.singletonMap("test", migration)));
        assertEquals(3, executedSqls.size());
        assertTrue(executedSqls.get(0).startsWith("ALTER TABLE"));
        assertTrue(executedSqls.get(1).startsWith("INSERT"));
        assertTrue(executedSqls.get(2).startsWith("DELETE"));
        // System.out.println(executedSqls);
    }

    @Test
    public void executeSqlTaskFail() {
        SqlQuerier querier = sql -> Collections.emptyList();
        DbInitializer dbInitializer = new DbInitializer("test",
                sqls -> {
                    throw new BatchUpdateException(new int[] {Statement.EXECUTE_FAILED}, new SQLException());
                },
                querier,
                ProgressHandlerImpl.nopProgressHandler());
        try {
            dbInitializer.executeSqlTasks(Stage.DEFAULT_DATA, Collections.singletonList(
                    new SqlTask("test_task", Collections.singletonList("SELECT 1 FROM dual"))));
            fail("should has exception!");
        } catch (IllegalStateException e) {
            assertTrue(e.getCause() instanceof SQLException);
            // System.out.println(e.getCause().getMessage());
            assertTrue(e.getCause().getMessage().contains("test_task"));
            assertTrue(e.getCause().getMessage().contains("SELECT 1 FROM dual"));
        }
    }

    @Test
    public void getObVersion() {
        SqlQuerier querier = sql -> Collections.singletonList(new Row(Collections.singletonMap(
                "@@version_comment",
                "OceanBase 2.2.77 (r20210819232008-ff9ad99ef4844ba8592609d2f73be88c8a7a4418) (Built Aug 19 2021 23:53:35)")));
        DbInitializer dbInitializer = new DbInitializer("test", null, querier, null);
        Version version = dbInitializer.getObVersion();
        assertTrue(version.same("2.2.77"));


        querier = sql -> Collections.emptyList();
        dbInitializer = new DbInitializer("test", null, querier, null);
        assertNull(dbInitializer.getObVersion());

        querier = sql -> Collections.singletonList(new Row(Collections.singletonMap(
                "a", "b")));
        dbInitializer = new DbInitializer("test", null, querier, null);
        assertNull(dbInitializer.getObVersion());
    }
}
