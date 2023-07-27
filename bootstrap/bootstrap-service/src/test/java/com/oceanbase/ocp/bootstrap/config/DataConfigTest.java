package com.oceanbase.ocp.bootstrap.config;

import org.junit.Test;

public class DataConfigTest {

    @Test
    public void testDataConfig() {
        DataConfig test = new DataConfig("test");
        test.tableDefinitions();
        test.dataDefinitions();
        test.migrations();
        test.getTableDefinition("a");
        test.getDataDefinition("a");
        test.getMigration("a");
    }
}
