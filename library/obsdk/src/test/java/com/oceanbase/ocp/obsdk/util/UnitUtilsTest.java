package com.oceanbase.ocp.obsdk.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitUtilsTest {

    @Test
    public void formatUnitConfig() {
        String aliasName =
                UnitUtils.formatUnitConfig(1.5d, 2.0d, 6L * 1024 * 1024 * 1024, 7L * 1024 * 1024 * 1024);
        assertEquals("2C7G", aliasName);
    }
}
