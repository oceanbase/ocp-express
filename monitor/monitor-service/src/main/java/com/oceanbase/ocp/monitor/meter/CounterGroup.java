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

package com.oceanbase.ocp.monitor.meter;

import java.util.concurrent.Callable;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class CounterGroup extends MeterGroup<Counter> {

    CounterGroup(MeterRegistry registry, Builder builder) {
        super(registry, builder.name, builder.description, builder.baseUnits, builder.labelNames);
    }

    @Override
    protected Counter construct(String[] tags) {
        return Counter.builder(name)
                .description(description)
                .baseUnit(baseUnits)
                .tags(tags)
                .register(registry);
    }

    public static final class Builder extends MeterGroup.Builder<Counter, CounterGroup, Builder> {

        Builder(String name) {
            super(name);
        }

        public CounterGroup build(MeterRegistry registry) {
            return new CounterGroup(registry, this);
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public <V> V time(Object label0, Callable<V> callable) {
        return MeterUtils.time(of(label0), callable);
    }

    public <V> V time(Object labelValue0, Object labelValue1, Callable<V> callable) {
        return MeterUtils.time(of(labelValue0, labelValue1), callable);
    }

    public <V> V time(Object labelValue0, Object labelValue1, Object labelValue2, Callable<V> callable) {
        return MeterUtils.time(of(labelValue0, labelValue1, labelValue2), callable);
    }

    public <V> V time(Object labelValue0, Object labelValue1, Object labelValue2, Object labelValue3,
            Callable<V> callable) {
        return MeterUtils.time(of(labelValue0, labelValue1, labelValue2, labelValue3), callable);
    }

    public <V> V time(Object[] labelValues, Callable<V> callable) {
        return MeterUtils.time(of(labelValues), callable);
    }

    public void time(Object label0, Runnable runnable) {
        MeterUtils.time(of(label0), runnable);
    }

    public void time(Object labelValue0, Object labelValue1, Runnable runnable) {
        MeterUtils.time(of(labelValue0, labelValue1), runnable);
    }

    public void time(Object labelValue0, Object labelValue1, Object labelValue2, Runnable runnable) {
        MeterUtils.time(of(labelValue0, labelValue1, labelValue2), runnable);
    }

    public void time(Object labelValue0, Object labelValue1, Object labelValue2, Object labelValue3,
            Runnable runnable) {
        MeterUtils.time(of(labelValue0, labelValue1, labelValue2, labelValue3), runnable);
    }

    public void time(Object[] labelValues, Runnable runnable) {
        MeterUtils.time(of(labelValues), runnable);
    }

    public void increment(Object label0, double amount) {
        of(label0).increment(amount);
    }

    public void incrementOne(Object... label0) {
        of(label0).increment(1);
    }

    public void increment(Object labelValue0, Object labelValue1, double amount) {
        of(labelValue0, labelValue1).increment(amount);
    }

    public void increment(Object labelValue0, Object labelValue1, Object labelValue2, double amount) {
        of(labelValue0, labelValue1, labelValue2).increment(amount);
    }

    public void increment(Object labelValue0, Object labelValue1, Object labelValue2, Object labelValue3,
            double amount) {
        of(labelValue0, labelValue1, labelValue2, labelValue3).increment(amount);
    }

    public void increment(Object[] labelValues, double amount) {
        of(labelValues).increment(amount);
    }
}
