package com.oceanbase.ocp.obsdk.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;

public class ConnectPropertiesTest {

    private static ConnectProperties connectProperties;

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
    public void setUp() {
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

    @Test
    public void toString_shouldNotContainsPassword() {
        String str = connectProperties.toString();
        assertFalse(str.contains("password"));
    }

    @Test
    public void getDatabase_shouldReturnDefaultDatabaseWhenNotInputDatabase() {
        String target = connectProperties.getDatabase();
        assertEquals("oceanbase", target);
    }

    @Test
    public void getDatabase_shouldReturnSpecifiedDatabaseWhenInputDatabase() {
        String database = "db123";
        String target = connectProperties.withDatabase(database).getDatabase();
        assertEquals(database, target);
    }

    @Test
    public void getDatabase_shouldReturnUsernameWhenOracleMode() {
        String target = connectProperties.withCompatibilityMode(CompatibilityMode.ORACLE).getDatabase();
        assertEquals(connectProperties.getUsername(), target);
    }

    @Test
    public void getFullUsername_shouldReturnSimpleNameWhenConnectionModeIsPlain() {
        String username = "test_user@test_tenant";
        String tenantName = "test_tenant";
        String clusterName = "test_cluster";
        ConnectProperties conn = connectProperties
                .withConnectionMode(ConnectionMode.PLAIN)
                .withUsername(username)
                .withTenantName(tenantName)
                .withClusterName(clusterName)
                .withObClusterId(null);
        assertEquals(username, conn.getFullUsername());
    }

    @Test
    public void getFullUsername_shouldReturnBasicFullNameWhenConnectionModeIsDirect() {
        String username = "test_user";
        String tenantName = "test_tenant";
        String clusterName = "test_cluster";
        ConnectProperties conn = connectProperties
                .withConnectionMode(ConnectionMode.DIRECT)
                .withUsername(username)
                .withTenantName(tenantName)
                .withClusterName(clusterName);
        assertEquals(username + "@" + tenantName, conn.getFullUsername());
    }

    @Test
    public void getFullUsername_shouldReturnFullNameWithClusterNameWhenClusterIdIsNull() {
        String username = "test_user";
        String tenantName = "test_tenant";
        String clusterName = "test_cluster";
        ConnectProperties conn = connectProperties
                .withUsername(username)
                .withTenantName(tenantName)
                .withClusterName(clusterName)
                .withObClusterId(null);
        assertEquals(username + "@" + tenantName + "#" + clusterName, conn.getFullUsername());
    }

    @Test
    public void getFullUsername_shouldReturnFullNameWithClusterNameAndClusterId() {
        String username = "test_user";
        String tenantName = "test_tenant";
        String clusterName = "test_cluster";
        Long clusterId = 12312L;
        ConnectProperties conn = connectProperties
                .withUsername(username)
                .withTenantName(tenantName)
                .withClusterName(clusterName)
                .withObClusterId(clusterId);
        assertEquals(username + "@" + tenantName + "#" + clusterName + ":" + clusterId,
                conn.getFullUsername());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenConnectionModeIsNull() {
        connectProperties.withConnectionMode(null).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenCompatibilityModeIsNull() {
        connectProperties.withCompatibilityMode(null).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenProxyModeButAddressIsNull() {
        connectProperties.withAddress(null).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenProxyModeButPortIsNull() {
        connectProperties.withPort(null).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenPlainModeButAddressIsNull() {
        connectProperties.withConnectionMode(ConnectionMode.PLAIN).withAddress(null).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenPlainModeButPortIsNull() {
        connectProperties.withConnectionMode(ConnectionMode.PLAIN).withPort(null).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenDirectModeButObsAddrListAndAddressBothEmpty() {
        connectProperties.withConnectionMode(ConnectionMode.DIRECT)
                .withAddress(null)
                .withPort(null)
                .withObsAddrList(Collections.emptyList())
                .validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenUsernameIsNull() {
        connectProperties.withUsername(null).validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenUsernameIsBlank() {
        connectProperties.withUsername(" ").validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionWhenConnectionModeIsProxyButClusterNameIsEmpty() {
        connectProperties.withConnectionMode(ConnectionMode.PROXY).withClusterName("").validate();
    }

    @Test
    public void validate_shouldSuccessWhenConnectionModeIsProxy() {
        connectProperties.withConnectionMode(ConnectionMode.PROXY).validate();
    }

    @Test
    public void validate_shouldSuccessWhenConnectionModeIsPlain() {
        connectProperties.withConnectionMode(ConnectionMode.PLAIN).validate();
    }

    @Test
    public void validate_shouldSuccessWhenConnectionModeIsDirect() {
        List<ObServerAddr> obsAddrList = new ArrayList<>();
        obsAddrList.add(ObServerAddr.builder().address("1.1.1.1").port(1234).build());
        connectProperties.withConnectionMode(ConnectionMode.DIRECT).withObsAddrList(obsAddrList).validate();
    }

    @Test
    public void validate_shouldSuccessWhenConnectionModeIsDirectObsAddrListIsEmptyButAddressIsValid() {
        connectProperties.withConnectionMode(ConnectionMode.DIRECT)
                .withAddress("127.0.0.1")
                .withPort(2883)
                .withObsAddrList(Collections.emptyList())
                .validate();
    }

}
