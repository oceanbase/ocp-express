package com.oceanbase.ocp.bootstrap.util;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.oceanbase.ocp.bootstrap.spi.AfterDataInitializationHook;

public class ServiceUtilsTest {

    @Test
    public void loadServices() {
        List<AfterDataInitializationHook> hooks = ServiceUtils.loadServices(AfterDataInitializationHook.class);
        assertTrue(hooks.size() > 0);
        AfterDataInitializationHook hook1 = hooks.get(0);

        hooks = ServiceUtils.loadServices(AfterDataInitializationHook.class);
        assertTrue(hooks.size() > 0);
        AfterDataInitializationHook hook2 = hooks.get(0);
        assertSame(hook1, hook2);
    }
}
