package com.oceanbase.ocp.obsdk.connector.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheTimerTest {

    @Test
    public void shouldSuccessWhenConfigureTasks() {
        ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        CacheTimer cacheTimer = new CacheTimer();
        cacheTimer.configureTasks(taskRegistrar);
        assertEquals(1, taskRegistrar.getTriggerTaskList().size());

        Trigger trigger = taskRegistrar.getTriggerTaskList().get(0).getTrigger();
        Date now = new Date();
        Date next = getNextDate(trigger, now);
        assertTrue(next.after(now));
        long period = (next.getTime() - now.getTime()) / 1000;
        assertTrue(period >= 30 && period <= 1800);
    }

    private Date getNextDate(Trigger trigger, Date now) {
        TriggerContext triggerContext = mock(TriggerContext.class);
        when(triggerContext.lastScheduledExecutionTime()).thenReturn(now);
        when(triggerContext.lastCompletionTime()).thenReturn(now);
        return trigger.nextExecutionTime(triggerContext);
    }
}
