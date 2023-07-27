package com.oceanbase.ocp.obsdk.connector;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;

import lombok.SneakyThrows;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ObConnectors.class, ConnectTemplate.class})
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class ConnectTemplateTest {

    @Test(expected = Exception.class)
    @SneakyThrows
    public void executeWithSchema_on_mysql_mode_expect_exception() {
        PowerMockito.mockStatic(ObConnectors.class);
        ObConnector connector = mock(ObConnector.class);
        PowerMockito.when(ObConnectors.getObConnector(any())).thenReturn(connector);

        ConnectProperties connectProperties = ConnectProperties.builder()
                .connectionMode(ConnectionMode.PROXY)
                .compatibilityMode(CompatibilityMode.MYSQL)
                .address("127.0.0.1")
                .port(2883)
                .username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("******")
                .build();
        new ConnectTemplate(connectProperties).executeWithSchema("test", "select 1 from dual");
    }

    @Test
    @SneakyThrows
    public void executeWithSchema_on_oracle_mode_use_sys_user_expect_ok() {
        PowerMockito.mockStatic(ObConnectors.class);
        ObConnector connector = mock(ObConnector.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connector.dataSource()).thenReturn(dataSource);
        PowerMockito.when(ObConnectors.getObConnector(any())).thenReturn(connector);

        ConnectProperties connectProperties = ConnectProperties.builder()
                .connectionMode(ConnectionMode.PROXY)
                .compatibilityMode(CompatibilityMode.ORACLE)
                .address("127.0.0.1")
                .port(2883)
                .username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("******")
                .build();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        PowerMockito.whenNew(JdbcTemplate.class).withArguments(any()).thenReturn(jdbcTemplate);
        new ConnectTemplate(connectProperties).executeWithSchema("SYS", "select 1 from dual");
        verify(connection, atLeastOnce()).prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = SYS");
        verify(jdbcTemplate).execute("select 1 from dual");
    }

    @Test
    @SneakyThrows
    public void executeWithSchema_on_oracle_mode_use_normal_user_expect_ok() {
        PowerMockito.mockStatic(ObConnectors.class);
        ObConnector connector = mock(ObConnector.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connector.dataSource()).thenReturn(dataSource);
        PowerMockito.when(ObConnectors.getObConnector(any())).thenReturn(connector);

        ConnectProperties connectProperties = ConnectProperties.builder()
                .connectionMode(ConnectionMode.PROXY)
                .compatibilityMode(CompatibilityMode.ORACLE)
                .address("127.0.0.1")
                .port(2883)
                .username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("******")
                .build();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        PowerMockito.whenNew(JdbcTemplate.class).withArguments(any()).thenReturn(jdbcTemplate);
        new ConnectTemplate(connectProperties).executeWithSchema("ANHE", "select 1 from dual");
        verify(connection).prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = ANHE");
        verify(connection).prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = SYS");
        verify(jdbcTemplate).execute("select 1 from dual");
    }

    @Test(expected = OceanBaseException.class)
    @SneakyThrows
    public void executeWithSchema_customizeException() {
        PowerMockito.mockStatic(ObConnectors.class);
        ObConnector connector = mock(ObConnector.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connector.dataSource()).thenReturn(dataSource);
        when(ps.execute()).thenThrow(new SQLException());
        PowerMockito.when(ObConnectors.getObConnector(any())).thenReturn(connector);

        ConnectProperties connectProperties = ConnectProperties.builder()
                .connectionMode(ConnectionMode.PROXY)
                .compatibilityMode(CompatibilityMode.ORACLE)
                .address("127.0.0.1")
                .port(2883)
                .username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("******")
                .build();
        new ConnectTemplate(connectProperties).executeWithSchema("ANHE", "select 1 from dual");
    }
}
