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

package com.oceanbase.ocp.bootstrap.core.def;

import java.util.Objects;

public class Const {

    public static final Const NULL = new Const("NULL"),
            CURRENT_TIMESTAMP = new Const("CURRENT_TIMESTAMP"),
            CURRENT_TIMESTAMP_6 = new Const("CURRENT_TIMESTAMP(6)"),
            MAXVALUE = new Const("MAXVALUE");
    private final String value;

    Const(String value) {
        this.value = value.toUpperCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Const valueOf(String value) {
        return new Const(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Const aConst = (Const) o;
        return value.equals(aConst.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
