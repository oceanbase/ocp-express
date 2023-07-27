package com.oceanbase.ocp.obsdk.operator.resource;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.connector.ObConnectors;
import com.oceanbase.ocp.obsdk.operator.resource.model.CreateResourcePoolInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.CreateUnitConfigInput;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObConnectors.class, ObConnectTemplate.class})
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class MysqlResourceOperatorTest {

    private static ObConnectTemplate template;
    private static MysqlResourceOperator operator;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);
        when(template.getObVersion()).thenReturn("2.2.1");
        operator = new MysqlResourceOperator(template);
    }

    @Test
    public void createUnitConfig_before400() {
        when(template.getObVersion()).thenReturn("3.1.2");
        operator = new MysqlResourceOperator(template);
        CreateUnitConfigInput input = buildCreateUnitConfigInput();
        operator.createUnitConfig(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), any());
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("CREATE RESOURCE UNIT"));
        assertThat(sql, containsString("MAX_MEMORY"));
    }

    @Test
    public void createUnitConfig_after400() {
        when(template.getObVersion()).thenReturn("4.0.0.0");
        operator = new MysqlResourceOperator(template);
        CreateUnitConfigInput input = buildCreateUnitConfigInput();
        operator.createUnitConfig(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).update(sqlCaptor.capture(), any());
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("CREATE RESOURCE UNIT"));
        assertThat(sql, containsString("MEMORY_SIZE"));
    }

    @Test
    public void createResourcePool() {
        CreateResourcePoolInput input = CreateResourcePoolInput.builder()
                .name("resource_pool_1")
                .unitConfigName("unit_config_1")
                .unitCount(1L)
                .zoneList(Collections.singletonList("zone1"))
                .build();
        when(template.getObVersion()).thenReturn("3.1.2");
        operator.createResourcePool(input);
        verify(template).queryForObject(anyString(), any(), any(RowMapper.class));
    }

    @Test
    public void createResourcePool_ob4() {
        CreateResourcePoolInput input = CreateResourcePoolInput.builder()
                .name("resource_pool_1")
                .unitConfigName("unit_config_1")
                .unitCount(1L)
                .zoneList(Collections.singletonList("zone1"))
                .build();
        when(template.getObVersion()).thenReturn("4.0.0.0");
        operator.createResourcePool(input);
        verify(template).queryForObject(anyString(), any(), any(RowMapper.class));
    }

    @Test
    public void listUnit_before400() {
        when(template.getObVersion()).thenReturn("3.1.2");
        operator.listAllUnits();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any(), any());
        assertThat(sqlCaptor.getValue(), containsString("__all_unit"));
    }

    @Test
    public void listUnit_after400() {
        when(template.getObVersion()).thenReturn("4.0.0.0");
        operator.listAllUnits();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).query(sqlCaptor.capture(), any(), any());
        assertThat(sqlCaptor.getValue(), containsString("DBA_OB_UNITS"));
    }

    private CreateUnitConfigInput buildCreateUnitConfigInput() {
        return CreateUnitConfigInput.builder()
                .name("config")
                .maxCpu(1.5)
                .minCpu(1.5)
                .maxMemoryByte(6 * 1024 * 1024 * 1024L)
                .minMemoryByte(6 * 1024 * 1024 * 1024L)
                .build();
    }
}
