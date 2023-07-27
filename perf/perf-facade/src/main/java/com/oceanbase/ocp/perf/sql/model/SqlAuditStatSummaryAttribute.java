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

package com.oceanbase.ocp.perf.sql.model;

import lombok.Data;

@Data
public class SqlAuditStatSummaryAttribute {

    private int ordinal;

    private String name;

    private String title;

    private String tooltip;

    private MeasureUnit unit;

    private Group group;

    private AttributeOperation operation;

    private boolean displayByDefault;

    private boolean displayAlways;

    private boolean allowSearch;

    private AttributeDataType dataType;

    public enum Group {
        FAVORITE,
        BASIC,
        TIME_STAT,
        PLAN_STAT
    }
}
