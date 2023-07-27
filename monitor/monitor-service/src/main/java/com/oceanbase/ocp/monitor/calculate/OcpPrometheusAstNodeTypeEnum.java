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

public enum OcpPrometheusAstNodeTypeEnum {

    /**
     * Node type of prometheus ast.
     */
    SCALAR("scalar"),
    MATHOP("mathop"),
    COMPARE("compare"),
    ITEM("item"),
    FUNCTION("function"),
    INTERVAL_FUNCTION("interval_function"),
    AGG_FUNCTION("agg_function");

    private String nodeType;

    OcpPrometheusAstNodeTypeEnum(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }
}
