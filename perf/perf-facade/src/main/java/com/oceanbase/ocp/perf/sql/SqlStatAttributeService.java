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

import com.oceanbase.ocp.perf.sql.model.AttributeOperation;
import com.oceanbase.ocp.perf.sql.model.MeasureUnit;
import com.oceanbase.ocp.perf.sql.model.SqlAuditStatSummaryAttribute;

import lombok.Getter;
import lombok.NoArgsConstructor;

public interface SqlStatAttributeService {

    SqlStatAttributeSpec getSqlStatAttribute(String attribute);

    @Getter
    @NoArgsConstructor
    public static final class SqlStatAttributeSpec {

        public String name;
        public String titleKey;
        public String tooltipKey;
        public MeasureUnit unit;
        public TopSqlView topSqlView;
        public JavaDataType javaDataType;

        @Getter
        @NoArgsConstructor

        public static final class TopSqlView {

            public SqlAuditStatSummaryAttribute.Group group;
            public AttributeOperation operation;
            public boolean displayByDefault;
            public boolean displayAlways;
            public boolean allowSearch;
        }

    }

    public enum JavaDataType {

        DOUBLE() {

            @Override
            public <T, R> R detect(Detector<T, R> detector, T t) {
                return detector.onDouble(t);
            }
        },
        STRING() {

            @Override
            public <T, R> R detect(Detector<T, R> detector, T t) {
                return detector.onString(t);
            }
        },
        LONG() {

            @Override
            public <T, R> R detect(Detector<T, R> detector, T t) {
                return detector.onLong(t);
            }
        },
        BOOLEAN() {

            @Override
            public <T, R> R detect(Detector<T, R> detector, T t) {
                return detector.onBoolean(t);
            }
        };

        public interface Detector<T, R> {

            R onDouble(T t);

            R onString(T t);

            R onBoolean(T t);

            R onLong(T t);
        }

        public abstract <T, R> R detect(Detector<T, R> detector, T t);
    }

    @Getter
    @NoArgsConstructor
    public static final class PlanStatAttributeSpec {

        public String name;
        public String titleKey;
        public String tooltipKey;
        public MeasureUnit unit;
        public JavaDataType javaDataType;

    }
}
