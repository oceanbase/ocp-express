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

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeUtils {

    public static String formatTimeUsToDate(long timeUs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeUs / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(c.getTime());
    }

    public static String getDateString(int offsetDay, String dateFormat) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, offsetDay);
        date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(date);
    }

    public static OffsetDateTime calculateStartTimeOfThisDay(OffsetDateTime time) {
        int year = time.get(ChronoField.YEAR);
        int month = time.get(ChronoField.MONTH_OF_YEAR);
        int day = time.get(ChronoField.DAY_OF_MONTH);
        return OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    /**
     * Duration to microsecond.
     *
     * @param duration time duration
     * @return microsecond
     */
    public static long toUs(Duration duration) {
        return TimeUnit.SECONDS.toMicros(duration.getSeconds()) + TimeUnit.NANOSECONDS.toMicros(duration.getNano());
    }

    /**
     * Convert second level timestamp to microsecond.
     *
     * @param seconds second level timestamp
     * @return microsecond
     */
    public static long toUs(Long seconds) {
        return TimeUnit.SECONDS.toMicros(seconds);
    }

    /**
     * Instant to microsecond.
     *
     * @param instant An instantaneous point on the time-line.
     * @return microsecond
     */
    public static long toUs(Instant instant) {
        return TimeUnit.SECONDS.toMicros(instant.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(instant.getNano());
    }

    /**
     * Offset date time to microsecond.
     *
     * @param offsetDateTime datetime with offset
     * @return microsecond
     */
    public static long toUs(OffsetDateTime offsetDateTime) {
        return TimeUnit.SECONDS.toMicros(offsetDateTime.toInstant().getEpochSecond())
                + TimeUnit.NANOSECONDS.toMicros(offsetDateTime.toInstant().getNano());
    }

    public static Instant usToInstant(long micros) {
        return Instant.ofEpochSecond(TimeUnit.MICROSECONDS.toSeconds(micros), (micros % 1_000_000) * 1_000);
    }

    public static OffsetDateTime usToUtc(long micros) {
        return msToUtc(TimeUnit.MICROSECONDS.toMillis(micros));
    }

    public static OffsetDateTime usToMicroUtc(long micros) {
        return usToInstant(micros).atOffset(ZoneOffset.UTC);
    }

    public static String usToUtcString(long micros) {
        return usToUtc(micros).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static OffsetDateTime msToUtc(long millis) {
        return Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC);
    }

    public static String toUtcString(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String toLocalString(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static String toLocalString(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static Instant atUtcStartOfDay(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static Instant atUtcStartOfDay(long micros) {
        return atUtcStartOfDay(usToInstant(micros));
    }

    public static OffsetDateTime atUtcStartOfDay(OffsetDateTime time) {
        OffsetDateTime result = time.withOffsetSameInstant(ZoneOffset.UTC);
        return calculateStartTimeOfThisDay(result);
    }

    public static <T extends Temporal> T min(T t1, T t2) {
        if (Duration.between(t1, t2).isNegative()) {
            return t2;
        }
        return t1;
    }

    public static <T extends Temporal> T max(T t1, T t2) {
        if (Duration.between(t1, t2).isNegative()) {
            return t1;
        }
        return t2;
    }

    public static OffsetDateTime toUtc(Instant instant) {
        return usToUtc(toUs(instant));
    }

}
