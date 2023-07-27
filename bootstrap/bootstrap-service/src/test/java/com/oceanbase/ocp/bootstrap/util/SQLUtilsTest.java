package com.oceanbase.ocp.bootstrap.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import javax.sql.DataSource;

import org.junit.Test;

import com.oceanbase.ocp.bootstrap.core.def.Const;

public class SQLUtilsTest {

    @Test
    public void isValidName() {
        assertTrue(SQLUtils.isValidName("hello"));
        assertTrue(SQLUtils.isValidName("__all_user"));
        assertTrue(SQLUtils.isValidName("a_1_2"));
        assertTrue(SQLUtils.isValidName("ABC"));
        assertFalse(SQLUtils.isValidName("1aaa"));
        assertFalse(SQLUtils.isValidName("a$$"));
    }

    @Test
    public void escape() {
        String escapedSQL = SQLUtils.escape("testa");
        assertSame("testa", escapedSQL);

        escapedSQL = SQLUtils.escape("test'a");
        assertEquals("test\\'a", escapedSQL);

        escapedSQL = SQLUtils.escape("test\"a");
        assertEquals("test\\\"a", escapedSQL);

        escapedSQL = SQLUtils.escape("test\\a");
        assertEquals("test\\\\a", escapedSQL);
    }

    @Test
    public void unescape() {
        String result = SQLUtils.unescape("testa");
        assertSame("testa", result);

        result = SQLUtils.unescape("test\\'a");
        assertEquals("test'a", result);

        result = SQLUtils.unescape("test\\\"a");
        assertEquals("test\"a", result);

        result = SQLUtils.unescape("test\\\\a");
        assertEquals("test\\a", result);

        result = SQLUtils.unescape("test\\r\\n\\t\\ba");
        assertEquals("test\r\n\t\ba", result);
    }

    @Test
    public void valueToString() {
        assertEquals("'2022-09-20 16:41:18.0'", SQLUtils.valueToString(new Date(1663663278000L)));
        assertEquals("'2022-09-20 16:41:18.0'", SQLUtils.valueToString(Instant.ofEpochSecond(1663663278L)));
        assertEquals("12345", SQLUtils.valueToString(12345));
        assertEquals("'123'", SQLUtils.valueToString("123"));
        assertEquals("true", SQLUtils.valueToString(true));
        assertEquals("CURRENT_TIMESTAMP", SQLUtils.valueToString(Const.CURRENT_TIMESTAMP));
        assertEquals("NULL", SQLUtils.valueToString(null));
    }

    @Test
    public void stringToValue() {
        assertEquals("2022-09-20 16:41:18", SQLUtils.stringToValue("'2022-09-20 16:41:18'"));
        assertEquals(123L, SQLUtils.stringToValue("123"));
        assertEquals(123.4, SQLUtils.stringToValue("123.4"));
        assertEquals(true, SQLUtils.stringToValue("true"));
        assertEquals(Const.CURRENT_TIMESTAMP, SQLUtils.stringToValue("CURRENT_TIMESTAMP"));
        assertEquals(Const.CURRENT_TIMESTAMP_6, SQLUtils.stringToValue("current_timestamp(6)"));
        assertNull(SQLUtils.stringToValue("null"));

    }

    @Test
    public void dataSourceUrl() {
        String url = SQLUtils.dataSourceUrl("1.1.1.1:2881", "test_db");
        assertTrue(url.startsWith("jdbc:oceanbase://1.1.1.1:2881/test_db?"));
    }

    @Test
    public void buildDataSource() {
        DataSource dataSource = SQLUtils.buildDataSource("127.0.0.1:2881", "test", "teat", "test");
        assertNotNull(dataSource);
    }

    @Test
    public void emptyBatch() throws SQLException {
        SQLUtils.executeBatch(null, Collections.emptyList());
    }
}
