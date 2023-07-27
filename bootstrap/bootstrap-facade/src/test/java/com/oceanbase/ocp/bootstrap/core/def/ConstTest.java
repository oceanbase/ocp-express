package com.oceanbase.ocp.bootstrap.core.def;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConstTest {

    @Test
    public void testConst() {
        Const c = Const.valueOf("current_timestamp");
        assertEquals(Const.CURRENT_TIMESTAMP, c);
        assertEquals(Const.CURRENT_TIMESTAMP.hashCode(), c.hashCode());
        assertEquals("CURRENT_TIMESTAMP", c.getValue());

        c = Const.valueOf("current_timestamp(6)");
        assertEquals(Const.CURRENT_TIMESTAMP_6, c);
        assertEquals(Const.CURRENT_TIMESTAMP_6.hashCode(), c.hashCode());
        assertEquals("CURRENT_TIMESTAMP(6)", c.toString());
    }
}
