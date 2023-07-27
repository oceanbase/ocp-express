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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.monitor.IIntervalMetricDataStore;
import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.monitor.meter.CounterGroup;
import com.oceanbase.ocp.monitor.model.metric.MetricData;
import com.oceanbase.ocp.monitor.model.metric.MetricDataRange;
import com.oceanbase.ocp.monitor.model.metric.MetricLine;
import com.oceanbase.ocp.monitor.model.storage.ValueNode;
import com.oceanbase.ocp.monitor.service.SeriesIdKeyService;
import com.oceanbase.ocp.monitor.storage.IRollupMetricDataDao;
import com.oceanbase.ocp.monitor.util.TimestampUtils;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RollupMetricDataStore implements IIntervalMetricDataStore {

    private final IntervalMetricDataCache writeCache;
    private final IRollupMetricDataDao persistent;
    private final SeriesIdKeyService seriesIdKeyService;

    private final CounterGroup metricDataCounter;

    public RollupMetricDataStore(IntervalMetricDataCache writeCache,
            IRollupMetricDataDao persistent, SeriesIdKeyService seriesIdKeyService, MeterRegistry meterRegistry) {
        this.writeCache = writeCache;
        this.persistent = persistent;
        this.seriesIdKeyService = seriesIdKeyService;
        this.metricDataCounter = CounterGroup.builder("ocp_monitor_store_metric_lines")
                .description("OCP metric lines wrote to cache")
                .labelNames("type")
                .build(meterRegistry);
    }

    @Override
    public int store(LinkedList<MetricLine> lines) {
        if (CollectionUtils.isEmpty(lines)) {
            return 0;
        }
        // use queue, remove object from gc-root asap
        LinkedList<MetricData> dataList = new LinkedList<>();
        MetricLine e;
        while ((e = lines.poll()) != null) {
            dataList.add(new MetricData(seriesIdKeyService.getSeriesId(e.getMetric()),
                    e.getTimestamp(), e.getValue()));
        }
        metricDataCounter.increment("second", dataList.size());
        return writeCache.writes(dataList);
    }

    @Override
    public MetricDataRange range(Long seriesId, Long start, Long end, Long step) {
        Map<Long, MetricDataRange> seriesId2DataList = ranges(Collections.singletonList(seriesId), start, end, step);
        return seriesId2DataList.get(seriesId);
    }

    @Override
    public Map<Long, MetricDataRange> rangesFromCache(List<Long> seriesIds, Long start, Long end, Long step) {
        return writeCache.ranges(seriesIds, start, end, step);
    }

    @Override
    public Map<Long, MetricDataRange> ranges(List<Long> seriesIds, Long start, Long end, Long step) {
        return ranges(seriesIds, start, end, step, TimestampUtils.currentTimeSecond());
    }

    Map<Long, MetricDataRange> ranges(List<Long> seriesIds, Long start, Long end, Long step, Long nowSecond) {
        QueryContext queryContext = createQueryContext(start, end, nowSecond, step);

        Map<Long, MetricDataRange> rangesFromWriteCache = queryContext.needQueryWriteCache
                ? writeCache.ranges(seriesIds, queryContext.wCacheStart, queryContext.wCacheEnd, step)
                : emptySeriesId2RangeMap(seriesIds);

        queryContext.putWriteCacheResult(rangesFromWriteCache);
        log.debug("Query context={}", queryContext);
        if (queryContext.needQueryDb) {
            long dbStart = queryContext.dbStart;
            long dbEnd = queryContext.dbEnd;

            Map<Long, List<ValueNode>> nodeFromDb = fetchNodeFromDb(seriesIds, dbStart, dbEnd);
            Map<Long, MetricDataRange> rangesFromDb = new HashMap<>();
            for (Long seriesId : nodeFromDb.keySet()) {
                MetricDataRange range = new MetricDataRange();
                range.setSeriesId(seriesId);
                range.setDataList(mapToMetricData(seriesId, nodeFromDb.get(seriesId), start, end, step));
                rangesFromDb.put(seriesId, range);
            }
            queryContext.putDbResult(rangesFromDb);
        }

        if (log.isDebugEnabled()) {
            log.debug("store-ranges, queryContext:{}", queryContext);
        }
        return queryContext.getMergedResult();
    }

    private List<MetricData> mapToMetricData(Long seriesId, List<ValueNode> valueNodes, Long start, Long end,
            Long step) {
        List<MetricData> dataList = new ArrayList<>();
        long nodeIntervalSeconds = writeCache.getNodeIntervalSeconds();
        Map<Long, ValueNode> mappedValueNodes =
                valueNodes.stream().collect(Collectors.toMap(ValueNode::getEpochSecondStart, o -> o, (o1, o2) -> o1));

        long currentPos = calcFirstPos(start, step);
        while (currentPos <= end) {
            long nodeStart = TimestampUtils.calcStartByInterval(currentPos, nodeIntervalSeconds);
            ValueNode node = mappedValueNodes.get(nodeStart);
            if (node == null || node.isEmptyNode()) {
                currentPos = calcFirstPosInNextNode(currentPos, nodeIntervalSeconds, step);
                continue;
            }
            long nodeEnd = nodeStart + nodeIntervalSeconds - 1;
            long currentEnd = nodeEnd > end ? end : nodeEnd;
            while (currentPos <= currentEnd) {
                int offset = (int) ((currentPos - nodeStart) / node.getInterval());
                if (offset < node.getLength()) {
                    double value = node.getValue(offset);
                    if (value != MonitorConstants.VALUE_NOT_EXIST) {
                        long timestamp = nodeStart + (long) offset * node.getInterval();
                        MetricData data = new MetricData(seriesId, timestamp, value);
                        dataList.add(data);
                    }
                }
                currentPos += step;
            }
        }
        return dataList;
    }

    private long calcFirstPos(long start, long nodeStepSeconds) {
        if (nodeStepSeconds == 1) {
            return start;
        }
        long firstStart = TimestampUtils.calcStartByInterval(start, nodeStepSeconds);
        return start == firstStart ? start : firstStart + nodeStepSeconds;
    }

    private long calcFirstPosInNextNode(long currentPos, long interval, long step) {
        long nextPos = currentPos + step;
        long nodeStart = TimestampUtils.calcStartByInterval(currentPos, interval);
        long nextNodeStart = nodeStart + interval;
        if (nextPos >= nextNodeStart) {
            return nextPos;
        }
        long stepCount = (nextNodeStart - nextPos) / step;
        return nextPos + stepCount * step;
    }

    private Map<Long, MetricDataRange> emptySeriesId2RangeMap(List<Long> seriesIds) {
        return seriesIds.stream().collect(Collectors.toMap(seriesId -> seriesId,
                seriesId -> new MetricDataRange(seriesId, Collections.emptyList())));
    }

    private Map<Long, List<ValueNode>> fetchNodeFromDb(List<Long> seriesIds, Long start, Long end) {
        long nodeStart = writeCache.nodeStartFromPos(start);
        long nodeEnd = writeCache.nodeStartFromPos(end);
        long nodeIntervalSeconds = writeCache.getNodeIntervalSeconds();
        Map<Long, List<ValueNode>> seriesId2NodeList = persistent.scan(seriesIds, nodeStart, nodeEnd);
        int nodesCountFromDb = 0;
        int nodesFilledCount = 0;
        for (Long seriesId : seriesIds) {
            List<ValueNode> nodes = seriesId2NodeList.getOrDefault(seriesId, Collections.emptyList());
            nodesCountFromDb += nodes.size();
            List<ValueNode> filledNodes =
                    fillEmptyNodesIfNotExists(seriesId, nodes, nodeStart, nodeEnd, nodeIntervalSeconds);
            nodesFilledCount += (filledNodes.size() - nodes.size());
            seriesId2NodeList.put(seriesId, filledNodes);
        }
        log.info("fetchFromDb, nodesCountFromDb={}, nodesFilledCount={}", nodesCountFromDb, nodesFilledCount);
        return seriesId2NodeList;
    }

    LinkedList<ValueNode> fillEmptyNodesIfNotExists(Long seriesId, List<ValueNode> sourceNodes, long nodeStart,
            long nodeEnd, long nodeIntervalSeconds) {
        Validate.isTrue(nodeStart <= nodeEnd, "nodeStart cannot after nodeEnd");
        int sourceNodesSize = sourceNodes.size();
        if (sourceNodesSize > 1) {
            sourceNodes.sort(Comparator.comparingLong(ValueNode::getEpochSecondStart));
        }
        int sourceNodePos = 0;
        LinkedList<ValueNode> valueNodes = new LinkedList<>();
        for (long nodePos = nodeStart; nodePos <= nodeEnd; nodePos += nodeIntervalSeconds) {
            if (sourceNodePos < sourceNodesSize && nodePos == sourceNodes.get(sourceNodePos).getEpochSecondStart()) {
                valueNodes.add(sourceNodes.get(sourceNodePos));
                sourceNodePos++;
            } else {
                valueNodes.add(ValueNode.createEmptyNode(seriesId, nodePos));
            }
        }
        return valueNodes;
    }

    /**
     * initial QueryContext, calculate write cache query parameters
     */
    private QueryContext createQueryContext(Long start, Long end, long nowSecond, long step) {
        Validate.isTrue(start < nowSecond, "start should before now");
        // start time align cache interval
        long firstPosInWriteCache = writeCache.calcFirstPos(start);

        // start time align step
        long startTime = TimestampUtils.calcNextByInterval(firstPosInWriteCache, step);

        // fix end
        final long endTime = end > nowSecond ? nowSecond : end;
        if (end > nowSecond) {
            log.warn("end after nowSecond, fix to nowSecond={}", endTime);
        }
        QueryContext context = new QueryContext();
        context.setQueryStart(startTime);
        context.setQueryEnd(endTime);

        long wCacheBegin = writeCache.getCacheStartSecond();
        if (wCacheBegin > endTime) {
            context.setNeedQueryWriteCache(false);
        } else {
            long wCacheStart = Math.max(wCacheBegin, startTime);
            wCacheStart = TimestampUtils.calcNextByInterval(wCacheStart, step);
            context.setNeedQueryWriteCache(true);
            context.setWCacheStart(wCacheStart);
            context.setWCacheEnd(endTime);
        }
        return context;
    }

    @Data
    private class QueryContext {

        private long queryStart;
        private long queryEnd;

        private long wCacheStart;
        private long wCacheEnd;
        private boolean needQueryWriteCache = true;
        private boolean needQueryDb = false;
        private long dbStart;
        private long dbEnd;

        private transient Map<Long, MetricDataRange> rangesFromWriteCache = null;
        private transient Map<Long, MetricDataRange> rangesFromDb = null;

        /**
         * below statistic fields
         */
        private Integer wCacheDataCount;
        private Integer dbCount;

        @JsonIgnore
        Map<Long, MetricDataRange> getMergedResult() {
            return merges(rangesFromWriteCache, rangesFromDb);
        }

        void putWriteCacheResult(Map<Long, MetricDataRange> rangesFromWriteCache) {
            Validate.notNull(rangesFromWriteCache, "rangesFromWriteCache is null");
            this.rangesFromWriteCache = rangesFromWriteCache;
            this.wCacheDataCount = countWriteCacheResult();
            long writeCacheResultFrom = writeCacheResultFrom();
            if (writeCacheResultFrom > queryStart) {
                needQueryDb = true;
                dbStart = queryStart;
                dbEnd = writeCache.calcPreviousPos(writeCacheResultFrom);
            }
        }

        void putDbResult(Map<Long, MetricDataRange> rangesFromDb) {
            Validate.notNull(rangesFromDb, "rangesFromDb is null");
            this.rangesFromDb = rangesFromDb;
            this.dbCount = countDbResult();
        }

        private long writeCacheResultFrom() {
            long resultFrom = queryEnd + 1;
            for (Map.Entry<Long, MetricDataRange> entry : rangesFromWriteCache.entrySet()) {
                Long rangeFrom = entry.getValue().getRangeFrom();
                if (rangeFrom != null && rangeFrom < resultFrom) {
                    resultFrom = rangeFrom;
                }
            }
            return resultFrom;
        }

        Integer countWriteCacheResult() {
            return countFromResult(rangesFromWriteCache);
        }

        Integer countDbResult() {
            return countFromResult(rangesFromDb);
        }

        private Integer countFromResult(Map<Long, MetricDataRange> result) {
            if (result == null) {
                return null;
            }
            int count = 0;
            for (Map.Entry<Long, MetricDataRange> entry : result.entrySet()) {
                count += entry.getValue().getCount();
            }
            return count;
        }

        private Map<Long, MetricDataRange> merges(Map<Long, MetricDataRange> left, Map<Long, MetricDataRange> right) {
            if (CollectionUtils.isEmpty(right)) {
                return left;
            }
            Map<Long, MetricDataRange> seriesId2DataRange = new LinkedHashMap<>();
            right.forEach((seriesId, dataList) -> seriesId2DataRange.put(seriesId,
                    merge(seriesId, dataList, left.get(seriesId))));
            return seriesId2DataRange;
        }

        private MetricDataRange merge(Long seriesId, MetricDataRange range1, MetricDataRange range2) {
            if (range2 == null || CollectionUtils.isEmpty(range2.getDataList())) {
                return range1;
            }
            Map<Long, MetricData> timestamp2Data = new TreeMap<>();
            for (MetricData data : range1.getDataList()) {
                timestamp2Data.put(data.getTimestamp(), data);
            }
            for (MetricData data : range2.getDataList()) {
                timestamp2Data.put(data.getTimestamp(), data);
            }
            ArrayList<MetricData> dataList = new ArrayList<>(timestamp2Data.values());
            return new MetricDataRange(seriesId, dataList);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("queryStart", queryStart)
                    .add("queryEnd", queryEnd)
                    .add("wCacheStart", wCacheStart)
                    .add("wCacheEnd", wCacheEnd)
                    .add("needQueryWriteCache", needQueryWriteCache)
                    .add("needQueryDb", needQueryDb)
                    .add("dbStart", dbStart)
                    .add("dbEnd", dbEnd)
                    .toString();
        }
    }

}
