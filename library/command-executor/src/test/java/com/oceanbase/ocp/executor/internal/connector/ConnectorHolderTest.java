package com.oceanbase.ocp.executor.internal.connector;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.oceanbase.ocp.executor.internal.connector.impl.DefaultAgentConnector;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorHolderTest {

    @Test
    public void test_put_success() {
        ConnectorHolder connectorHolder = new ConnectorHolder(1, 1);
        DefaultAgentConnector sshConnector = Mockito.mock(DefaultAgentConnector.class);
        Mockito.when(sshConnector.cacheKey()).thenReturn(ConnectorKey.http(ConnectProperties.builder().build()));
        Mockito.when(sshConnector.isAlive()).thenReturn(true);
        connectorHolder.put(sshConnector);
        Connector<?> connector = connectorHolder.get(ConnectorKey.http(ConnectProperties.builder().build()));
        Assert.assertNotNull(connector);
    }

    @Test
    public void test_get_success() {
        ConnectorHolder connectorHolder = new ConnectorHolder(1, 1);
        DefaultAgentConnector sshConnector = Mockito.mock(DefaultAgentConnector.class);
        Mockito.when(sshConnector.cacheKey()).thenReturn(ConnectorKey
                .http(ConnectProperties.builder().build()));
        connectorHolder.put(sshConnector);
        Connector<?> connector = connectorHolder.get(ConnectorKey.http(ConnectProperties.builder().build()));
        Assert.assertNull(connector);
    }
}
