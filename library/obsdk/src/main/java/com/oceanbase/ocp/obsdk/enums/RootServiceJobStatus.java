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

package com.oceanbase.ocp.obsdk.enums;

public enum RootServiceJobStatus {

    SUCCESS("SUCCESS"), FAILED("FAILED"), INPROGRESS("INPROGRESS"), UNKNOWN("UNKNOWN");

    private String value;

    RootServiceJobStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static RootServiceJobStatus fromValue(String text) {
        for (RootServiceJobStatus b : RootServiceJobStatus.values()) {
            if (b.value.equals(text) || b.name().equals(text)) {
                return b;
            }
        }
        return UNKNOWN;
    }
}
