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

package com.oceanbase.ocp.perf.sql;

import static com.oceanbase.ocp.core.i18n.ErrorCodes.PERF_SQL_EXCEED_MAX_TIME_RANGE;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.util.time.Interval;
import com.oceanbase.ocp.core.property.PropertyService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RefreshScope
@Getter
@Setter
public class SqlStatProperties {

    @Autowired
    private PropertyService propertyService;

    @Value("${ocp.perf.sql.inner-sql-threshold:0.5}")
    private double innerSqlThreshold = 0.5D;

    @Value("${ocp.perf.sql.query-timeout:30000000}")
    private int queryTimeout = 30_000_000;

    @Value("#{T(org.springframework.boot.convert.DurationStyle).SIMPLE.parse(\"${ocp.perf.sql.max-query-range:7d}\")}")
    private Duration maxQueryRange = Duration.ofDays(7);

    @Value("${ocp.perf.sql.top-sql-limit:200}")
    private long topSqlLimit = 200;

    @Value("${ocp.perf.sql.slow-sql-limit:2000}")
    private int slowSqlLimit = 2000;

    @Value("${display-parse-failed-sql:true}")
    private boolean displayParseFailedSql = true;

    @Value("#{T(org.springframework.boot.convert.DurationStyle).SIMPLE.parse(\"${ocp.perf.sql.top-plan-max-query-range:7d}\")}")
    private Duration topPlanMaxQueryRange = Duration.ofDays(7);

    public void checkTopPlanMaxQueryRange(Interval interval) {
        if (interval.duration().compareTo(topPlanMaxQueryRange) > 0) {
            throw PERF_SQL_EXCEED_MAX_TIME_RANGE.exception(topPlanMaxQueryRange);
        }
    }
}
