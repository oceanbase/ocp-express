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
package com.oceanbase.ocp.partitioning.policy;

import static com.oceanbase.ocp.common.util.ListUtils.listOf;
import static com.oceanbase.ocp.common.util.time.TimeUtils.toUtcString;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.springframework.boot.convert.DurationStyle;

import com.google.common.base.MoreObjects;

import com.oceanbase.ocp.common.util.time.Interval;
import com.oceanbase.ocp.common.util.time.TimeUtils;
import com.oceanbase.ocp.partitioning.constants.PartitioningType;
import com.oceanbase.ocp.partitioning.model.Partition;
import com.oceanbase.ocp.partitioning.model.PartitioningDefinition;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for partitioning rotating based on natural time.
 */
@Slf4j
@Getter
public class NaturalTimePartitionRolloverPolicy implements PartitionRolloverPolicy {

    private static final Duration MAX_PRE_CREATE = Duration.ofDays(732);
    private static final Duration MAX_RETENTION = Duration.ofDays(3660);

    /**
     * Rollover cycle of partitions.
     */
    public enum Cycle {

        /**
         * One partition per day.
         */
        DAILY {

            @Override
            Interval of(Instant instant, ZoneId zoneId, int offset) {
                Instant start = instant.atZone(zoneId).toLocalDate().plusDays(offset).atStartOfDay(zoneId).toInstant();
                return Interval.of(start, start.plus(1, ChronoUnit.DAYS));
            }
        },

        /**
         * One partition per month.
         */
        MONTHLY {

            @Override
            Interval of(Instant instant, ZoneId zoneId, int offset) {
                ZonedDateTime start =
                        instant.atZone(zoneId).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS)
                                .plusMonths(offset);
                ZonedDateTime end = start.plus(1, ChronoUnit.MONTHS);
                return Interval.of(start.toInstant(), end.toInstant());
            }
        },

