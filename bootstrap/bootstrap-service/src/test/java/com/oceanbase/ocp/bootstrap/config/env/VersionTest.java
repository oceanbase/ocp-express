package com.oceanbase.ocp.bootstrap.config.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersion() {
        Version ver1 = new Version("4.0.1-RELEASE");
        assertEquals(Arrays.asList(4, 0, 1), ver1.getParts());
        assertEquals("4.0.1-RELEASE", ver1.getVersionString());
        assertEquals("RELEASE", ver1.getNote());
        assertFalse(new Version("4.0.1-RELEASE").same("4.0.1-SNAPSHOT"));
        assertTrue(new Version("4.0.2-RELEASE").after("4.0.1-SNAPSHOT"));
        assertTrue(new Version("4.0-RELEASE").after(new Version("3.3.1-RELEASE")));
        assertTrue(new Version("4.0-RELEASE").before("4.0.1-RELEASE"));
        assertTrue(new Version("4.0-RELEASE").before(new Version("4.0.0.1-RELEASE")));
        assertTrue(new Version("4.0-RELEASE").same(new Version("4.0.0-RELEASE")));
        assertTrue(new Version("4.0.1-20221010").after(new Version("4.0.1-20220101")));
        assertTrue(new Version("4.0.1-20220101111213").after("4.0.1-20220101"));
        assertTrue(new Version("4.0.1-20220102").after("4.0.1-20220101111213"));
        assertEquals(new Version("4.0.1-20221010"), new Version("4.0.1-20221010"));
    }
}
