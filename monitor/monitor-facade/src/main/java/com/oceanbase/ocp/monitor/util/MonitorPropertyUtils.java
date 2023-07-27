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
package com.oceanbase.ocp.monitor.util;

import java.util.Arrays;
import java.util.List;

import com.oceanbase.ocp.core.property.PropertyManager;
import com.oceanbase.ocp.core.util.BeanUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorPropertyUtils {

    private static final List<Integer> COLLECT_INTERVALS = Arrays.asList(1, 5, 10, 15, 60);

    private static final int MIN_INTERVAL = COLLECT_INTERVALS.get(0);
    private static final int MAX_INTERVAL = COLLECT_INTERVALS.get(COLLECT_INTERVALS.size() - 1);

    public static int getStandardSecondCollectInterval() {
        PropertyManager propertyManager = BeanUtils.getBean(PropertyManager.class);
        int interval = propertyManager.getMetricCollectSecondInterval();
        return getClosestInterval(interval);
    }

    public static long getAdaptedIntervalOrStep(long defaultValue) {
        if (defaultValue >= MAX_INTERVAL) {
            return defaultValue;
        }

        int collectInterval = getStandardSecondCollectInterval();
        if (defaultValue < MIN_INTERVAL) {
            return collectInterval;
        }

        return defaultValue < collectInterval ? collectInterval : defaultValue / collectInterval * collectInterval;
    }

    private static int getClosestInterval(int val) {
        if (val <= MIN_INTERVAL) {
            return MIN_INTERVAL;
        }
        for (int i = 1; i < COLLECT_INTERVALS.size(); i++) {
            if (val < COLLECT_INTERVALS.get(i)) {
                return COLLECT_INTERVALS.get(i - 1);
            }
        }
        return MAX_INTERVAL;
    }

}
