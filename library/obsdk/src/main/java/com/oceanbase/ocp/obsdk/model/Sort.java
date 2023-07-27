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

package com.oceanbase.ocp.obsdk.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Sort option for queries. See {@link org.springframework.data.domain.Sort}.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sort {

    private final List<Order> orders;

    public static Sort unsorted() {
        return new Sort(Collections.emptyList());
    }

    public static Sort by(Order... orders) {
        Validate.notNull(orders, "orders should not be null");
        return orders.length == 0 ? unsorted() : new Sort(Arrays.asList(orders));
    }

    public static Sort by(List<Order> orders) {
        Validate.notNull(orders, "orders should not be null");
        return orders.isEmpty() ? unsorted() : new Sort(orders);
    }

    public static Sort by(Direction direction, String... properties) {
        Validate.notNull(direction, "direction should not be null");
        Validate.notEmpty(properties, "properties should not be empty");
        List<Order> orders = Arrays.stream(properties)
                .map(it -> new Order(it, direction))
                .collect(Collectors.toList());
        return new Sort(orders);
    }

    public boolean isSorted() {
        return !isUnsorted();
    }

    public boolean isUnsorted() {
        return orders.isEmpty();
    }

    public enum Direction {

        ASC,
        DESC,
        ;

        public boolean isAscending() {
            return this == ASC;
        }

        public boolean isDescending() {
            return this == DESC;
        }
    }

    @Data
    @AllArgsConstructor
    public static class Order {

        private String property;
        private Direction direction;

        public static Order asc(String property) {
            return new Order(property, Direction.ASC);
        }

        public static Order desc(String property) {
            return new Order(property, Direction.DESC);
        }
    }
}
