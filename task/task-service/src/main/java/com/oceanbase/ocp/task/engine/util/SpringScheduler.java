/*
 * Copyright (c) 2023 OceanBase
 * OCP Express is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.oceanbase.ocp.task.engine.util;

import java.util.Date;

import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringScheduler {

    private final CronTrigger trigger;
    private final SimpleTriggerContext triggerContext;

    private Date lastRunTime;
    private Date nextRunTime;

    public SpringScheduler(String scheduleRule, Date lastRunTime, Date nextRunTime) {
        this.trigger = new CronTrigger(scheduleRule);
        SimpleTriggerContext triggerContext = new SimpleTriggerContext();
        triggerContext.update(lastRunTime, lastRunTime, lastRunTime);
        this.triggerContext = triggerContext;
        this.lastRunTime = lastRunTime;
        this.nextRunTime = nextRunTime;
    }

    public boolean trySchedule() {
        if (this.nextRunTime == null) {
            this.nextRunTime = trigger.nextExecutionTime(triggerContext);
        }
        Date currentDate = new Date();
        log.debug("Try schedule, lastRun={}, nextRun={}, currentDate={}", lastRunTime, nextRunTime, currentDate);
        if (nextRunTime.before(currentDate)) {
            log.info("Schedule time arrived, lastRun={}, nextRun={}, currentDate={}",
                    lastRunTime, nextRunTime, currentDate);
            triggerContext.update(nextRunTime, currentDate, currentDate);
            lastRunTime = currentDate;
            nextRunTime = trigger.nextExecutionTime(triggerContext);
            return true;
        } else {
            return false;
        }
    }

    public Date lastRunTime() {
        return lastRunTime;
    }

    public Date nextRunTime() {
        return nextRunTime;
    }

}
