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
package com.oceanbase.ocp.obops.host.model;

import lombok.Getter;

public enum ExporterType {

    HOST("host"),

    OB("ob");

    @Getter
    private final String type;

    ExporterType(String type) {
        this.type = type;
    }

    public static ExporterType typeOf(String value) {
        for (ExporterType t : values()) {
            if (t.getType().equalsIgnoreCase(value)) {
                return t;
            }
        }
        return null;
    }

}
