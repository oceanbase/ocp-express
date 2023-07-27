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

import static com.google.common.collect.Lists.newArrayList;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oceanbase.ocp.common.util.time.Interval;
import com.oceanbase.ocp.monitor.config.MonitorDataRolloverProperties;
import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.partitioning.policy.NaturalTimePartitionRolloverPolicy;
import com.oceanbase.ocp.partitioning.policy.PartitionRolloverPolicy.Scope;

@Configuration
public class MonitorPartitionRolloverConfiguration {

    @Autowired
    private MonitorDataRolloverProperties properties;

    private NaturalTimePartitionRolloverPolicy buildDailyDaemon(String table,
            Function<OffsetDateTime, String> formatter, Function<Instant, Long> valueFormatter, int retentionDays) {
        ZoneOffset zoneOffset = OffsetTime.now().getOffset();
        Function<Interval, String> nameFormatter = ((Function<Interval, Instant>) interval -> interval.end)
                .andThen(instant -> instant.atOffset(zoneOffset))
                .andThen(formatter);
        NaturalTimePartitionRolloverPolicy partitioningDaemon = NaturalTimePartitionRolloverPolicy.builder()
                .scope(Scope.Metadb)
                .tableName(table)
                .partitionColumn("timestamp")
                .cycle(NaturalTimePartitionRolloverPolicy.Cycle.DAILY)
                .zoneOffset(zoneOffset)
                .preCreate(() -> Duration.ofDays(MonitorConstants.PARTITION_CREATE_AHEAD_DAYS))
                .retention(() -> Duration.ofDays(retentionDays))
                .preserves(() -> newArrayList("DUMMY"))
                .partitionNameFormatter(nameFormatter)
                .build();
        if (valueFormatter != null) {
            partitioningDaemon.setPartitionValueFormatter(valueFormatter);
        }
        return partitioningDaemon;
    }

    @Bean(MonitorConstants.TABLE_NAME_SECOND_DATA)
    public NaturalTimePartitionRolloverPolicy pdForSecondData() {
        Function<OffsetDateTime, String> formatter = o -> "p" + LocalDateTime
                .ofInstant(o.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return buildDailyDaemon(MonitorConstants.TABLE_NAME_SECOND_DATA, formatter, Instant::getEpochSecond,
                properties.getSecondDataRetentionDays());
    }

    @Bean(MonitorConstants.TABLE_NAME_MINUTE_DATA)
    public NaturalTimePartitionRolloverPolicy pdForMinuteData() {
        Function<OffsetDateTime, String> formatter = o -> "p" + LocalDateTime
                .ofInstant(o.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return buildDailyDaemon(MonitorConstants.TABLE_NAME_MINUTE_DATA, formatter, Instant::getEpochSecond,
                properties.getMinuteDataRetentionDays());
    }

}
