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

package com.oceanbase.ocp.monitor.store;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.monitor.IIntervalMetricDataStore;
import com.oceanbase.ocp.monitor.IMetricDataStore;
import com.oceanbase.ocp.monitor.model.metric.MetricDataRange;

@Component
public class MetricDataStoreFactory implements IMetricDataStore {

    private static final long SECONDS_IN_MINUTE = 60L;

    @Autowired
    @Qualifier("secondStore")
    private IIntervalMetricDataStore secondStore;

    @Autowired
    @Qualifier("minuteStore")
    private IIntervalMetricDataStore minuteStore;

    @Override
    public Map<Long, MetricDataRange> ranges(List<Long> seriesIds, Long start, Long end, Long step) {
        Validate.notNull(step);
        return getDataStore(step).ranges(seriesIds, start, end, step);
    }

    @Override
    public Map<Long, MetricDataRange> rangesFromCache(List<Long> seriesIds, Long start, Long end, Long step) {
        return getDataStore(step).rangesFromCache(seriesIds, start, end, step);
    }

    private IIntervalMetricDataStore getDataStore(long step) {
        IIntervalMetricDataStore dataStore;
        if (step < SECONDS_IN_MINUTE) {
            dataStore = secondStore;
        } else {
            dataStore = minuteStore;
        }
        return dataStore;
    }

}
