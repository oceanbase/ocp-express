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

package com.oceanbase.ocp.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MathUtils {

    public static Double stdOfDoubleArray(Object nums) {
        return Math.sqrt(varOfDoubleArray(nums));
    }

    public static Double stdOfDoubleList(List nums) {
        return stdOfDoubleArray(nums.toArray());
    }

    public static Double stdOfDoubleSet(Set nums) {
        return stdOfDoubleArray(nums.toArray());
    }

    public static Object varOfDoubleList(List value) {
        return varOfDoubleArray(value.toArray());
    }

    public static Object varOfDoubleSet(Set value) {
        return varOfDoubleArray(value.toArray());
    }

    public static Double varOfDoubleArray(Object nums) {
        if (((Object[]) nums).length == 0) {
            return 0.0D;
        }
        double avg = Arrays.stream((Object[]) nums).map(a -> (Double) a).reduce(0.0D, Double::sum)
                / ((Object[]) nums).length;
        return Arrays.stream(((Object[]) nums))
                .map(a -> ((Double) a - avg) * ((Double) a - avg) / (((Object[]) nums).length - 1))
                .reduce(0.0D, Double::sum);
    }

}
