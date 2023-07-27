package com.oceanbase.ocp.obsdk.connector;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ObConnectors.class)
public class ConnectPropertiesImmutableTest {

    @Test
    public void withAddressAndPort_notModifyingOrigin() throws Exception {
        String address = "127.0.0.1";
        Integer port = 2881;
        ConnectProperties connectProperties = ConnectProperties.builder()
                .connectionMode(ConnectionMode.DIRECT)
                .compatibilityMode(CompatibilityMode.MYSQL)
                .obsAddrList(
                        Collections.singletonList(ObServerAddr.builder().address("127.0.0.1").port(3000).build()))
                .address(address)
                .port(port)
                .username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("******")
                .build();

        ObConnector obConnector = mock(ObConnector.class);
        when(obConnector.key()).thenReturn(ObConnectorKey.of(connectProperties));
        PowerMockito.spy(ObConnectors.class);
        PowerMockito.doReturn(obConnector).when(ObConnectors.class, "newObConnector", any());
        ObConnectors.getObConnector(connectProperties);

        assertEquals(address, connectProperties.getAddress());
        assertEquals(port, connectProperties.getPort());
    }


}
