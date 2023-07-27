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

package com.oceanbase.ocp.monitor.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.monitor.OcpCacheStatService;
import com.oceanbase.ocp.monitor.storage.MetricDataWriteQueue;
import com.oceanbase.ocp.monitor.store.IntervalMetricDataCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OcpCacheStatServiceImpl implements OcpCacheStatService {

    @Autowired
    private SeriesIdKeyService seriesIdKeyService;

    @Autowired
    private IntervalMetricDataCache secondWriteCache;

    @Autowired
    private IntervalMetricDataCache minuteWriteCache;

    @Autowired
    private MetricDataWriteQueue secondWriteQueue;

    @Autowired
    private MetricDataWriteQueue minuteWriteQueue;

    @Override
    public Map<String, Number> printCacheStat() {
        Map<String, Number> cacheStatus = new HashMap<>();
        cacheStatus.put("seriesIdKeySize", seriesIdKeyService.cacheSize());
        cacheStatus.put("secondReadCacheSize", secondWriteCache.rCacheSize());
        cacheStatus.put("secondWriteCacheSize", secondWriteCache.wCacheSize());
        cacheStatus.put("secondWriteQueue", secondWriteQueue.size());
        cacheStatus.put("minuteReadCacheSize", minuteWriteCache.rCacheSize());
        cacheStatus.put("minuteWriteCacheSize", minuteWriteCache.wCacheSize());
        cacheStatus.put("minuteWriteQueue", minuteWriteQueue.size());

        log.info("OcpCacheMonitor, cacheInfo={}", JsonUtils.toJsonString(cacheStatus));
        log.info("MetricSearchStats:{}", seriesIdKeyService.getMetricSearchStats());
        return cacheStatus;
    }

}
