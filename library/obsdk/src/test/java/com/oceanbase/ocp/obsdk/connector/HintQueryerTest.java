package com.oceanbase.ocp.obsdk.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.oceanbase.ocp.common.sql.Hint;
import com.oceanbase.ocp.common.sql.Hints;

public class HintQueryerTest {

    private ObConnectTemplate obConnectTemplate;

    private static final long TIMEOUT_MILLIS = 60000000L;
    private static final Hint TIMEOUT_HINT = Hints.timeout(TIMEOUT_MILLIS);
    private static final Hint WEAK_READ_HINT = Hints.weakRead();

    @Before
    public void setUp() throws Exception {
        obConnectTemplate = mock(ObConnectTemplate.class);
    }

    @Test
    public void query_noHint() {
        String sql = "SELECT USERNAME FROM DBA_USERS";

        HintQueryer hintQueryer = new HintQueryer(obConnectTemplate);
        hintQueryer.queryForObject(sql, String.class);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(obConnectTemplate).queryForObject(sqlCaptor.capture(), any(Class.class));
        String realSql = sqlCaptor.getValue();
        assertEquals(sql, realSql);
    }

    @Test
    public void query_weakRead_ob4() {
        String sql = "SELECT USERNAME FROM DBA_USERS";

        when(obConnectTemplate.getObVersion()).thenReturn("4.0.0.0");
        HintQueryer hintQueryer = new HintQueryer(obConnectTemplate);
        hintQueryer.queryForObject(sql, String.class);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(obConnectTemplate).queryForObject(sqlCaptor.capture(), any(Class.class));
        String realSql = sqlCaptor.getValue();
        assertEquals(sql, realSql);
    }

    @Test
    public void query_weakRead_ob3() {
        String sql = "SELECT USERNAME FROM DBA_USERS";

        when(obConnectTemplate.getObVersion()).thenReturn("3.2.3.0");
        HintQueryer hintQueryer = new HintQueryer(obConnectTemplate);
        hintQueryer.weakRead().queryForObject(sql, String.class);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(obConnectTemplate).queryForObject(sqlCaptor.capture(), any(Class.class));
        String realSql = sqlCaptor.getValue();
        assertThat(realSql, Matchers.containsString(WEAK_READ_HINT.text()));
    }

    @Test
    public void query_timeout() {
        String sql = "SELECT USERNAME FROM DBA_USERS";

        when(obConnectTemplate.getObVersion()).thenReturn("3.2.3.0");
        HintQueryer hintQueryer = new HintQueryer(obConnectTemplate);
        hintQueryer.timeout(TIMEOUT_MILLIS).queryForObject(sql, String.class);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(obConnectTemplate).queryForObject(sqlCaptor.capture(), any(Class.class));
        String realSql = sqlCaptor.getValue();
        assertThat(realSql, Matchers.containsString(TIMEOUT_HINT.text()));
    }

    @Test
    public void query_twoHints() {
        String sql = "SELECT USERNAME FROM DBA_USERS";

        when(obConnectTemplate.getObVersion()).thenReturn("3.2.3.0");
        HintQueryer hintQueryer = new HintQueryer(obConnectTemplate);
        hintQueryer.weakRead().timeout(TIMEOUT_MILLIS).queryForObject(sql, String.class);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(obConnectTemplate).queryForObject(sqlCaptor.capture(), any(Class.class));
        String realSql = sqlCaptor.getValue();
        assertThat(realSql, Matchers.containsString(WEAK_READ_HINT.text()));
        assertThat(realSql, Matchers.containsString(TIMEOUT_HINT.text()));
    }
}
