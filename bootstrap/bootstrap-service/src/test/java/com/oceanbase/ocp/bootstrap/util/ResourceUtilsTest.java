package com.oceanbase.ocp.bootstrap.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.oceanbase.ocp.bootstrap.config.ConfigLoader;

public class ResourceUtilsTest {

    @Test
    public void listResourceDir() {
        List<String> names = ResourceUtils.listResourceDir(ConfigLoader.DIR_NAME, name -> name.endsWith(".yaml"));
        assertTrue(names.size() > 0);
        for (String name : names) {
            if (!name.endsWith(".yaml")) {
                fail("bad name included: " + name);
            }
        }
    }

    @Test
    public void loadResource() {
        String loaded = ResourceUtils.loadResource("test_resource.txt");
        assertEquals("hello world", loaded);
    }
}
