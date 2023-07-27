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

package com.oceanbase.ocp.bootstrap.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceUtils {

    private static final ConcurrentHashMap<Class<?>, ServiceLoader<?>> SERVICE_LOADERS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadServices(Class<T> cls) {
        ServiceLoader<T> loader = (ServiceLoader<T>) SERVICE_LOADERS.computeIfAbsent(cls, ServiceLoader::load);
        List<T> ret = new ArrayList<>();
        loader.forEach(ret::add);
        return ret;
    }
}
