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
package com.oceanbase.ocp.core.util;

import java.util.List;
import java.util.stream.Collectors;

import com.oceanbase.ocp.obsdk.model.Sort;
import com.oceanbase.ocp.obsdk.model.Sort.Direction;
import com.oceanbase.ocp.obsdk.model.Sort.Order;

/**
 * Convert spring pagination object to OB-SDK pagination object.
 */
public class PageableUtils {

    public static Sort buildSort(org.springframework.data.domain.Sort sort) {
        List<Order> orders = sort.stream()
                .map(PageableUtils::buildOrder)
                .collect(Collectors.toList());
        return Sort.by(orders);
    }

    private static Order buildOrder(org.springframework.data.domain.Sort.Order order) {
        Direction direction = order.isAscending() ? Direction.ASC : Direction.DESC;
        String property = order.getProperty();
        return new Order(property, direction);
    }

}
