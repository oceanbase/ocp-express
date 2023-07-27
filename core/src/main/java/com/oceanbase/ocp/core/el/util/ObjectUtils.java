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

package com.oceanbase.ocp.core.el.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectUtils {

    public static Map<String, Object> getFieldMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clz = obj.getClass();
        while (!clz.equals(Object.class)) {
            Arrays.stream(clz.getDeclaredFields())
                    .forEach(field -> {
                        try {
                            field.setAccessible(true);
                            map.put(field.getName(), field.get(obj));
                        } catch (IllegalAccessException e) {
                            log.error("Error in getting access to field:{}", field);
                        }
                    });
            clz = clz.getSuperclass();
        }
        return map;
    }
}
