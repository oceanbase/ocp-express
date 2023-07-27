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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.monitor.IIntervalMetricDataCache;
import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.monitor.meter.CounterGroup;
import com.oceanbase.ocp.monitor.model.metric.MetricData;
import com.oceanbase.ocp.monitor.model.metric.MetricDataRange;
import com.oceanbase.ocp.monitor.model.storage.ValueNode;
import com.oceanbase.ocp.monitor.model.storage.ValueNodeKey;
import com.oceanbase.ocp.monitor.storage.MetricDataWriteQueue;
import com.oceanbase.ocp.monitor.util.MonitorPropertyUtils;
import com.oceanbase.ocp.monitor.util.TimestampUtils;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntervalMetricDataCache implements IIntervalMetricDataCache {

    private final CircularFifoQueue<Long> readCacheSecondQueue;
    private final CircularFifoQueue<Map<ValueNodeKey, ValueNode>> readCache;

    private final long nodeIntervalSeconds;
    private final int dumpDelaySecs;
    private long nodeStepSeconds;

    private final Map<Long, Long> seriesIdTsMap;

    private final MetricDataWriteQueue writeQueue;

    private final Queue<ValueNodeKey> keyCacheQueue;

    private final Map<ValueNodeKey, ValueNode> writeCacheMap;

    private final ScheduledThreadPoolExecutor dumpExecutor;

    private CounterGroup valueNodeCounter;

    public IntervalMetricDataCache(int readCacheCount, long nodeIntervalSeconds, MetricDataWriteQueue writeQueue,
            int dumpDelaySecs) {
        Validate.isTrue(nodeIntervalSeconds > 0, "intervalSeconds must be positive");
        Validate.isTrue(nodeIntervalSeconds % 60 == 0, "intervalSeconds must be times of 60");
        this.readCacheSecondQueue = new CircularFifoQueue<>(readCacheCount);
        this.readCache = new CircularFifoQueue<>(readCacheCount);
        this.nodeIntervalSeconds = nodeIntervalSeconds;
        this.nodeStepSeconds = nodeIntervalSeconds / 60;
        this.writeQueue = writeQueue;
        this.seriesIdTsMap = new ConcurrentHashMap<>(10240);

        this.writeCacheMap = new ConcurrentHashMap<>(10240);
        this.keyCacheQueue = new ConcurrentLinkedQueue<>();
        this.dumpDelaySecs = dumpDelaySecs;
        if (writeQueue != null) {
            this.dumpExecutor = new ScheduledThreadPoolExecutor(1,
                    new OcpThreadFactory("dump-executor-interval" + nodeIntervalSeconds));
            dumpExecutor.scheduleWithFixedDelay(this::dumpValueNode, 10, 3, TimeUnit.SECONDS);
        } else {
            this.dumpExecutor = null;
        }
    }

    public void startMeter(MeterRegistry meterRegistry) {
        valueNodeCounter = CounterGroup.builder("ocp_monitor_value_node_count")
                .description("OCP metric value node count")
                .labelNames("source", "type")
                .build(meterRegistry);
    }

    public long rCacheSize() {
        long rCacheSize = 0;
        for (Map<ValueNodeKey, ValueNode> rCache : readCache) {
            rCacheSize += rCache.size();
        }
        return rCacheSize;
    }

    public long wCacheSize() {
        return writeCacheMap.size();
    }

    public long getCacheStartSecond() {
        if (!readCacheSecondQueue.isEmpty()) {
            return readCacheSecondQueue.get(0);
        }
        return Optional.ofNullable(keyCacheQueue.peek())
                .map(ValueNodeKey::getEpochSecondStart)
                .orElse(Instant.now().getEpochSecond());
    }

    @Override
    public long getNodeIntervalSeconds() {
        return nodeIntervalSeconds;
    }

    @Override
    public MetricDataRange range(Long seriesId, Long start, Long end, Long step) {
        List<MetricData> dataList = series(seriesId, start, end, step);
        return new MetricDataRange(seriesId, dataList);
    }

    @Override
    public Map<Long, MetricDataRange> ranges(List<Long> seriesIds, Long start, Long end, Long step) {
        return seriesIds.stream()
                .collect(Collectors.toMap(seriesId -> seriesId, seriesId -> range(seriesId, start, end, step)));
    }

    public List<MetricData> series(Long seriesId, Long start, Long end, Long step) {
        Validate.notNull(seriesId, "seriesId is null");
        Validate.isTrue(start <= end, "start should not after end");
        Validate.isTrue(step % nodeStepSeconds == 0, "step should times of nodeStepSeconds");

        List<MetricData> dataList = new ArrayList<>();
        long currentPos = calcFirstPos(start);
        while (currentPos <= end) {
            long nodeStart = nodeStartFromPos(currentPos);
            ValueNode node = getFromCache(new ValueNodeKey(seriesId, nodeStart));
            if (node == null || node.isEmptyNode()) {
                currentPos = calcFirstPosInNextNode(currentPos, step);
                continue;
            }
            countingValueNode("read");
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

    @Override
    public boolean write(MetricData data) {
        Long seriesId = data.getSeriesId();
        Long timestamp = data.getTimestamp();
        long maxTimestamp = seriesIdTsMap.getOrDefault(seriesId, 0L);
        if (maxTimestamp >= timestamp) {
            log.debug("Abandon metric data, seriesId={}, timestamp={}, maxTimestamp={}",
                    seriesId, timestamp, maxTimestamp);
            return false;
        }
        long nodeStart = nodeStartFromPos(timestamp);
        if (nodeIntervalSeconds == 60) {
            int interval = MonitorPropertyUtils.getStandardSecondCollectInterval();
            if (interval != nodeStepSeconds) {
                nodeStepSeconds = interval;
            }
        }
        int offset = (int) ((timestamp - nodeStart) / nodeStepSeconds);
        ValueNodeKey valueNodeKey = new ValueNodeKey(seriesId, nodeStart);
        if (!writeCacheMap.containsKey(valueNodeKey)) {
            ValueNode valueNode = new ValueNode(seriesId, nodeStart, (int) (nodeIntervalSeconds / nodeStepSeconds),
                    (int) nodeStepSeconds);
            keyCacheQueue.add(valueNodeKey);
            writeCacheMap.put(valueNodeKey, valueNode);
        }
        ValueNode valueNode = writeCacheMap.get(valueNodeKey);
        // 修改间隔后的下一分钟才开始存入采集的数据
        if (valueNode.getInterval() == nodeStepSeconds) {
            valueNode.setValue(offset, data.getValue());
        }
        writeCacheMap.put(valueNodeKey, valueNode);
        countingValueNode("write");
        seriesIdTsMap.put(seriesId, timestamp);
        return true;
    }

    private void countingValueNode(String type) {
        if (valueNodeCounter == null) {
            return;
        }
        valueNodeCounter.incrementOne("cache", type);
    }

    @Override
    public int writes(LinkedList<MetricData> dataList) {
        Validate.notNull(dataList, "data is null");
        int successCount = 0;
        MetricData e;
        while ((e = dataList.poll()) != null) {
            if (write(e)) {
                successCount++;
            }
        }
        return successCount;
    }

    long calcPreviousPos(long start) {
        long firstStart = calcFirstPos(start);
        return firstStart - nodeStepSeconds;
    }

    long calcFirstPos(long start) {
        if (nodeStepSeconds == 1) {
            return start;
        }
        long firstStart = TimestampUtils.calcStartByInterval(start, nodeStepSeconds);
        return start == firstStart ? start : firstStart + nodeStepSeconds;
    }

    private long calcFirstPosInNextNode(long currentPos, long step) {
        long nextPos = currentPos + step;
        long nodeStart = nodeStartFromPos(currentPos);
        long nextNodeStart = nodeStart + nodeIntervalSeconds;
        if (nextPos >= nextNodeStart) {
            return nextPos;
        }
        long stepCount = (nextNodeStart - nextPos) / step;
        return nextPos + stepCount * step;
    }

    private ValueNode getFromCache(ValueNodeKey nodeKey) {
        if (writeCacheMap.containsKey(nodeKey)) {
            return writeCacheMap.get(nodeKey);
        }
        for (Map<ValueNodeKey, ValueNode> rCache : readCache) {
            if (rCache.containsKey(nodeKey)) {
                return rCache.get(nodeKey);
            }
        }
        return null;
    }

    private void dumpValueNode() {
        Function<ValueNode, Boolean> consumable = valueNode -> {
            long nodeEnd = valueNode.getEpochSecondStart() + nodeIntervalSeconds - 1;
            return Instant.now().getEpochSecond() - nodeEnd > dumpDelaySecs;
        };
        Map<ValueNodeKey, ValueNode> readCacheSegment = new HashMap<>();
        ValueNodeKey headerKey = keyCacheQueue.peek();
        Set<ValueNodeKey> writeCacheRemoveSet = new HashSet<>();
        while (!keyCacheQueue.isEmpty()) {
            try {
                headerKey = keyCacheQueue.peek();
                ValueNode node = writeCacheMap.get(headerKey);
                if (node == null) {
                    keyCacheQueue.poll();
                    continue;
                }
                if (!consumable.apply(node)) {
                    break;
                }
                ValueNode valueNode = writeCacheMap.get(headerKey);
                writeCacheRemoveSet.add(headerKey);
                readCacheSegment.put(headerKey, valueNode);
                writeQueue.offer(valueNode);
                keyCacheQueue.poll();
            } catch (Throwable throwable) {
                log.warn("Dump value node failed.", throwable);
                writeCacheMap.remove(headerKey);
                writeCacheRemoveSet.remove(headerKey);
                keyCacheQueue.poll();
            }
        }
        if (!writeCacheRemoveSet.isEmpty()) {
            for (ValueNodeKey toRemove : writeCacheRemoveSet) {
                writeCacheMap.remove(toRemove);
            }
        }
        if (!readCacheSegment.isEmpty()) {
            readCache.add(readCacheSegment);
            readCacheSecondQueue.add(headerKey.getEpochSecondStart());
        }
    }

    long nodeStartFromPos(long pos) {
        return TimestampUtils.calcStartByInterval(pos, nodeIntervalSeconds);
    }

    @PreDestroy
    public void destroy() {
        if (dumpExecutor != null) {
            ExecutorUtils.shutdown(dumpExecutor, 1);
        }
    }

}
