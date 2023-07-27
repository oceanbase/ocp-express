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

package com.oceanbase.ocp.perf.sql.param;

import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.validateInterval;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.common.util.time.Interval;
import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.perf.sql.SqlAuditRawStatService;
import com.oceanbase.ocp.perf.sql.SqlStatAttributeService;
import com.oceanbase.ocp.perf.sql.model.SqlAuditStatBase;
import com.oceanbase.ocp.perf.sql.model.SqlAuditStatSummary;
import com.oceanbase.ocp.perf.sql.model.SqlInfo;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class QueryTopSqlParam {

    @NonNull
    public final Interval interval;

    @NonNull
    public final Long tenantId;

    public final String tenantName;

    public final String server;

    public final Boolean inner;

    public final String sqlText;

    public final Search search;

    public final Long limit;

    public Integer sqlTextLength;

    public String filterExpression;

    public boolean parseSqlType;

    public boolean parseStatement = false;

    public boolean parseTable = false;

    public Boolean groupByServer;

    @lombok.Builder(builderClassName = "Builder")
    public QueryTopSqlParam(OffsetDateTime startTime, OffsetDateTime endTime,
            Long tenantId, String tenantName, String server, Boolean inner, String sqlText, Search search, Long limit,
            Integer sqlTextLength, Boolean groupByServer,
            String filterExpression, boolean parseSqlType, boolean parseStatement) {
        this.interval = validateInterval(startTime, endTime);
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.server = server;
        this.inner = inner != null && inner;
        this.sqlText = sqlText;
        this.search = search;
        this.limit = limit;
        this.sqlTextLength = sqlTextLength;
        this.groupByServer = groupByServer == null || groupByServer;
        this.filterExpression = filterExpression;
        this.parseSqlType = parseSqlType;
        this.parseStatement = parseStatement;
    }

    public static final class Search {

        @NonNull
        public String attribute;

        @NonNull
        public String operator;

        @NonNull
        public String value;

        @lombok.Builder(builderClassName = "Builder")
        Search(String attribute, String operator, String value) {
            this.attribute = attribute.trim();
            this.operator = operator.trim();
            this.value = value.trim();
        }

        public boolean searchSqlId() {
            return "sqlId".equals(attribute) && "EQ".equalsIgnoreCase(operator) && StringUtils.isNotEmpty(value);
        }

        public Predicate<SqlAuditStatSummary> createFilter(SqlAuditRawStatService service) {
            SqlStatAttributeService.SqlStatAttributeSpec spec =
                    service.getSqlStatAttributeService().getSqlStatAttribute(attribute);
            if (spec == null || spec.topSqlView == null || !spec.topSqlView.allowSearch) {
                throw new IllegalArgumentException(ErrorCodes.PERF_SQL_SEARCH_ATTR_INVALID, attribute);
            }
            Field f = getField();
            Operator op = Operator.from(operator);
            if (op == null) {
                throw new IllegalArgumentException(ErrorCodes.PERF_SQL_SEARCH_OP_INVALID, operator);
            }
            Object expected;
            try {
                expected =
                        spec.javaDataType.detect(new SqlStatAttributeService.JavaDataType.Detector<Object, Object>() {

                            @Override
                            public Object onDouble(Object o) {
                                return Double.parseDouble(String.valueOf(o));
                            }

                            @Override
                            public Object onString(Object o) {
                                return String.valueOf(o);
                            }

                            @Override
                            public Object onBoolean(Object o) {
                                return Boolean.parseBoolean(String.valueOf(o));
                            }

                            @Override
                            public Object onLong(Object o) {
                                return Long.parseLong(String.valueOf(o));
                            }
                        }, value);
            } catch (Exception e) {
                throw new IllegalArgumentException(ErrorCodes.PERF_SQL_SEARCH_VALUE_INVALID, value);
            }
            return (model) -> {
                Object value;
                try {
                    f.setAccessible(true);
                    value = f.get(model);
                } catch (IllegalAccessException e) {
                    log.error(String.format("Error when retrieve model value: model=%s, attr=%s, type=%s", model,
                            attribute, spec.javaDataType), e);
                    return false;
                }
                if (value == null) {
                    return false;
                }
                try {
                    return op.test(spec.javaDataType, value, expected);
                } catch (Exception e) {
                    log.error(String.format("Error when comparing model value: model=%s, attr=%s, type=%s, op=%s, val=",
                            attribute, spec.javaDataType.name(), op, value), e);
                    return false;
                }
            };
        }

        private Field getField() {
            Field f;
            try {
                f = SqlAuditStatSummary.class.getDeclaredField(attribute);
            } catch (Exception e0) {
                try {
                    f = SqlAuditStatBase.class.getDeclaredField(attribute);
                } catch (Exception e1) {
                    log.error("Error while get reflect.Field of attribute: " + attribute, e1);
                    throw new IllegalArgumentException(ErrorCodes.PERF_SQL_SEARCH_ATTR_INVALID, attribute);
                }
            }
            return f;
        }
    }

    public Predicate<SqlInfo> createSqlTextFilter() {
        return (it) -> isBlank(sqlText)
                || (it != null && isNoneBlank(it.sqlText) && StringUtils.containsIgnoreCase(it.sqlText, sqlText));
    }

    public static final Predicate<SqlAuditStatSummary> PREDICATE_TRUE = (it) -> true;
    public static final Predicate<SqlAuditStatSummary> PREDICATE_FALSE = (it) -> false;

    public Predicate<SqlAuditStatSummary> createSearchFilter(SqlAuditRawStatService service) {
        if (search == null) {
            return PREDICATE_TRUE;
        }
        return search.createFilter(service);
    }

    private enum Operator {

        /**
         * Operators.
         */
        EQ() {

            @Override
            boolean test(SqlStatAttributeService.JavaDataType dataType, Object value,
                    Object expected) {
                return value.equals(expected);
            }
        },

        NE() {

            @Override
            boolean test(SqlStatAttributeService.JavaDataType dataType, Object value,
                    Object expected) {
                return !value.equals(expected);
            }
        },

        GT() {

            @Override
            boolean test(SqlStatAttributeService.JavaDataType dataType, Object value,
                    Object expected) {
                switch (dataType) {
                    case BOOLEAN:
                        return false;
                    case STRING:
                        return ((String) value).compareTo((String) expected) > 0;
                    case DOUBLE:
                        return ((Double) value).compareTo((Double) expected) > 0;
                    case LONG:
                        return ((Long) value).compareTo((Long) expected) > 0;
                    default:
                        throw new IllegalStateException("Unreachable: GT");
                }
            }
        },

        GE() {

            @Override
            boolean test(SqlStatAttributeService.JavaDataType dataType, Object value,
                    Object expected) {
                switch (dataType) {
                    case BOOLEAN:
                        return false;
                    case STRING:
                        return ((String) value).compareTo((String) expected) >= 0;
                    case DOUBLE:
                        return ((Double) value).compareTo((Double) expected) >= 0;
                    case LONG:
                        return ((Long) value).compareTo((Long) expected) >= 0;
                    default:
                        throw new IllegalStateException("Unreachable: GE");
                }
            }
        },

        LT() {

            @Override
            boolean test(SqlStatAttributeService.JavaDataType dataType, Object value,
                    Object expected) {
                switch (dataType) {
                    case BOOLEAN:
                        return false;
                    case STRING:
                        return ((String) value).compareTo((String) expected) < 0;
                    case DOUBLE:
                        return ((Double) value).compareTo((Double) expected) < 0;
                    case LONG:
                        return ((Long) value).compareTo((Long) expected) < 0;
                    default:
                        throw new IllegalStateException("Unreachable: LT");
                }
            }
        },

        LE() {

            @Override
            boolean test(SqlStatAttributeService.JavaDataType dataType, Object value,
                    Object expected) {
                switch (dataType) {
                    case BOOLEAN:
                        return false;
                    case STRING:
                        return ((String) value).compareTo((String) expected) <= 0;
                    case DOUBLE:
                        return ((Double) value).compareTo((Double) expected) <= 0;
                    case LONG:
                        return ((Long) value).compareTo((Long) expected) <= 0;
                    default:
                        throw new IllegalStateException("Unreachable: LE");
                }
            }
        };

        abstract boolean test(SqlStatAttributeService.JavaDataType dataType, Object value, Object expected);

        static Operator from(String name) {
            if (isBlank(name)) {
                return null;
            } else if ("EQ".equals(name) || "eq".equalsIgnoreCase(name) || "=".equals(name)) {
                return EQ;
            } else if ("NE".equals(name) || "ne".equalsIgnoreCase(name) || "<>".equals(name) || "!=".equals(name)) {
                return NE;
            } else if ("GT".equals(name) || "gt".equalsIgnoreCase(name) || ">".equals(name)) {
                return GT;
            } else if ("GE".equals(name) || "ge".equalsIgnoreCase(name) || ">=".equals(name)) {
                return GE;
            } else if ("LT".equals(name) || "lt".equalsIgnoreCase(name) || "<".equals(name)) {
                return LT;
            } else if ("LE".equals(name) || "le".equalsIgnoreCase(name) || "<=".equals(name)) {
                return LE;
            }
            return null;
        }
    }
}
