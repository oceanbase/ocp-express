package com.oceanbase.ocp.bootstrap.core.def;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class DataDefinitionTest {

    @Test
    public void fromToConfig() {
        DataDefinition dataDefinition = new DataDefinition("test", "test1");
        dataDefinition.setRows(Arrays.asList(
                new Row(ImmutableMap.of("id", 1, "name", "alice", "class", 10)),
                new Row(ImmutableMap.of("id", 2, "name", "bob ", "class", 11))));
        dataDefinition.setDelete(Arrays.asList(
                new Row(ImmutableMap.of("id", 3)),
                new Row(ImmutableMap.of("id", 4))));
        dataDefinition.setOnDuplicateUpdate(Collections.singletonList("class"));
        Map<String, Object> configItems = dataDefinition.toConfigItems();
        assertEquals(4, configItems.size());
        DataDefinition dataDefinition2 = DataDefinition.fromConfig("test", configItems);
        assertEquals(dataDefinition, dataDefinition2);
    }

}
