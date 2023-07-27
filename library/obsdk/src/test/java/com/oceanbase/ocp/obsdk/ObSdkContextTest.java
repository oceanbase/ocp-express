package com.oceanbase.ocp.obsdk;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ConnectionMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObSdkContextTest {

    @Before
    public void setUp() throws Exception {
        ObSdkContext.setConnectStateExpireMillis(10_000L);
        ObSdkContext.setCredentialExpireMillis(10_000L);
    }

    @After
    public void tearDown() throws Exception {
        ObSdkContext.clear();
    }

    private ConnectProperties buildConnectProperties(String clusterName) {
        return ConnectProperties.builder()
                .connectionMode(ConnectionMode.DIRECT)
                .clusterName(clusterName)
                .address("1.2.3.4")
                .port(2881)
                .password("hello")
                .build();
    }

    @Test
    public void connectFailed_shouldReturnFalseWhenConnectFirstTime() {
        ConnectProperties key = buildConnectProperties("ob1");
        Assert.assertFalse(ObSdkContext.connectFailed(key));
    }

    @Test
    public void connectFailed_shouldReturnTrueWhenConnectFailedOnce() {
        ConnectProperties key = buildConnectProperties("ob1");
        ObSdkContext.setConnectState(key, false);
        Assert.assertTrue(ObSdkContext.connectFailed(key));
        ConnectProperties key2 = buildConnectProperties("ob2");
        Assert.assertFalse(ObSdkContext.connectFailed(key2));
    }

    @Test
    public void connectFailed_shouldReturnFalseWhenConnectSuccess() {
        ConnectProperties key = buildConnectProperties("ob1");
        ObSdkContext.setConnectState(key, true);
        Assert.assertFalse(ObSdkContext.connectFailed(key));
    }

    @Test
    public void connectFailed_shouldReturnFalseWhenConnectStateExpired() {
        ObSdkContext.setConnectStateExpireMillis(200L);
        ConnectProperties key = buildConnectProperties("ob1");
        ObSdkContext.setConnectState(key, false);
        Assert.assertTrue(ObSdkContext.connectFailed(key));
        waitFor(100);
        Assert.assertTrue(ObSdkContext.connectFailed(key));
        waitFor(200);
        Assert.assertFalse(ObSdkContext.connectFailed(key));
    }

    @Test
    public void connectFailed_shouldReturnFalseWhenContextCleared() {
        ConnectProperties key = buildConnectProperties("ob1");
        ObSdkContext.setConnectState(key, false);
        Assert.assertTrue(ObSdkContext.connectFailed(key));
        ObSdkContext.clear();
        Assert.assertFalse(ObSdkContext.connectFailed(key));
    }

    @Test
    public void getCredential_shouldReturnNullWhenGetFirstTime() {
        String key = "user1";
        Assert.assertNull(ObSdkContext.getCredential(key));
    }

    @Test
    public void getCredential_shouldReturnValueWhenGetAgain() {
        String key1 = "user1";
        String credential = "value";
        Assert.assertNull(ObSdkContext.getCredential(key1));
        ObSdkContext.putCredential(key1, credential);
        Assert.assertEquals(credential, ObSdkContext.getCredential(key1));

        String key2 = "user2";
        Assert.assertNull(ObSdkContext.getCredential(key2));
    }

    @Test
    public void getCredential_shouldReturnNullWhenValueExpired() {
        ObSdkContext.setCredentialExpireMillis(200L);
        String key = "user1";
        String credential = "value";
        Assert.assertNull(ObSdkContext.getCredential(key));
        ObSdkContext.putCredential(key, credential);
        Assert.assertEquals(credential, ObSdkContext.getCredential(key));
        waitFor(100);
        Assert.assertEquals(credential, ObSdkContext.getCredential(key));
        waitFor(200);
        Assert.assertNull(ObSdkContext.getCredential(key));
    }

    @Test
    public void getCredential_shouldReturnNullWhenContextCleared() {
        String key = "user1";
        String credential = "value";
        Assert.assertNull(ObSdkContext.getCredential(key));
        ObSdkContext.putCredential(key, credential);
        Assert.assertEquals(credential, ObSdkContext.getCredential(key));
        ObSdkContext.clear();
        Assert.assertNull(ObSdkContext.getCredential(key));
    }

    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException detected");
        }
    }
}
