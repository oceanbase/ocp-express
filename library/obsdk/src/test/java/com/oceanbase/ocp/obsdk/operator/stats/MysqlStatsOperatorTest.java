package com.oceanbase.ocp.obsdk.operator.stats;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

@RunWith(MockitoJUnitRunner.class)
public class MysqlStatsOperatorTest {

    @Mock
    private ObConnectTemplate connectTemplate;

    private MysqlStatsOperator operator;

    @Before
    public void setUp() throws Exception {
        operator = new MysqlStatsOperator(connectTemplate);
    }

    @Test
    public void allServerStats_after400() {
        operator = new MysqlStatsOperator(connectTemplate);
        operator.allServerStats();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(connectTemplate).query(sqlCaptor.capture(), any());
        assertThat(sqlCaptor.getValue(), containsString("GV$OB_SERVERS"));
    }
}
