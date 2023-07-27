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

package com.oceanbase.ocp.obsdk.accessor;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.oceanbase.ocp.obsdk.connector.ConnectProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Holder for ob accessor
 *
 */
@Slf4j
class ObAccessorHolder {

    private long cacheSize = 200;
    private long maxIdleSeconds = 7200;
    private Cache<ConnectProperties, ObAccessor> cache;

    ObAccessorHolder() {
        cache = Caffeine.newBuilder()
                .expireAfterAccess(maxIdleSeconds, TimeUnit.SECONDS)
                .maximumSize(cacheSize)
                .build();
    }

    /**
     * put ObAccessor in cache
     *
     * @param key {@link ConnectProperties}
     * @param obAccessor {@link ObAccessor}
     */
    void put(ConnectProperties key, ObAccessor obAccessor) {
        cache.put(key, obAccessor);
    }

    /**
     * get ObAccessor from cache
     *
     * @param key {@link ConnectProperties}
     * @return {@link ObAccessor}
     */
    ObAccessor get(ConnectProperties key) {
        return cache.getIfPresent(key);
    }
}
