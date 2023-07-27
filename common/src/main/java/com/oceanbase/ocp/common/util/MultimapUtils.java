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

import java.util.function.Function;
import java.util.stream.Collector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

public class MultimapUtils {

    /**
     * Returns a {@code Collector} that grouping elements according to a
     * classification function, into a {@code ListMultimap}.
     *
     * <p>
     * The classification function maps elements to some key type {@code K}. The
     * collector produces a {@code ListMultimap<K, T>} whose keys are the result of
     * applying the provided classification function to the input elements, and
     * whose values are the input elements themselves.
     *
     * @param <E> the type of the input elements
     * @param <K> the type of the keys
     * @param classifier the classifier function mapping input elements to keys
     * @return a {@code Collector} which grouping elements into a
     *         {@code ListMultimap}
     */
    @SuppressWarnings("UnstableApiUsage")
    public static <E, K> Collector<E, ?, ListMultimap<K, E>> groupingBy(Function<E, K> classifier) {
        return Multimaps.toMultimap(classifier, Function.identity(), ArrayListMultimap::create);
    }
}
