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

import java.util.Objects;

public class ValueNodeKey {

    private long seriesId;
    private long epochSecondStart;

    public ValueNodeKey(long seriesId, long epochSecondStart) {
        this.seriesId = seriesId;
        this.epochSecondStart = epochSecondStart;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(long seriesId) {
        this.seriesId = seriesId;
    }

    public long getEpochSecondStart() {
        return epochSecondStart;
    }

    public void setEpochSecondStart(long epochSecondStart) {
        this.epochSecondStart = epochSecondStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueNodeKey that = (ValueNodeKey) o;
        return seriesId == that.seriesId && epochSecondStart == that.epochSecondStart;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seriesId, epochSecondStart);
    }
}
