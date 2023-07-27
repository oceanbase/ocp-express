package com.oceanbase.ocp.obsdk.accessor.object;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

@RunWith(MockitoJUnitRunner.class)
public class OracleObjectAccessorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
    }

    @Test
    public void listDatabases() {
        OracleObjectAccessor accessor = new OracleObjectAccessor(template);

        accessor.listDatabases();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any(RowMapper.class));
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("DBA_OBJECTS"));
    }

}
