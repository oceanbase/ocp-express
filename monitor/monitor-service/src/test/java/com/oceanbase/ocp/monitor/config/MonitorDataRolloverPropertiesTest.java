/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.oceanbase.ocp.monitor.config;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.oceanbase.ocp.monitor.constants.MonitorConstants;

public class MonitorDataRolloverPropertiesTest {

    private MonitorDataRolloverProperties defaultProperties;
    private MonitorDataRolloverProperties customProperties;

    @Before
    public void setup() {
        defaultProperties = new MonitorDataRolloverProperties(null);

        Map<String, Integer> confMap = new HashMap<>();
        confMap.put(MonitorConstants.TABLE_NAME_SECOND_DATA, 2);
        confMap.put(MonitorConstants.TABLE_NAME_MINUTE_DATA, 3);
        customProperties = new MonitorDataRolloverProperties(confMap);
    }


    @Test
    public void getSecondDataRetentionDays() {
        assertEquals(8, defaultProperties.getSecondDataRetentionDays());

        assertEquals(2, customProperties.getSecondDataRetentionDays());
    }

    @Test
    public void getMinuteDataRetentionDays() {
        assertEquals(31, defaultProperties.getMinuteDataRetentionDays());

        assertEquals(3, customProperties.getMinuteDataRetentionDays());
    }

}
