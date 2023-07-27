package com.oceanbase.ocp.bootstrap.core.def;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class RowTest {

    @Test
    public void testRow() {
        Row row = new Row(ImmutableMap.of("id", 1, "name", "alice", "class", 10));
        assertEquals(10, row.get("class"));
        assertEquals(1, row.getFirstField());
    }

}
