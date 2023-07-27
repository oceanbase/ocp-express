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

package com.oceanbase.ocp.monitor.storage;

import java.util.LinkedList;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.monitor.helper.BufferedQueue;
import com.oceanbase.ocp.monitor.meter.CounterGroup;
import com.oceanbase.ocp.monitor.model.storage.ValueNode;

import io.micrometer.core.instrument.MeterRegistry;

public class MetricDataWriteQueue implements IMetricDataWriteQueue {

    private final BufferedQueue<ValueNode> queue;

    private CounterGroup valueNodeCounter;

    public MetricDataWriteQueue(int queueSize) {
        this.queue = new BufferedQueue<>(queueSize);
    }

    public void startMeter(MeterRegistry meterRegistry) {
        valueNodeCounter = CounterGroup.builder("ocp_monitor_value_node_count")
                .description("OCP metric value node count")
                .labelNames("source", "type")
                .build(meterRegistry);
    }

    @Override
    public boolean offer(ValueNode node) {
        Validate.notNull(node, "node is null");

        queue.offer(node);
        countingValueNode("offer");
        return true;
    }

    @Override
    public LinkedList<ValueNode> poll(int size) {
        Validate.isTrue(size > 0, "size must > 0");

        LinkedList<ValueNode> nodes = new LinkedList<>();
        while (nodes.size() < size) {
            ValueNode node = queue.poll();
            if (node == null) {
                break;
            }
            countingValueNode("poll");
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public int size() {
        return queue.getBufSize();
    }

    private void countingValueNode(String type) {
        if (valueNodeCounter == null) {
            return;
        }
        valueNodeCounter.incrementOne("writeQueue", type);
    }

}
