package com.oceanbase.ocp.obsdk.accessor.sql.tuning;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.oceanbase.ocp.obsdk.accessor.sql.tuning.model.ObOutline;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

import lombok.extern.slf4j.Slf4j;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class OracleSqlTuningAccessorTest {

    @Test
    public void getAllConcurrentLimitOutline_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        SqlRowSet sqlRowSet = mock(SqlRowSet.class);
        when(sqlRowSet.next()).thenReturn(true).thenReturn(false);
        when(sqlRowSet.getLong("concurrent_num")).thenReturn(1L);
        when(sqlRowSet.getLong("database_id")).thenReturn(2L);
        when(sqlRowSet.getString("outlineName")).thenReturn("otn");
        when(template.queryForRowSet(anyString(), any())).thenReturn(sqlRowSet);
        List<ObOutline> allOutline = new OracleSqlTuningAccessor(template).getAllConcurrentLimitOutline(1L);
        Assert.assertEquals(1, allOutline.size());
    }

    @Test
    public void getAllConcurrentLimitOutline_on_version_4_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        SqlRowSet sqlRowSet = mock(SqlRowSet.class);
        when(sqlRowSet.next()).thenReturn(true).thenReturn(false);
        when(sqlRowSet.getLong("concurrent_num")).thenReturn(1L);
        when(sqlRowSet.getLong("database_id")).thenReturn(2L);
        when(sqlRowSet.getString("outlineName")).thenReturn("otn");
        when(template.queryForRowSet(anyString(), any())).thenReturn(sqlRowSet);
        when(sqlRowSet.getTimestamp("gmt_create")).thenReturn(Timestamp.from(Instant.now()));
        List<ObOutline> allOutline = new OracleSqlTuningAccessor(template).getAllConcurrentLimitOutline(1L);
        Assert.assertEquals(1, allOutline.size());
    }

    @Test
    public void createOutline_expect_exception() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        new OracleSqlTuningAccessor(template).createOutline("test", "otn", "statement", "statement");
        verify(template).executeWithSchema(anyString(), anyString());
    }

    @Test(expected = RuntimeException.class)
    public void dropOutline_expect_exception() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        doThrow(new RuntimeException("Error")).when(template).executeWithSchema(anyString(), anyString());
        new OracleSqlTuningAccessor(template).dropOutline("test", "otn");
    }

    @Test
    public void getAllOutline_on_version_4_expect_ok() {
        ObConnectTemplate template = mock(ObConnectTemplate.class);
        SqlRowSet sqlRowSet = mock(SqlRowSet.class);
        when(sqlRowSet.next()).thenReturn(true).thenReturn(false);
        when(sqlRowSet.getLong("database_id")).thenReturn(2L);
        when(sqlRowSet.getString("outlineName")).thenReturn("otn");
        when(sqlRowSet.getTimestamp("gmt_create")).thenReturn(Timestamp.from(OffsetDateTime.now().toInstant()));
        when(template.queryForRowSet(anyString(), any())).thenReturn(sqlRowSet);
        List<ObOutline> allOutline = new OracleSqlTuningAccessor(template).getAllOutline(1L);
        Assert.assertEquals(1, allOutline.size());
    }

}
