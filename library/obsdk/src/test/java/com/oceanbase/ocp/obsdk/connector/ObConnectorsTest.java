package com.oceanbase.ocp.obsdk.connector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oceanbase.ocp.obsdk.connector.impl.DefaultConnector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest(ObConnectors.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class ObConnectorsTest {

    private static ConnectProperties connectProperties;

    @BeforeClass
    public static void beforeClass() {
        connectProperties = ConnectProperties.builder()
                .connectionMode(ConnectionMode.PROXY)
                .address("127.0.0.1")
                .port(2883).username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("*******")
                .database("oceanbase")
                .build();
    }

    @Before
    public void setUp() {
        connectProperties = ConnectProperties.builder()
                .connectionMode(ConnectionMode.PROXY)
                .address("127.0.0.1")
                .port(2883).username("root")
                .tenantName("sys")
                .clusterName("test_cluster")
                .obClusterId(12312L)
                .password("*******")
                .database("oceanbase")
                .build();
    }

    @After
    public void tearDown() throws Exception {
        ObConnectors.clearAll();
    }

    @Test(expected = NullPointerException.class)
    public void getObConnector_shouldThrowNullPointerExceptionWhenInputNull() {
        ObConnectors.getObConnector(null);
    }

    @Test
    public void getObConnector_shouldSuccessWhenGetObConnectorByCorrectParam() throws Exception {
        DefaultConnector defaultConnector = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);
        when(defaultConnector.key()).thenReturn(ObConnectorKey.of(connectProperties));

        ObConnector actualObConnector = ObConnectors.getObConnector(connectProperties);
        assertThat(actualObConnector, is(defaultConnector));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getObConnector_shouldThrowExceptionWhenGetObConnectorByNullAddress() throws Exception {
        DefaultConnector defaultConnector = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);

        ObConnector actualObConnector = ObConnectors.getObConnector(connectProperties.withAddress(" "));
        assertThat(actualObConnector, is(defaultConnector));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getObConnector_shouldThrowExceptionWhenGetObConnectorByNullPort() throws Exception {
        DefaultConnector defaultConnector = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);

        ObConnector actualObConnector = ObConnectors.getObConnector(connectProperties.withPort(null));
        assertThat(actualObConnector, is(defaultConnector));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getObConnector_shouldThrowExceptionWhenGetObConnectorByNullUsername() throws Exception {
        DefaultConnector defaultConnector = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(defaultConnector);

        ObConnector actualObConnector = ObConnectors.getObConnector(connectProperties.withUsername(" "));
        assertThat(actualObConnector, is(defaultConnector));
    }

    @Test
    public void getObConnector_shouldReturnSameConnectorWhenInputConnectPropertiesIsSame() throws Exception {
        DefaultConnector mockConnector1 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector1);
        when(mockConnector1.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector1.isAlive()).thenReturn(true);
        ObConnector c1 = ObConnectors.getObConnector(connectProperties);
        assertThat(c1, is(mockConnector1));

        DefaultConnector mockConnector2 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector2);
        when(mockConnector2.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector2.isAlive()).thenReturn(true);
        ObConnector c2 = ObConnectors.getObConnector(connectProperties);

        assertThat(c2, is(mockConnector1));

        DefaultConnector mockConnector3 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector3);
        when(mockConnector3.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector3.isAlive()).thenReturn(true);
        ObConnector c3 = ObConnectors.getObConnector(connectProperties);

        assertThat(c3, is(mockConnector1));
    }

    @Test
    public void getObConnector_shouldReturnNewConnectorWhenConnectPropertiesIsDifferent() throws Exception {
        ConnectProperties connectProperties1 = connectProperties.withUsername("user1");
        DefaultConnector mockConnector1 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector1);
        when(mockConnector1.key()).thenReturn(ObConnectorKey.of(connectProperties1));
        when(mockConnector1.isAlive()).thenReturn(true);
        ObConnector c1 = ObConnectors.getObConnector(connectProperties1);
        assertThat(c1, is(mockConnector1));

        ConnectProperties connectProperties2 = connectProperties.withUsername("user2");
        DefaultConnector mockConnector2 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector2);
        when(mockConnector2.key()).thenReturn(ObConnectorKey.of(connectProperties2));
        when(mockConnector2.isAlive()).thenReturn(true);
        ObConnector c2 = ObConnectors.getObConnector(connectProperties2);

        assertThat(c2, is(mockConnector2));
    }

    @Test
    public void cleanUp_shouldReturnFormerConnectorAfterCleanUpSinceNotExpired() throws Exception {
        DefaultConnector mockConnector1 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector1);
        when(mockConnector1.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector1.isAlive()).thenReturn(true);
        ObConnector c1 = ObConnectors.getObConnector(connectProperties);
        assertThat(c1, is(mockConnector1));

        ObConnectors.cleanUp();

        DefaultConnector mockConnector2 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector2);
        when(mockConnector2.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector2.isAlive()).thenReturn(true);
        ObConnector c2 = ObConnectors.getObConnector(connectProperties);

        assertThat(c2, is(mockConnector1));
    }

    @Test
    public void clearAll_shouldReturnNewConnectorAfterClearAllSinceOldBeenEvicted() throws Exception {
        DefaultConnector mockConnector1 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector1);
        when(mockConnector1.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector1.isAlive()).thenReturn(true);
        ObConnector c1 = ObConnectors.getObConnector(connectProperties);
        assertThat(c1, is(mockConnector1));

        ObConnectors.clearAll();

        DefaultConnector mockConnector2 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector2);
        when(mockConnector2.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector2.isAlive()).thenReturn(true);
        ObConnector c2 = ObConnectors.getObConnector(connectProperties);

        assertThat(c2, is(mockConnector2));
    }

    @Test
    public void invalidate_shouldReturnNewConnectorAfterInvalidateSpecifiedCluster() throws Exception {
        DefaultConnector mockConnector1 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector1);
        when(mockConnector1.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector1.isAlive()).thenReturn(true);
        ObConnector c1 = ObConnectors.getObConnector(connectProperties);
        assertThat(c1, is(mockConnector1));

        ObConnectors.invalidate(connectProperties.getClusterName());

        DefaultConnector mockConnector2 = mock(DefaultConnector.class);
        PowerMockito.whenNew(DefaultConnector.class).withAnyArguments().thenReturn(mockConnector2);
        when(mockConnector2.key()).thenReturn(ObConnectorKey.of(connectProperties));
        when(mockConnector2.isAlive()).thenReturn(true);
        ObConnector c2 = ObConnectors.getObConnector(connectProperties);

        assertThat(c2, is(mockConnector2));
    }
}
