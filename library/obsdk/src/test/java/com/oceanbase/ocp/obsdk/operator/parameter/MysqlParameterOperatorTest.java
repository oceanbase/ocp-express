package com.oceanbase.ocp.obsdk.operator.parameter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.connector.ObConnectors;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObConnectors.class, ObConnectTemplate.class})
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class MysqlParameterOperatorTest {

    private static final String CLUSTER_PARAMETER_NAME = "restore_concurrency";
    private static final String TENANT_PARAMETER_NAME = "audit_trail";

    private ObConnectTemplate template;
    private MysqlParameterOperator operator;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
        when(template.getObVersion()).thenReturn("2.2.77");
        operator = new MysqlParameterOperator(template);
    }

    @Test
    public void getHiddenClusterParameter_after400() {
        when(template.getObVersion()).thenReturn("4.0.0.0");
        operator.getHiddenClusterParameter(CLUSTER_PARAMETER_NAME);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any(), any());
        MatcherAssert.assertThat(sqlCaptor.getValue(), CoreMatchers.containsString("GV$OB_PARAMETERS"));
    }

    @Test
    public void listTenantParameters_after400() {
        when(template.getObVersion()).thenReturn("4.0.0.0");
        operator.listTenantParameters();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template, times(2)).query(sqlCaptor.capture(), any());
        MatcherAssert.assertThat(sqlCaptor.getAllValues().get(1), CoreMatchers.containsString("GV$OB_PARAMETERS"));
    }
}
