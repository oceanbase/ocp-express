package com.oceanbase.ocp.obsdk.connector.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import com.oceanbase.ocp.obsdk.config.DataSourceConfig;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ObConnectorKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractConnector.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class DefaultConnectorTest {

    private static ConnectProperties connectProperties;

    @BeforeClass
    public static void beforeClass() {
        connectProperties = ConnectProperties.builder()
                .address("127.0.0.1")
                .port(2883)
                .username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("******")
                .database("oceanbase")
                .build();
    }

    @Before
    public void setUp() {
        connectProperties = ConnectProperties.builder()
                .address("127.0.0.1")
                .port(2883)
                .username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("******")
                .database("oceanbase")
                .build();
    }

    @Test
    public void shouldAliveWhenMysqlConnectorInited() throws Exception {
        DruidDataSource dataSource = mock(DruidDataSource.class);
        PowerMockito.whenNew(DruidDataSource.class).withAnyArguments().thenReturn(dataSource);
        when(dataSource.isClosed()).thenReturn(false);
        DruidPooledConnection connection = mock(DruidPooledConnection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        DefaultConnector defaultConnector = new DefaultConnector(connectProperties);
        assertEquals(ObConnectorKey.of(connectProperties), defaultConnector.key());
        defaultConnector.init(DataSourceConfig.builder().socketReadTimeout(1L).socketConnectTimeout(1L).build());
        assertFalse(defaultConnector.isAlive());
    }

    @Test
    public void shouldNotAliveWhenMysqlConnectorClosed() throws Exception {
        DruidDataSource dataSource = mock(DruidDataSource.class);
        PowerMockito.whenNew(DruidDataSource.class).withAnyArguments().thenReturn(dataSource);
        when(dataSource.isClosed()).thenReturn(false);

        DefaultConnector defaultConnector = new DefaultConnector(connectProperties);
        assertEquals(ObConnectorKey.of(connectProperties), defaultConnector.key());
        defaultConnector.init(DataSourceConfig.builder().socketReadTimeout(1L).socketConnectTimeout(1L).build());

        // invoke close
        defaultConnector.close();
        assertFalse(defaultConnector.isAlive());
    }

    @Test
    public void shouldSuccessWhenGetDataSource() throws Exception {
        DruidDataSource dataSource = mock(DruidDataSource.class);
        PowerMockito.whenNew(DruidDataSource.class).withAnyArguments().thenReturn(dataSource);
        when(dataSource.isClosed()).thenReturn(false);

        DefaultConnector defaultConnector = new DefaultConnector(connectProperties);
        assertEquals(ObConnectorKey.of(connectProperties), defaultConnector.key());
        defaultConnector.init(DataSourceConfig.builder().socketReadTimeout(1L).socketConnectTimeout(1L).build());
        DataSource ds = defaultConnector.dataSource();
        assertThat(ds, is(dataSource));
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailedWhenGetDataSource() throws Exception {
        DruidDataSource dataSource = mock(DruidDataSource.class);
        PowerMockito.whenNew(DruidDataSource.class).withAnyArguments().thenReturn(dataSource);
        Mockito.doThrow(new RuntimeException()).when(dataSource).init();
        Mockito.doThrow(new RuntimeException()).when(dataSource).close();
        DefaultConnector defaultConnector = new DefaultConnector(connectProperties);
        defaultConnector.init(DataSourceConfig.builder().socketReadTimeout(1L).socketConnectTimeout(1L).build());
    }
}
