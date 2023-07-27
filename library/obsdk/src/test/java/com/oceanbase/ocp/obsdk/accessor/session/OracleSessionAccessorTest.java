package com.oceanbase.ocp.obsdk.accessor.session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.model.Sort;
import com.oceanbase.ocp.obsdk.model.Sort.Direction;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;

public class OracleSessionAccessorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void killSession_sqlShouldCorrectWhenNormal() {
        OracleSessionAccessor accessor = new OracleSessionAccessor(template);
        accessor.killSession(11212L);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("KILL 11212", sqlCaptor.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void killSession_shouldThrowExceptionWhenInputNull() {
        OracleSessionAccessor accessor = new OracleSessionAccessor(template);
        accessor.killSession(null);
    }

    @Test
    public void killQuery_sqlShouldCorrectWhenNormal() {
        OracleSessionAccessor accessor = new OracleSessionAccessor(template);
        accessor.killQuery(11212L);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());

        Assert.assertEquals("KILL QUERY 11212", sqlCaptor.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void killQuery_shouldThrowExceptionWhenInputNull() {
        OracleSessionAccessor accessor = new OracleSessionAccessor(template);
        accessor.killQuery(null);
    }

    @Test
    public void listSession_expect_ok() {
        when(template.getObVersion()).thenReturn("4.0.0.0");
        OracleSessionAccessor accessor = new OracleSessionAccessor(template);
        ObSession obSession = new ObSession();
        obSession.setHost("127.0.0.1:2881");
        obSession.setProxySessid(BigInteger.ONE);
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
}
