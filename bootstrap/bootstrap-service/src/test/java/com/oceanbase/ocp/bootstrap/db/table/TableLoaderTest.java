package com.oceanbase.ocp.bootstrap.db.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.db.SqlQuerier;

public class TableLoaderTest {

    @Test
    public void load() throws SQLException {
        TableLoader tableLoader = new TableLoader(sql -> Collections.singletonList(new Row(ImmutableMap.of(
                "Table", "ocp_id_generator",
                "Create Table", "CREATE TABLE `ocp_id_generator` (\n"
                        + "  `id` int(11) NOT NULL DEFAULT '0',\n"
                        + "  PRIMARY KEY (`id`)\n"
                        + ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'lz4_1.0' REPLICA_NUM = 1 BLOCK_SIZE = "
                        + "16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 10 TABLEGROUP = 'tg_test_meta_400'"))));
        TableDefinition tableDefinition = tableLoader.load("ocp_id_generator");
        assertEquals("ocp_id_generator", tableDefinition.getName());
        // YamlLoader yamlLoader = new YamlLoader();
        // System.out.println(yamlLoader.toYaml(tableDefinition.toConfigItems()));

        tableLoader = new TableLoader(sql -> Collections.emptyList());
        assertNull(tableLoader.load("ocp_id_generator"));

    }

    @Test
    public void allTables() throws SQLException {
        TableLoader tableLoader = new TableLoader(sql -> Arrays.asList(
                new Row(Collections.singletonMap("Tables_in_test", "audit_event_history")),
                new Row(Collections.singletonMap("Tables_in_test", "audit_event_meta")),
                new Row(Collections.singletonMap("Tables_in_test", "backup_collect_file"))));
        List<String> allTables = tableLoader.allTableNames();
        assertEquals(3, allTables.size());
        // System.out.println(allTables);
    }

    @Test
    public void loadAll() throws SQLException {
        SqlQuerier querier = sql -> {
            sql = sql.toLowerCase();
            if (sql.startsWith("show create")) {
                return Collections.singletonList(new Row(ImmutableMap.of(
                        "Table", "ocp_id_generator",
                        "Create Table", "CREATE TABLE `ocp_id_generator` (\n"
                                + "  `id` int(11) NOT NULL DEFAULT '0',\n"
                                + "  PRIMARY KEY (`id`)\n"
                                + ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'lz4_1.0' REPLICA_NUM = 1 BLOCK_SIZE = "
                                + "16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 10 TABLEGROUP = 'tg_test_meta_400'")));
            } else {
                return Collections.singletonList(
                        new Row(Collections.singletonMap("Tables_in_test", "ocp_id_generator")));
            }
        };
        TableLoader tableLoader = new TableLoader(querier);
        Map<String, TableDefinition> all = tableLoader.loadAllTables();
        assertEquals(1, all.size());
        assertTrue(all.containsKey("ocp_id_generator"));
        // System.out.println(all);
    }
}
