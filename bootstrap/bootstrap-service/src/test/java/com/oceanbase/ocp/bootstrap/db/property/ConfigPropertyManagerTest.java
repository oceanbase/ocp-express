package com.oceanbase.ocp.bootstrap.db.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import com.oceanbase.ocp.bootstrap.core.def.Row;

public class ConfigPropertyManagerTest {

    @Test
    public void extractValue() {
        String value;
        value = ConfigPropertyManager.extractValue(Collections.singletonList(new Row(
                ImmutableMap.of("value", "1", "default_value", "2"))));
        assertEquals("1", value);
        value = ConfigPropertyManager.extractValue(Collections.singletonList(new Row(
                ImmutableMap.of("default_value", "1"))));
        assertEquals("1", value);
        value = ConfigPropertyManager.extractValue(Collections.emptyList());
        assertNull(value);
    }
}