        /**
         * One partition per year.
         */
        YEARLY {

            @Override
            Interval of(Instant instant, ZoneId zoneId, int offset) {
                ZonedDateTime start =
                        instant.atZone(zoneId).with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS)
                                .withMonth(1).plusYears(offset);
                ZonedDateTime end = start.plus(1, ChronoUnit.YEARS);
                return Interval.of(start.toInstant(), end.toInstant());
            }
        };

        /**
         * Obtain a time interval based on a natural time cycle at a specified time
         * point.
         *
         * @param instant An instantaneous point on the time-line.
         * @param zoneId Time zone
         * @param offset 0 : current period, 1 : next period, -1 : pre period
         * @return Time intervals based on natural time cycles
         */
        abstract Interval of(Instant instant, ZoneId zoneId, int offset);
    }

    /**
     * Database scope.
     */
    private final Scope scope;

    /**
     * Table name.
     */
    private final String tableName;

    /**
     * Partition definition rule.
     */
    private final PartitioningDefinition partitioningDefinition;

    /**
     * Cycle period.
     */
    private final Cycle cycle;

    /**
     * Preserved partitions.
     */
    private final Supplier<List<String>> preserveSupplier;

    /**
     * Pre created partition cycle.
     */
    private final Supplier<Duration> preCreateSupplier;

    /**
     * Duration of partition retention.
     */
    private final Supplier<Duration> retentionSupplier;

    /**
     * Naming rules for partitions (formatted based on the start time of the
     * partition).
     */
    private final Function<Interval, String> partitionNameFormatter;

    /**
     * The generation rules for partition values are converted to subtle by default.
     */
    private Function<Instant, Long> partitionValueFormatter = TimeUtils::toUs;

    /**
     * The time zone used when creating partitions, defaults to UTC time zone.
     */
    private final ZoneOffset zoneOffset;

    @lombok.Builder(builderClassName = "Builder")
    public NaturalTimePartitionRolloverPolicy(@NonNull Scope scope, @NonNull String tableName,
            @NonNull Cycle cycle, @NonNull String partitionColumn, @NonNull ZoneOffset zoneOffset,
            @NonNull Supplier<Duration> preCreate, @NonNull Supplier<Duration> retention,
            @NonNull Function<Interval, String> partitionNameFormatter, Supplier<List<String>> preserves) {
        this.scope = scope;
        this.tableName = tableName;
        this.partitioningDefinition = PartitioningDefinition.builder()
                .partitioningColumn(partitionColumn)
                .partitioningType(PartitioningType.RANGE)
                .build();
        this.cycle = cycle;
        this.zoneOffset = zoneOffset;
        this.preCreateSupplier = preCreate;
        this.retentionSupplier = retention;
        this.partitionNameFormatter = partitionNameFormatter;
        this.preserveSupplier = preserves;
    }

    public void setPartitionValueFormatter(Function<Instant, Long> formatter) {
        if (formatter == null) {
            throw new IllegalArgumentException("Invalid partition value formatter");
        }
        this.partitionValueFormatter = formatter;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scope", scope)
                .add("table", tableName)
                .toString();
    }

    @Override
    public List<Partition> claimPartitions(Context context) {
        List<Partition> expectations = new ArrayList<>();
        if (preserveSupplier != null) {
            List<String> preserve = preserveSupplier.get();
            if (preserve != null && preserve.size() > 0) {
                for (String partitionName : preserve) {
                    Partition partition = context.getPartition(partitionName);
                    if (partition == null) {
                        log.warn("Preserving partition '{}' not exists in table '{}'", partitionName, tableName);
                    } else {
                        expectations.add(partition);
                    }
                }
            }
        }
        expectations.addAll(partitionsBefore(context));
        expectations.add(partitionCurrent(context));
        expectations.addAll(partitionsHereafter(context));
        return expectations;
    }

    private String partitionName(Interval interval) {
        String name;
        try {
            name = partitionNameFormatter.apply(interval);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Can't format partition name: table=%s, start=%s, cycle=%s",
                    tableName, toUtcString(interval.start), cycle.name()), e);
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException(
                    String.format("Partition name require non-blank: table=%s, start=%s, cycle=%s", tableName,
                            toUtcString(interval.start), cycle.name()));
        }
        return name;
    }

    private List<Object> partitionValues(Interval interval) {
        return listOf(partitionValueFormatter.apply(interval.end));
    }

    private Partition createPartitionFor(Interval interval) {
        return Partition.builder()
                .name(partitionName(interval))
                .partitionValues(partitionValues(interval))
                .build();
    }

    private Duration validateRetention() {
        Duration duration = retentionSupplier.get();
        Validate.notNull(duration, "Partition retention require non-null: table=%s", tableName);
        if (duration.compareTo(MAX_RETENTION) > 0) {
            throw new IllegalArgumentException(
                    String.format("Exceed max retention duration: table=%s, value=%s, max=%s",
                            tableName, DurationStyle.SIMPLE.print(duration),
                            DurationStyle.SIMPLE.print(MAX_RETENTION)));
        }
        if (duration.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    String.format("Retention require positive: table=%s, value=%s",
                            tableName, DurationStyle.SIMPLE.print(duration)));
        }
        return duration;
    }

    private Duration validatePreCreate() {
        Duration duration = preCreateSupplier.get();
        Validate.notNull(duration, "Partition pre-create require non-null: table=%s", tableName);
        if (duration.compareTo(MAX_PRE_CREATE) > 0) {
            throw new IllegalArgumentException(
                    String.format("Exceed max pre-create duration: table=%s, value=%s, max=%s",
                            tableName, DurationStyle.SIMPLE.print(duration),
                            DurationStyle.SIMPLE.print(MAX_PRE_CREATE)));
        }
        if (duration.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    String.format("Pre-create require positive: table=%s, value=%s",
                            tableName, DurationStyle.SIMPLE.print(duration)));
        }
        return duration;
    }

    /**
     *
     * Partitions that should still be retained before the current time.
     */
    private List<Partition> partitionsBefore(Context context) {
        Duration retention = validateRetention();
        List<Partition> partitions = new ArrayList<>();
        for (int offset = -1;; offset--) {
            Interval interval = cycle.of(context.now(), zoneOffset, offset);
            if (context.now().compareTo(interval.end.plus(retention)) > 0) {
                break;
            } else {
                partitions.add(0, createPartitionFor(interval));
            }
        }
        return partitions;
    }

    /**
     * Partition of the current time.
     */
    private Partition partitionCurrent(Context context) {
        Interval interval = cycle.of(context.now(), zoneOffset, 0);
        return createPartitionFor(interval);
    }

    /**
     * Partitions that need to be pre created after the current time.
     */
    private List<Partition> partitionsHereafter(Context context) {
        Duration preCreate = validatePreCreate();
        List<Partition> partitions = new ArrayList<>();
        for (int offset = 1;; offset++) {
            Interval interval = cycle.of(context.now(), zoneOffset, offset);
            if (context.now().plus(preCreate).compareTo(interval.start) >= 0) {
                partitions.add(createPartitionFor(interval));
            } else {
                break;
            }
        }
        return partitions;
    }
}
