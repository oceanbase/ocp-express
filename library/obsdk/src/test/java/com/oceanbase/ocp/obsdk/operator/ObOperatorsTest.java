package com.oceanbase.ocp.obsdk.operator;

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
public class ObOperatorsTest {

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
    public void newObOperator_shouldThrowExceptionWhenInputNull() {
        ObOperators.newObOperator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newObOperator_shouldThrowExceptionWhenInputInvalidConnectProperties() {
        ObOperators.newObOperator(connectProperties.withUsername(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newObOperator_shouldThrowExceptionWhenOracleMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperators.newObOperator(connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE));
    }

    @Test
    public void newObOperator_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperator operator =
                ObOperators.newObOperator(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(operator);
        assertNotNull(operator.cluster());
        assertNotNull(operator.resource());
        assertNotNull(operator.session());
        assertNotNull(operator.stats());
        assertNotNull(operator.tenant());
    }

    @Test
    public void newMetaOperator_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperator operator =
                ObOperators.newMetaOperator(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(operator);
        assertNotNull(operator.cluster());
        assertNotNull(operator.parameter());
    }

    @Test(expected = NullPointerException.class)
    public void newResourceOperator_shouldThrowExceptionWhenInputNull() {
        ObOperators.newResourceOperator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newResourceOperator_shouldThrowExceptionWhenInputInvalidConnectProperties() {
        ObOperators.newResourceOperator(connectProperties.withUsername(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newResourceOperator_shouldThrowExceptionWhenOracleMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperators.newResourceOperator(connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE));
    }

    @Test
    public void newResourceOperator_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ResourceOperator operator =
                ObOperators.newResourceOperator(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(operator);
    }

    @Test(expected = NullPointerException.class)
    public void newClusterOperator_shouldThrowExceptionWhenInputNull() {
        ObOperators.newClusterOperator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newClusterOperator_shouldThrowExceptionWhenInputInvalidConnectProperties() {
        ObOperators.newClusterOperator(connectProperties.withAddress(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newClusterOperator_shouldThrowExceptionWhenOracleMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperators.newClusterOperator(connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE));
    }

    @Test
    public void newClusterOperator_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ClusterOperator operator =
                ObOperators.newClusterOperator(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(operator);
    }

    @Test(expected = NullPointerException.class)
    public void newTenantOperator_shouldThrowExceptionWhenInputNull() {
        ObOperators.newTenantOperator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newTenantOperator_shouldThrowExceptionWhenInputInvalidConnectProperties() {
        ObOperators.newTenantOperator(connectProperties.withTenantName(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newTenantOperator_shouldThrowExceptionWhenOracleMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperators.newTenantOperator(connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE));
    }

    @Test
    public void newTenantOperator_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        TenantOperator operator =
                ObOperators.newTenantOperator(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(operator);
    }

    @Test(expected = NullPointerException.class)
    public void newSessionOperator_shouldThrowExceptionWhenInputNull() {
        ObOperators.newSessionOperator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newSessionOperator_shouldThrowExceptionWhenInputInvalidConnectProperties() {
        ObOperators.newSessionOperator(connectProperties.withUsername(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newSessionOperator_shouldThrowExceptionWhenOracleMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperators.newSessionOperator(connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE));
    }

    @Test
    public void newSessionOperator_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        SessionOperator operator =
                ObOperators.newSessionOperator(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(operator);
    }

    @Test(expected = NullPointerException.class)
    public void newStatsOperator_shouldThrowExceptionWhenInputNull() {
        ObOperators.newStatsOperator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newStatsOperator_shouldThrowExceptionWhenInputInvalidConnectProperties() {
        ObOperators.newStatsOperator(connectProperties.withUsername(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newStatsOperator_shouldThrowExceptionWhenOracleMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        ObOperators.newStatsOperator(connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE));
    }

    @Test
    public void newStatsOperator_shouldSuccessWhenMysqlMode() throws Exception {
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        Method methodGetObVersion = PowerMockito.method(ObConnectTemplate.class, "getObVersion");
        PowerMockito.replace(methodGetObVersion).with((proxy, method, args) -> "2.4.0");

        StatsOperator operator =
                ObOperators.newStatsOperator(connectProperties.withCompatibilityMode(CompatibilityMode.MYSQL));
        assertNotNull(operator);
    }

}
