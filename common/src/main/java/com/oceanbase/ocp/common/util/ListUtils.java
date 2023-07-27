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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.common.lang.Pair;

public final class ListUtils {

    public static <T> List<T> listOf(T... values) {
        if (values == null) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }

    public static <E, K> List<E> removeDuplicate(List<E> list, Function<E, K> keyExtractor) {
        Map<K, E> map = new HashMap<>();
        for (E item : list) {
            K key = keyExtractor.apply(item);
            if (!map.containsKey(key)) {
                map.put(key, item);
            }
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Split list to several list.
     *
     * @param list original list
     * @param num sublist count
     * @param <T> type of list item
     * @return split list
     */
    public static <T> List<List<T>> split(List<T> list, int num) {
        Validate.isTrue(num >= 1, "num should be equal to or greater than 1");
        Validate.notNull(list);
        List<List<T>> result = new ArrayList<>(num);
        int i = 0;
        while (i < list.size()) {
            int idx = i % num;
            if (result.size() < idx + 1) {
                result.add(new ArrayList<>());
            }
            result.get(idx).add(list.get(i++));
        }
        return result;
    }


    public static <A, B> List<B> transform(List<A> fromList, BiFunction<Integer, A, B> function) {
        Validate.notNull(fromList);
        if (fromList.size() == 0) {
            return Collections.emptyList();
        }
        List<B> toList = new ArrayList<>();
        for (int i = 0; i < fromList.size(); i++) {
            toList.add(function.apply(i, fromList.get(i)));
        }
        return toList;
    }

    /**
     * Cartesian list of two lists.
     */
    public static <A, B> List<Pair<A, B>> cartesianProduct(List<? extends A> list1, List<? extends B> list2) {
        List<Pair<A, B>> result = new ArrayList<>();
        for (A a : list1) {
            for (B b : list2) {
                result.add(Pair.of(a, b));
            }
        }
        return result;
    }

}
