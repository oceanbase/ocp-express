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

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;

public final class DistributionSummaryGroup extends MeterGroup<DistributionSummary> {

    double[] serviceLevelObjectives;

    private DistributionSummaryGroup(MeterRegistry registry, Builder builder) {
        super(registry, builder.name, builder.description, builder.baseUnits, builder.labelNames);
        this.serviceLevelObjectives = builder.serviceLevelObjectives;
    }

    @Override
    protected DistributionSummary construct(String[] tags) {
        return DistributionSummary.builder(name)
                .description(description)
                .baseUnit(baseUnits)
                .tags(tags)
                .serviceLevelObjectives(serviceLevelObjectives)
                .register(registry);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder extends
            MeterGroup.Builder<DistributionSummary, DistributionSummaryGroup, Builder> {

        private double[] serviceLevelObjectives;

        Builder(String name) {
            super(name);
        }

        private static final double[] FOREGROUND_TIME_SLOS = {300D, 1200D};

        private static final double[] BACKGROUND_TIME_SLOS = {100D, 200D, 500D, 1000D, 2000D, 5000D};

        public Builder defaultForegroundTimeServiceLevelObjectives() {
            this.baseUnits(BaseUnits.MILLISECONDS);
            this.serviceLevelObjectives = FOREGROUND_TIME_SLOS;
            return this;
        }

        public Builder defaultBackgroundTimeServiceLevelObjectives() {
            this.baseUnits(BaseUnits.MILLISECONDS);
            this.serviceLevelObjectives = BACKGROUND_TIME_SLOS;
            return this;
        }

        public Builder serviceLevelObjectives(double... slos) {
            this.serviceLevelObjectives = slos;
            return this;
        }

        @Override
        public DistributionSummaryGroup build(MeterRegistry registry) {
            return new DistributionSummaryGroup(registry, this);
        }
    }

    public void time(Object label0, long start) {
        MeterUtils.time(of(label0), start);
    }

    public void duration(Object label0, long duration) {
        MeterUtils.duration(of(label0), duration);
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

    public void time(Object[] labelValues, long amount) {
        MeterUtils.time(of(labelValues), amount);
    }
}
