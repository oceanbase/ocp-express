package com.oceanbase.ocp.bootstrap.core.def;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

public class MigrationTest {

    @Test
    public void testMigration() {
        Migration migration = new Migration("test");
        migration.setSourceSql("SELECT 1 f FROM dual");
        migration.setOnDuplicateUpdate(Collections.singletonList("f"));
        migration.setTargetTable("test2");
        migration.setSince("4.0.0");
        migration.setExpr("data");
        Map<String, Object> configItems = migration.toConfigItems();
        Migration migration1 = Migration.fromConfig("test", configItems);
        assertEquals(migration.getSourceSql(), migration1.getSourceSql());
        assertEquals(migration.getTargetTable(), migration1.getTargetTable());
        assertEquals(migration.getExpr(), migration1.getExpr());
    }

    @Test
    public void testMigration2() {
        Migration migration = new Migration("test");
        migration.setCondition("oldOcpVersion().before('4.0.0')");
        migration.setRawSqls(Arrays.asList("SELECT 1 a FROM dual", "SELECT 2 b FROM dual"));
        Map<String, Object> configItems = migration.toConfigItems();
        Migration migration1 = Migration.fromConfig("test", configItems);
        assertEquals(migration.getCondition(), migration1.getCondition());
        assertEquals(migration.getRawSqls(), migration1.getRawSqls());
    }
}
