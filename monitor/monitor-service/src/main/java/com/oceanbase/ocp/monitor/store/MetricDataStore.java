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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oceanbase.ocp.monitor.IIntervalMetricDataStore;
import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.monitor.model.metric.MetricData;
import com.oceanbase.ocp.monitor.model.metric.MetricDataRange;
import com.oceanbase.ocp.monitor.model.metric.MetricLine;
import com.oceanbase.ocp.monitor.model.storage.ValueNode;
import com.oceanbase.ocp.monitor.storage.IRollupMetricDataDao;
import com.oceanbase.ocp.monitor.util.TimestampUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricDataStore implements IIntervalMetricDataStore {

    private final IRollupMetricDataDao persistent;

    public MetricDataStore(IRollupMetricDataDao persistent) {
        this.persistent = persistent;
    }

    @Override
    public int store(LinkedList<MetricLine> lines) {
        return lines.size();
    }

    @Override
    public Map<Long, MetricDataRange> ranges(List<Long> seriesIds, Long start, Long end, Long step) {
        Map<Long, List<ValueNode>> nodeFromDb = fetchNodeFromDb(seriesIds, start, end);
        Map<Long, MetricDataRange> rangesFromDb = new HashMap<>();
        for (Long seriesId : nodeFromDb.keySet()) {
            MetricDataRange range = new MetricDataRange();
            range.setSeriesId(seriesId);
            range.setDataList(mapToMetricData(seriesId, nodeFromDb.get(seriesId), start, end, step));
            rangesFromDb.put(seriesId, range);
        }
        return rangesFromDb;
    }

    private Map<Long, List<ValueNode>> fetchNodeFromDb(List<Long> seriesIds, Long start, Long end) {
        Map<Long, List<ValueNode>> seriesId2NodeList = persistent.scan(seriesIds, start, end);
        for (Long seriesId : seriesIds) {
            List<ValueNode> nodes = seriesId2NodeList.getOrDefault(seriesId, Collections.emptyList());
            seriesId2NodeList.put(seriesId, nodes);
        }
        return seriesId2NodeList;
    }

    private List<MetricData> mapToMetricData(Long seriesId, List<ValueNode> valueNodes, Long start, Long end,
            Long step) {
        List<MetricData> dataList = new ArrayList<>();
        ValueNode valid = getFirstValidNode(valueNodes);
        if (valid == null) {
            return dataList;
        }
        long nodeIntervalSeconds = valid.getInterval() * 60L;
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

    private ValueNode getFirstValidNode(List<ValueNode> nodeList) {
        int idx = 0;
        while (idx < nodeList.size()) {
            if (nodeList.get(idx) != null) {
                return nodeList.get(idx);
            }
            idx++;
        }
        return null;
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

}
