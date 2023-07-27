package com.oceanbase.ocp.obsdk.accessor;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.alibaba.druid.pool.DruidDataSource;

import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ConnectionMode;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.connector.ObConnectorKey;
import com.oceanbase.ocp.obsdk.connector.ObConnectors;
import com.oceanbase.ocp.obsdk.connector.impl.DefaultConnector;
import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest({ObConnectors.class, ObConnectTemplate.class})
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class ObAccessorsTest {

    private static ConnectProperties connectProperties;
    private DefaultConnector defaultConnector = mock(DefaultConnector.class);

    @BeforeClass
    public static void beforeClass() {
        connectProperties = ConnectProperties.builder()
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
    }

    @Before
    public void setUp() throws Exception {
        connectProperties = ConnectProperties.builder()
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

        when(defaultConnector.dataSource()).thenReturn(new DruidDataSource());
        when(defaultConnector.key()).thenReturn(ObConnectorKey.of(connectProperties));
    }

    @After
    public void tearDown() throws Exception {
        ObConnectors.clearAll();
    }

    @Test(expected = NullPointerException.class)
    public void newObAccessor_shouldThrowNullPointerExceptionWhenInputNull() {
        ObAccessors.newObAccessor(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newObAccessor_shouldThrowIllegalArgumentExceptionWhenInputInvalid() {
        ObAccessors.newObAccessor(connectProperties.withUsername(""));
    }

    @Test
    public void newObAccessor_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObAccessor accessor =
                ObAccessors.newObAccessor(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(accessor);
        assertNotNull(accessor.session());
        assertNotNull(accessor.variable());
        assertNotNull(accessor.user());
        assertNotNull(accessor.database());
    }

    @Test
    public void newObAccessor_shouldSuccessWhenOracleMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObAccessor accessor =
                ObAccessors.newObAccessor(connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE));
        assertNotNull(accessor);
        assertNotNull(accessor.session());
        assertNotNull(accessor.variable());
        assertNotNull(accessor.user());
        assertNotNull(accessor.database());
    }
}
