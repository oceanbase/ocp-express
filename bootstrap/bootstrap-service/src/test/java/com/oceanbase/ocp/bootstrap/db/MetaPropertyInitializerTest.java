package com.oceanbase.ocp.bootstrap.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;

public class MetaPropertyInitializerTest {

    @Ignore
    @Test
    public void metaPropertyInitialize() {
        String[] keys = new String[] {
                "JDBC_URL", "JDBC_USERNAME", "JDBC_PASSWORD"
        };
        Map<String, String> backup = new HashMap<>();
        for (String key : keys) {
            backup.put(key, System.clearProperty(key));
        }
        try {
            MetaPropertyInitializer initializer = new MetaPropertyInitializer();
            assertFalse(initializer.isPropertyReady());
            System.setProperty("JDBC_USERNAME", "test");
            assertFalse(initializer.isPropertyReady());
            initializer.initialize("127.0.0.1:2881", "test", "user", "pswd");
            assertTrue(initializer.isPropertyReady());
        } finally {
            for (Entry<String, String> entry : backup.entrySet()) {
                if (entry.getValue() != null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
