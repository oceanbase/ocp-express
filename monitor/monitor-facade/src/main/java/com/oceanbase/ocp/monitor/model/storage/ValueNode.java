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
package com.oceanbase.ocp.monitor.model.storage;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.monitor.constants.MonitorConstants;

public class ValueNode {

    private static final int DEFAULT_NODE_LENGTH = 60;

    private final long seriesId;
    private final long epochSecondStart;
    private final int length;
    private final double[] values;

    private long collectAt = 0;

    private long storeAt = 0;

    private long loadAt = 0;

    private int interval = 1;

    public ValueNode(long seriesId, long epochSecondStart) {
        this(seriesId, epochSecondStart, DEFAULT_NODE_LENGTH);
    }

    public static ValueNode createEmptyNode(long seriesId, long epochSecondStart) {
        return new ValueNode(seriesId, epochSecondStart, 0);
    }

    private ValueNode(long seriesId, long epochSecondStart, int length) {
        this(seriesId, epochSecondStart, initialValues(length));
    }

    public ValueNode(long seriesId, long epochSecondStart, double[] values) {
        this.length = values.length;
        this.seriesId = seriesId;
        this.epochSecondStart = epochSecondStart;
        this.values = values;
    }

    public ValueNode(long seriesId, long epochSecondStart, int length, int interval) {
        this(seriesId, epochSecondStart, initialValues(length), interval);
    }

    public ValueNode(long seriesId, long epochSecondStart, double[] values, int interval) {
        this.length = values.length;
        this.seriesId = seriesId;
        this.epochSecondStart = epochSecondStart;
        this.values = values;
        this.interval = interval;
    }

    public boolean isEmptyNode() {
        return this.length == 0;
    }

    public double getFirstValidValue() {
        int firstValidOffset = getFirstValidOffset();
        if (firstValidOffset >= 0) {
            return values[firstValidOffset];
        }
        return MonitorConstants.VALUE_NOT_EXIST;
    }

    public int getFirstValidOffset() {
        for (int offset = 0; offset < length; offset++) {
            if (values[offset] != MonitorConstants.VALUE_NOT_EXIST) {
                return offset;
            }
        }
        return -1;
    }

    public void setValue(int offset, double value) {
        Validate.isTrue(offset >= 0, "offset cannot be negative");
        Validate.isTrue(offset < length, "offset over length");
        values[offset] = value;
    }

    public double getValue(int offset) {
        Validate.isTrue(offset >= 0, "offset cannot be negative");
        Validate.isTrue(offset < length, "offset over length");
        return values[offset];
    }

    private static double[] initialValues(int length) {
        double[] values = new double[length];
        Arrays.fill(values, MonitorConstants.VALUE_NOT_EXIST);
        return values;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public long getEpochSecondStart() {
        return epochSecondStart;
    }

    public double[] getValues() {
        return values;
    }

    public int getLength() {
        return length;
    }

    public long getCollectAt() {
        return collectAt;
    }

    public void setCollectAt(long collectAt) {
        this.collectAt = collectAt;
    }

    public long getStoreAt() {
        return storeAt;
    }

    public void setStoreAt(long storeAt) {
        this.storeAt = storeAt;
    }

    public long getLoadAt() {
        return loadAt;
    }

    public void setLoadAt(long loadAt) {
        this.loadAt = loadAt;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueNode valueNode = (ValueNode) o;
        return seriesId == valueNode.seriesId && epochSecondStart == valueNode.epochSecondStart
                && length == valueNode.length && collectAt == valueNode.collectAt && storeAt == valueNode.storeAt
                && loadAt == valueNode.loadAt && Arrays.equals(values, valueNode.values);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(seriesId, epochSecondStart, length, collectAt, storeAt, loadAt);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}
