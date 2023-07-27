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
package com.oceanbase.ocp.monitor.constants;

import lombok.Getter;

/**
 * @description Exporter type
 */
@Getter
public enum ExporterType {

    /**
     * Collect host metrics
     */
    HOST_MONITOR("HOST_MONITOR"),

    /**
     * Collect ob cluster metrics
     */
    OB_CLUSTER("OB_CLUSTER");

    private final String value;

    ExporterType(String value) {
        this.value = value;
    }

}
