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
package com.oceanbase.ocp.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.oceanbase.ocp.monitor.model.metric.MetricDataRange;

interface IMetricDataRead {

    /**
     * Query raw data by specified time range.
     */
    default MetricDataRange range(Long seriesId, Long start, Long end, Long step) {
        Map<Long, MetricDataRange> seriesId2DataList = ranges(Collections.singletonList(seriesId), start, end, step);
        return seriesId2DataList.get(seriesId);
    }

    /**
     * Query raw data by specified time range from cache.
     */
    default Map<Long, MetricDataRange> rangesFromCache(List<Long> seriesIds, Long start, Long end, Long step) {
        return ranges(seriesIds, start, end, step);
    }

    /**
     * Query raw data by specified time range with multiple seriesId.
     */
    Map<Long, MetricDataRange> ranges(List<Long> seriesIds, Long start, Long end, Long step);

}
