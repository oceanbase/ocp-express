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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.Validate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import lombok.SneakyThrows;

public class MeterUtils {

    /**
     * Timing, unit is millisecond.
     */
    @SneakyThrows
    public static void time(DistributionSummary meter, long start) {
        meter.record(System.currentTimeMillis() - start);
    }

    /**
     * Timing, unit is millisecond.
     */
    @SneakyThrows
    public static void duration(DistributionSummary meter, long duration) {
        meter.record(duration);
    }

    /**
     * Timing, unit is millisecond.
     */
    @SneakyThrows
    public static <V> V time(DistributionSummary meter, Callable<V> callable) {
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } finally {
            meter.record(System.currentTimeMillis() - start);
        }
    }

    /**
     * Timing, unit is millisecond.
     */
    @SneakyThrows
    public static void time(DistributionSummary meter, Runnable runnable) {
        long start = System.currentTimeMillis();
        try {
            runnable.run();
        } finally {
            meter.record(System.currentTimeMillis() - start);
        }
    }

    /**
     * Timing, unit is millisecond.
     */
    @SneakyThrows
    public static <V> V time(Counter meter, Callable<V> callable) {
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } finally {
            meter.increment(System.currentTimeMillis() - start);
        }
    }

    /**
     * Timing, unit is millisecond.
     */
    @SneakyThrows
    public static void time(Counter meter, Runnable runnable) {
        long start = System.currentTimeMillis();
        try {
            runnable.run();
        } finally {
            meter.increment(System.currentTimeMillis() - start);
        }
    }

    public static void time(DistributionSummary meter, TemporalUnit unit, Runnable runnable) {
        Validate.notNull(meter, "Meter require non-null");
        Validate.notNull(unit, "Time unit require non-null");
        Validate.notNull(runnable, "Runnable require non-null");
        Instant start = Instant.now();
        try {
            runnable.run();
        } finally {
            meter.record((double) start.until(Instant.now(), unit));
        }
    }

    public static void timeSeconds(DistributionSummary meter, Runnable runnable) {
        time(meter, ChronoUnit.SECONDS, runnable);
    }

}
