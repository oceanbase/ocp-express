package com.oceanbase.ocp.obsdk.connector;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObConnectors.class, ObConnectTemplate.class})
public class ObConnectTemplateTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ObConnector obConnector;

    @Mock
    private DataSource dataSource;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private Connection connection;

    private ConnectProperties connectProperties;

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ObConnectors.class);
        when(ObConnectors.getObConnector(any())).thenReturn(obConnector);
        when(obConnector.dataSource()).thenReturn(dataSource);
        PowerMockito.whenNew(JdbcTemplate.class).withAnyArguments().thenReturn(jdbcTemplate);
        PowerMockito.whenNew(NamedParameterJdbcTemplate.class).withAnyArguments()
                .thenReturn(namedParameterJdbcTemplate);
        PowerMockito.when(namedParameterJdbcTemplate.getJdbcTemplate()).thenReturn(jdbcTemplate);
        PowerMockito.when(dataSource.getConnection()).thenReturn(connection);
        connectProperties = ConnectProperties.builder()
                .address("127.0.0.1")
                .port(2881)
                .username("root")
                .tenantName("sys")
                .password("******")
                .clusterName("obCluster")
                .build();
    }

    @Test
    public void getNormalObVersion_okay() {
        PowerMockito.when(jdbcTemplate.queryForObject(eq(
                "select max(value) value from oceanbase.__all_virtual_sys_parameter_stat where name = 'min_observer_version'"),
                eq(String.class))).thenReturn("3.1.1");
        ObConnectTemplate template = new ObConnectTemplate(connectProperties);
        String obVersion = template.getCurrentObVersion();
        assertEquals("3.1.1", obVersion);
    }

    @Test
    public void getCeObVersion_okay() {
        PowerMockito.when(jdbcTemplate.queryForObject(eq(
                "select max(value) value from oceanbase.__all_virtual_sys_parameter_stat where name = 'min_observer_version'"),
                eq(String.class))).thenReturn("3.1.1-OceanBase CE");
        ObConnectTemplate template = new ObConnectTemplate(connectProperties);
        String obVersion = template.getCurrentObVersion();
        assertEquals("3.1.1", obVersion);
    }

    @Test
    public void getObVersionFromCommonTenant_okay() {
        PowerMockito.when(jdbcTemplate.queryForObject(eq("SHOW VARIABLES LIKE 'version_comment'"),
                ArgumentMatchers.<RowMapper<String>>any())).thenReturn(
                        "OceanBase 2.2.77 (r20210712224820-a153ceb17c858db62c8f35fa4acf61ec7b0a4877) (Built Jul 12 2021 23:21:12)");
        ConnectProperties properties = connectProperties.withTenantName("tenant1");
        ObConnectTemplate template = new ObConnectTemplate(properties);
        String obVersion = template.getObVersion();
        assertEquals("2.2.77", obVersion);
    }
}
