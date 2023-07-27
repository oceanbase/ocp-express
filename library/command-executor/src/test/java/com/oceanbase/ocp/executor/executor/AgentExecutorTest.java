package com.oceanbase.ocp.executor.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oceanbase.ocp.executor.config.Configuration;
import com.oceanbase.ocp.executor.internal.connector.ConnectProperties;
import com.oceanbase.ocp.executor.internal.template.HttpTemplate;
import com.oceanbase.ocp.executor.internal.util.PingUtils;
import com.oceanbase.ocp.executor.model.response.AgentResponse;

import jakarta.ws.rs.core.Response.Status;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PingUtils.class, AgentExecutor.class})
public class AgentExecutorTest {

    @Test
    public void getClockDiffMillis_success() throws Exception {
        PowerMockito.mockStatic(PingUtils.class);
        HttpTemplate template = mock(HttpTemplate.class);
        PowerMockito.whenNew(HttpTemplate.class).withAnyArguments().thenReturn(template);
        when(PingUtils.getAvgRttByHttp(any(AgentExecutor.class), anyInt(), anyInt())).thenReturn(Optional.of(1L));
        AgentResponse<OffsetDateTime> response = new AgentResponse<>();
        response.setStatus(Status.ACCEPTED);
        response.setData(OffsetDateTime.now());
        when(template.get(anyString(), any(Class.class))).thenReturn(response);
        AgentExecutor executor = new AgentExecutor(new ConnectProperties(), Configuration.builder().build());
        Optional<Long> clockDiffMillis = executor.getClockDiffMillis();
        Assert.assertTrue(clockDiffMillis.isPresent());
    }

    @Test
    public void getClockDiffMillis_not_present() throws Exception {
        PowerMockito.mockStatic(PingUtils.class);
        HttpTemplate template = mock(HttpTemplate.class);
        PowerMockito.whenNew(HttpTemplate.class).withAnyArguments().thenReturn(template);
        when(PingUtils.getAvgRttByHttp(any(AgentExecutor.class), anyInt(), anyInt())).thenReturn(Optional.empty());
        AgentResponse<OffsetDateTime> response = new AgentResponse<>();
        response.setStatus(Status.ACCEPTED);
        response.setData(OffsetDateTime.now());
        when(template.get(anyString(), any(Class.class))).thenReturn(response);
        AgentExecutor executor = new AgentExecutor(new ConnectProperties(), Configuration.builder().build());
        Optional<Long> clockDiffMillis = executor.getClockDiffMillis();
        Assert.assertFalse(clockDiffMillis.isPresent());
    }
}
