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

package com.oceanbase.ocp.obsdk.util;

public class UnitUtils {

    public static String formatUnitConfig(double minCpu, double maxCpu, long minMemory, long maxMemory) {
        return formatCpu(maxCpu) + formatMemory(maxMemory);
    }

    private static String formatCpu(double cpu) {
        if (cpu % 1 == 0) {
            return String.format("%.0fC", cpu);
        } else {
            return String.format("%.1fC", cpu);
        }
    }

    private static String formatMemory(long memory) {
        return String.format("%dG", memory / 1073741824L);
    }
}
