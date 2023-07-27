package com.oceanbase.ocp.executor.internal.template;

import java.util.HashMap;

import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oceanbase.ocp.executor.config.Configuration;
import com.oceanbase.ocp.executor.internal.auth.Authentication;
import com.oceanbase.ocp.executor.internal.auth.http.HttpAuthentication;
import com.oceanbase.ocp.executor.internal.connector.ConnectProperties;
import com.oceanbase.ocp.executor.model.response.AgentResponse;

import jakarta.ws.rs.WebApplicationException;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*"})
public class HttpTemplateTest {

    private final ConnectProperties connectProperties = ConnectProperties.builder().authentication(
            Authentication.builder().httpAuth(HttpAuthentication.basic("usernmae", "password")).build())
            .hostAddress("127.0.0.1")
            .httpPort(8888)
            .build();

    @Test(expected = Exception.class)
    public void testGet() {
        HttpTemplate connectTemplate = new HttpTemplate(connectProperties, Configuration.builder().build());
        connectTemplate.get("/api/to/path", String.class);
    }

    @Test(expected = Exception.class)
    public void testPost() {
        HttpTemplate connectTemplate = new HttpTemplate(connectProperties, Configuration.builder().build());
        connectTemplate.post("/api/to/path", String.class, new HashMap<>());
    }

    @Test
    public void test_safeExecute() {
        AgentResponse<String> resp = HttpTemplate.safeExecute(() -> {
            AgentResponse<String> ret = new AgentResponse<>();
            ret.setData("test");
            return ret;
        });
        Assert.assertEquals("test", resp.getData());

        try {
            HttpTemplate.safeExecute(() -> {
                throw new WebApplicationException(OutboundJaxrsResponse.status(400).build());
            });
            Assert.fail("should throws");
        } catch (WebApplicationException e) {
            // ok
        }
    }
}
