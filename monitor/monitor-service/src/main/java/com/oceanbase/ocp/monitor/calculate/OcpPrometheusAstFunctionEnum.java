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
package com.oceanbase.ocp.monitor.calculate;

public enum OcpPrometheusAstFunctionEnum {

    /**
     * Functions of prometheus ast.
     */
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    ADD_L_FILL_0_IF_ABSENT("L_FILL_0_ADD"),
    ADD_R_FILL_0_IF_ABSENT("R_FILL_0_ADD"),
    ADD_FILL_0_IF_ABSENT("FILL_0_ADD"),

    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    NE("<>"),
    EQ("=="),

    ABS("abs"),
    RATE("rate"),
    DELTA("delta"),
    ROUND("round"),
    SUM("sum"),
    COUNT("count"),
    AVG("avg"),
    MIN("min"),
    MAX("max");

    private final String function;

    OcpPrometheusAstFunctionEnum(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }
}
