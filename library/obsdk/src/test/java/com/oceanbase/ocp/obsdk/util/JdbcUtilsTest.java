package com.oceanbase.ocp.obsdk.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import com.oceanbase.ocp.obsdk.model.Table;
import com.oceanbase.ocp.obsdk.model.Table.Row;

public class JdbcUtilsTest {

    @Test
    public void extractRowSet() {
        SqlRowSet rowSet = mock(SqlRowSet.class);
        SqlRowSetMetaData metaData = mock(SqlRowSetMetaData.class);
        when(rowSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(anyInt())).thenAnswer(invocation -> {
            int columnIndex = (Integer) invocation.getArguments()[0];
            return "col" + columnIndex;
        });

        when(rowSet.next()).thenReturn(true, true, true, false);
        when(rowSet.getObject(anyInt())).thenAnswer(invocation -> {
            int columnIndex = (Integer) invocation.getArguments()[0];
            return "val" + columnIndex;
        });

        Table table = JdbcUtils.extractRowSet(rowSet);
        assertEquals(2, table.getColumnNames().size());
        assertEquals(3, table.getRows().size());
        for (Row row : table.getRows()) {
            assertEquals(2, row.getItems().size());
        }
    }
}
