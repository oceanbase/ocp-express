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

package com.oceanbase.ocp.common.util.time;

import static com.oceanbase.ocp.common.util.time.TimeUtils.usToInstant;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.Validate;

import com.google.common.base.MoreObjects;

import lombok.EqualsAndHashCode;

/**
 * Time interval left close and right open.
 */
@EqualsAndHashCode
public final class Interval {

    public final Instant start;
    public final Instant end;

    public Interval(Instant start, Instant end) {
        Validate.notNull(start, "Interval start-time require non-null");
        Validate.notNull(end, "Interval end-time require non-null");
        Validate.isTrue(start.isBefore(end),
                "Interval start-time require less than end-time: start-time=%d, end-time=%d", start.toEpochMilli(),
                end.toEpochMilli());
        this.start = start;
        this.end = end;
    }

    public static Interval of(Instant start, Instant end) {
        return new Interval(start, end);
    }

    public static Interval of(OffsetDateTime start, OffsetDateTime end) {
        return of(start.toInstant(), end.toInstant());
    }

    public static Interval ofEpochMicro(long startMicros, long endMicros) {
        return of(usToInstant(startMicros), usToInstant(endMicros));
    }

    private volatile Duration duration;

    /**
     * Calculate the duration of the interval.
     *
     * <p>
     * Not thread-safe。
     * </p>
     *
     * @return duration of interval
     */
    public Duration duration() {
        if (duration == null) {
            synchronized (this) {
                if (duration == null) {
                    duration = Duration.of(start.until(end, ChronoUnit.MICROS), ChronoUnit.MICROS);
                }
            }
        }
        return duration;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("start", TimeUtils.toLocalString(start))
                .add("end", TimeUtils.toLocalString(end))
                .toString();
    }
}
