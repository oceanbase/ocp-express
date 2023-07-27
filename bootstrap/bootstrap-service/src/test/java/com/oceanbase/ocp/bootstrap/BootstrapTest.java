package com.oceanbase.ocp.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.oceanbase.ocp.bootstrap.core.Action;

public class BootstrapTest {

    @Test
    public void testBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        assertEquals(Action.UNKNOWN, bootstrap.getAction());
        assertEquals(8180, bootstrap.getPort());
        assertNotNull(bootstrap.getProgressHandler());

        String[] args = {"--bootstrap", "--install", "--port=8081", "--auth=admin:root",
                "--meta-address=127.0.0.1",
                "--meta-database=ocp",
                "--meta-user=meta_ocp"};
        bootstrap.initialize(args);
        assertTrue(bootstrap.isEnabled());
        assertEquals(Action.INSTALL, bootstrap.getAction());
        assertNotNull(bootstrap.getProgressHandler());
        assertNotNull(bootstrap.getProgress());
        assertNotNull(bootstrap.getDataSourceProvider());
        assertNotNull(bootstrap.getMetaPropertyInitializer());
        assertNotNull(bootstrap.getConfigLoader());
        assertEquals(8081, bootstrap.getPort());
        System.out.println(bootstrap.getOcpVersion());
        assertNotNull(bootstrap.getOcpVersion());
        assertNotNull(bootstrap.getOcpBuildTime());
        assertFalse(bootstrap.metaDbPropertiesReady());
        try {
            bootstrap.waitDbPropertiesReady();
            fail("in non-interactive mode, should throws");
        } catch (Exception ignore) {
            // ok
        }
    }
}
