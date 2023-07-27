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

package com.oceanbase.ocp.perf.sql.util;

import static com.oceanbase.ocp.common.util.time.TimeUtils.toLocalString;
import static com.oceanbase.ocp.common.util.time.TimeUtils.toUs;
import static com.oceanbase.ocp.common.util.time.TimeUtils.toUtcString;
import static com.oceanbase.ocp.core.i18n.ErrorCodes.COMMON_START_END_TIME_NOT_VALID;
import static com.oceanbase.ocp.core.i18n.ErrorCodes.PERF_SQL_PLAN_UID_INVALID;
import static com.oceanbase.ocp.core.util.ExceptionUtils.require;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.apache.commons.lang3.StringUtils.substring;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.boot.convert.DurationStyle;

import com.oceanbase.ocp.common.util.time.Interval;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.obparser.MysqlParsers;
import com.oceanbase.ocp.obparser.OracleParsers;
import com.oceanbase.ocp.obparser.SqlType;
import com.oceanbase.ocp.obsdk.operator.sql.model.PlanUid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SqlStatUtils {

    private static final Pattern SENSITIVE_SQL =
            Pattern.compile(".*(alter|create)\\s+user.*", Pattern.CASE_INSENSITIVE);

    private static final List<Pattern> QUICK_PARSE_SQL_PATTERN = Arrays.asList(Pattern.compile(
            "^begin[\\s\\p{Zs}]*;?",
            Pattern.CASE_INSENSITIVE));

    public static String safeParameterizeSqlLiteral(TenantMode mode, String sql) {
        String stmt = quickParseStatement(sql);
        if (StringUtils.isNotBlank(stmt)) {
            return stmt;
        }
        if (notParsable(sql)) {
            log.debug("Sql {} is not parsable ,skip parse", sql);
            return "";
        }
        try {
            if (mode == TenantMode.MYSQL) {
                return MysqlParsers.parameterizeSql(sql, "?");
            } else {
                return OracleParsers.parameterizeSql(sql, "?");
            }
        } catch (Exception e) {
            log.error(String.format("Parameterize sql error, length=%d, sql=%s", sql.length(),
                    substring(sql, 0, Math.min(256, sql.length()))), e.getMessage());
            return "";
        }
    }

    public static String safeParameterizeSqlLiteral(TenantMode mode, String sql, boolean returnOriginalWhenFailed) {
        String parsedSql = safeParameterizeSqlLiteral(mode, sql);
        return (returnOriginalWhenFailed && StringUtils.isEmpty(parsedSql)) ? sql : parsedSql;
    }

    private static String quickParseStatement(String sql) {
        if (QUICK_PARSE_SQL_PATTERN.stream().anyMatch(p -> p.matcher(sql.trim()).find())) {
            return sql;
        }
        return "";
    }

    public static boolean notParsable(String sql) {
        return !parsable(sql);
    }

    public static boolean parsable(String sql) {
        String trimStr = sql.trim();
        boolean isPl = StringUtils.startsWithIgnoreCase(trimStr, "begin ");
        if (isPl) {
            return false;
        }
        boolean isInsert = StringUtils.startsWithIgnoreCase(trimStr, "insert ");
        boolean isReplace = StringUtils.startsWithIgnoreCase(trimStr, "replace ");
        boolean isTruncate = !StringUtils.endsWithIgnoreCase(trimStr, ")");
        return (!isInsert && !isReplace) || !isTruncate;
    }


    public static String toSimpleString(Duration duration) {
        return DurationStyle.SIMPLE.print(duration);
    }

    public static Interval validateInterval(OffsetDateTime startTime, OffsetDateTime endTime) {
        require(startTime != null && endTime != null, COMMON_START_END_TIME_NOT_VALID);
        try {
            return Interval.of(startTime, endTime);
        } catch (Exception e) {
            throw COMMON_START_END_TIME_NOT_VALID.exception();
        }
    }

    public static PlanUid validatePlanUid(String value) {
        try {
            return PlanUid.from(value);
        } catch (Exception e) {
            throw PERF_SQL_PLAN_UID_INVALID.exception(value);
        }
    }

    public static String info(Interval interval, int indent) {
        StringBuilder sb = new StringBuilder();
        String p = repeat('\t', indent);
        sb.append(p).append("Us\t");
        sb.append(toUs(interval.start)).append(" - ").append(toUs(interval.end)).append("\r\n");
        sb.append(p).append("UTC\t");
        sb.append(toUtcString(interval.start)).append(" - ").append(toUtcString(interval.end)).append("\r\n");
        sb.append(p).append("Lo\t");
        sb.append(toLocalString(interval.start)).append(" - ").append(toLocalString(interval.end));
        return sb.toString();
    }

    public static String info(Instant instant) {
        return toLocalString(instant) + "(" + toUs(instant) + ")";
    }

    public static Double usToMsRound2(Long value) {
        if (value == null) {
            return null;
        } else if (value == 0L) {
            return 0D;
        }
        return Math.round(value.doubleValue() / 10) / 100D;
    }

    public static Double usToMsRound2(BigInteger value) {
        if (value == null) {
            return null;
        } else if (value.longValue() == 0L) {
            return 0D;
        }
        return Math.round(value.doubleValue() / 10) / 100D;
    }

    public static Double avg2(Long value, Long count) {
        if (value == null || count == null) {
            return null;
        } else if (value == 0L || count == 0L) {
            return 0D;
        } else {
            return Math.round((value.doubleValue() / count.doubleValue()) * 100D) / 100D;
        }
    }

    public static Double avg2(Double value, Double count) {
        if (value == null || count == null) {
            return null;
        } else if (value == 0L || count == 0L) {
            return 0D;
        } else {
            return Math.round((value / count) * 100D) / 100D;
        }
    }

    public static Double avg2(Double value) {
        if (value == null) {
            return null;
        } else {
            return Math.round(value * 100D) / 100D;
        }
    }

    public static Double avg2(Long a, Long b, Long count) {
        if (a == null || b == null || count == null) {
            return null;
        } else if (a >= b || count == 0) {
            return 0D;
        } else {
            return avg2(b - a, count);
        }
    }

    public static Double avgPercent(Long value, Long count) {
        if (value == null || count == null) {
            return null;
        } else if (value == 0L || count == 0L) {
            return 0D;
        } else {
            return Math.round((value.doubleValue() * 100 / count.doubleValue()) * 10000D) / 10000D;
        }
    }

    public static Double avgPercent(Long a, Long b, Long count) {
        if (a == null || b == null || count == null) {
            return null;
        } else if (a == 0 || b == 0 || count == 0) {
            return 0D;
        } else {
            return avgPercent(b - a, count);
        }
    }

    public static Double avgUsTime(Long value, Long count) {
        if (value == null || count == null) {
            return null;
        } else if (value == 0L || count == 0L) {
            return 0D;
        }
        return avg2((value.doubleValue() / count) / 1000);
    }

    public static Double avgUsTime(BigInteger value, Long count) {
        if (value == null || count == null) {
            return null;
        } else if (value.longValue() == 0L || count == 0L) {
            return 0D;
        }
        return avg2((value.doubleValue() / count) / 1000);
    }

    public static Double avgUsTime(Long a, Long b, Long count) {
        if (a == null || b == null || count == null) {
            return null;
        } else if (a >= b || count == 0) {
            return 0D;
        }
        return avgUsTime(b - a, count);
    }

    public static Double toDouble(Long v) {
        return v == null ? null : v.doubleValue();
    }

    public static String intern(String value) {
        if (isBlank(value)) {
            return value;
        }
        return value.intern();
    }

    public static boolean isTimeoutException(Exception e) {
        if (e instanceof SQLException) {
            return ((SQLException) e).getErrorCode() == 4012;
        }
        if (e.getCause() instanceof SQLException) {
            return ((SQLException) e.getCause()).getErrorCode() == 4012;
        }
        return false;
    }

    public static SqlType safeParseSqlType(TenantMode mode, String sqlText) {
        Validate.notNull(mode, "Tenant mode require non-null");
        try {
            SqlType type = SqlType.quickParse(sqlText);
            if (type != SqlType.OTHER) {
                return type;
            } else if (mode == TenantMode.MYSQL) {
                return MysqlParsers.parseSqlType(sqlText);
            } else {
                return OracleParsers.parseSqlType(sqlText);
            }
        } catch (Exception e) {
            log.debug(String.format("SQL parse error, length=%d, sql=%s", sqlText.length(),
                    substring(sqlText, 0, 256)), e);
            return SqlType.OTHER;
        }
    }


    public static boolean isSensitive(String sqlText) {
        if (isBlank(sqlText)) {
            return false;
        }
        return SENSITIVE_SQL.matcher(sqlText).matches();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Table {

        private String dbName;
        private String tableName;
    }

}
