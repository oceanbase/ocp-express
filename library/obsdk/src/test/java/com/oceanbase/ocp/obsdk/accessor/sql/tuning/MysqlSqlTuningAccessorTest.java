package com.oceanbase.ocp.obsdk.accessor.sql.tuning;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.oceanbase.ocp.obsdk.accessor.sql.tuning.model.ObOutline;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

import lombok.extern.slf4j.Slf4j;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class MysqlSqlTuningAccessorTest {

    @Test
    public void getAllOutline_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        SqlRowSet sqlRowSet = mock(SqlRowSet.class);
        when(sqlRowSet.next()).thenReturn(true).thenReturn(false);
        when(sqlRowSet.getLong("database_id")).thenReturn(2L);
        when(sqlRowSet.getString("outlineName")).thenReturn("otn");
        when(sqlRowSet.getObject("sql_id")).thenReturn("otn".getBytes());
        when(template.queryForRowSet(anyString(), any())).thenReturn(sqlRowSet);
        List<ObOutline> allOutline = new MysqlSqlTuningAccessor(template).getAllOutline(1L);
        Assert.assertEquals(1, allOutline.size());
    }

    @Test
    public void getAllConcurrentLimitOutline_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        SqlRowSet sqlRowSet = mock(SqlRowSet.class);
        when(sqlRowSet.next()).thenReturn(true).thenReturn(false);
        when(sqlRowSet.getLong("concurrent_num")).thenReturn(1L);
        when(sqlRowSet.getLong("database_id")).thenReturn(2L);
        when(sqlRowSet.getString("outlineName")).thenReturn("otn");
        when(template.queryForRowSet(anyString(), any())).thenReturn(sqlRowSet);
        List<ObOutline> allOutline = new MysqlSqlTuningAccessor(template).getAllConcurrentLimitOutline(1L);
        Assert.assertEquals(1, allOutline.size());
    }

    @Test
    public void createOutlineToTargetStatement_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        when(template.getConnectProperties()).thenReturn(ConnectProperties.builder().database("test").build());
        new MysqlSqlTuningAccessor(template).createOutline("test", "otn", "statement", "statement");
        verify(template).execute(anyString());
    }

    @Test
    public void createOutline_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        when(template.getConnectProperties()).thenReturn(ConnectProperties.builder().database("test").build());
        new MysqlSqlTuningAccessor(template).createOutline("test", "otn", "sqlId", "hint");
        verify(template).execute(anyString());
    }

    @Test
    public void dropOutline_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        new MysqlSqlTuningAccessor(template).dropOutline("test", "otn");
        verify(template).execute(anyString());
    }

    @Test(expected = Exception.class)
    public void createOutline_with_syntax_error_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        when(template.getConnectProperties()).thenReturn(ConnectProperties.builder().database("test").build());
        new MysqlSqlTuningAccessor(template).createOutline("test", "otn", "sqlId", "hint");
        PowerMockito.doThrow(new Exception("You have an error in your SQL syntax")).when(template).execute(anyString());
    }

    @Test(expected = Exception.class)
    public void createOutline_with_outline_exists_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        when(template.getConnectProperties()).thenReturn(ConnectProperties.builder().database("test").build());
        new MysqlSqlTuningAccessor(template).createOutline("test", "otn", "sqlId", "hint");
        PowerMockito.doThrow(new Exception("already exists")).when(template).execute(anyString());
    }

}
