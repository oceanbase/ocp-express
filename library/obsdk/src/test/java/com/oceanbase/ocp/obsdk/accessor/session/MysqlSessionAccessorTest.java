package com.oceanbase.ocp.obsdk.accessor.session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.model.Sort;
import com.oceanbase.ocp.obsdk.model.Sort.Direction;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionStats;

@RunWith(MockitoJUnitRunner.class)

public class MysqlSessionAccessorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void killSession_sqlShouldCorrectWhenNormal() {
        MysqlSessionAccessor accessor = new MysqlSessionAccessor(template);
        accessor.killSession(11212L);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("KILL 11212", sqlCaptor.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void killSession_shouldThrowExceptionWhenInputNull() {
        MysqlSessionAccessor accessor = new MysqlSessionAccessor(template);
        accessor.killSession(null);
    }

    @Test
    public void killQuery_sqlShouldCorrectWhenNormal() {
        MysqlSessionAccessor accessor = new MysqlSessionAccessor(template);
        accessor.killQuery(11212L);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("KILL QUERY 11212", sqlCaptor.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void killQuery_shouldThrowExceptionWhenInputNull() {
        MysqlSessionAccessor accessor = new MysqlSessionAccessor(template);
        accessor.killQuery(null);
    }

    @Test
    public void listSession_expect_ok() {
        MysqlSessionAccessor accessor = new MysqlSessionAccessor(template);
        ObSession obSession = new ObSession();
        obSession.setHost("127.0.0.1:2881");
        when(template.query(anyString(), any(), any())).thenReturn(Collections.singletonList(obSession));
        accessor.listSession(SessionQueryCondition.builder()
                .dbName("db")
                .sort(Sort.by(Direction.ASC, "id"))
                .page(1)
                .size(5)
                .dbUser("user")
                .activeOnly(true)
                .clientIp("127.0.0.1")
                .tenantName("sys").build());
    }

    @Test
    public void countSession_expect_ok() {
        MysqlSessionAccessor accessor = new MysqlSessionAccessor(template);
        when(template.queryForObject(anyString(), any(), any(Class.class))).thenReturn(0L);
        accessor.countSession(SessionQueryCondition.builder().tenantName("sys").build());
    }

    @Test
    public void getSessionStats_expect_ok() {
        SessionStats sessionStats = new SessionStats();
        MysqlSessionAccessor accessor = new MysqlSessionAccessor(template);
        when(template.queryForObject(anyString(), any(), any(BeanPropertyRowMapper.class)))
                .thenReturn(sessionStats, Collections.emptyList(), Collections.emptyList(), 1L,
                        Collections.emptyList());
        SessionStats stats = accessor.getSessionStats("sys");
        Assert.assertNotNull(stats);
    }

}
