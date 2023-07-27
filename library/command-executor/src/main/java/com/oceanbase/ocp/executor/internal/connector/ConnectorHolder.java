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

package com.oceanbase.ocp.executor.internal.connector;

import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

import lombok.extern.slf4j.Slf4j;

/**
 * Connector holder, cache recently used connectors.
 */
@Slf4j
public class ConnectorHolder {

    private final Cache<ConnectorKey, Connector<?>> cache;

    ConnectorHolder(long cacheSize, long maxIdleSeconds) {
        cache = Caffeine.newBuilder()
                .expireAfterAccess(maxIdleSeconds, TimeUnit.SECONDS)
                .maximumSize(cacheSize)
                .writer(new ConnectorCacheWriter()).build();
    }

    Connector<?> put(Connector<?> connector) {
        ConnectorKey connectorKey = connector.cacheKey();
        Connector<?> connectorForRet = cache.get(connectorKey, key -> connector);
        log.info("[ConnectorHolder]: connector put to cache, key={}, estimated size:{}", connectorKey,
                cache.estimatedSize());
        return connectorForRet;
    }

    Connector<?> get(ConnectorKey connectorKey) {
        Connector<?> connector = cache.getIfPresent(connectorKey);
        if (connector != null) {
            if (!connector.isAlive()) {
                log.warn("[ConnectorHolder]:connector from cache is not alive, cache key:{}.", connectorKey);
                cache.invalidate(connectorKey);
                connector = null;
            } else {
                log.debug("[ConnectorHolder]:get  connector from cache, cache key:{}.", connectorKey);
            }
        } else {
            log.debug("[ConnectorHolder]: no  connector found in cache, cache key:{}.", connectorKey);
        }
        return connector;
    }

    private static class ConnectorCacheWriter implements CacheWriter<ConnectorKey, Connector<?>> {

        @Override
        public void write(@NonNull ConnectorKey key, @NonNull Connector<?> value) {
            // do nothing
        }

        @Override
        public void delete(@NonNull ConnectorKey key, @Nullable Connector<?> connector, @NonNull RemovalCause cause) {
            if (RemovalCause.REPLACED == cause) {
                throw new RuntimeException(
                        "[ConnectorHolder]:RemovalCause.REPLACED detected, the connector in cache can not be replaced!");
            }
            if (RemovalCause.SIZE == cause) {
                log.warn("[ConnectorHolder]:the cache of holder is full");
            }
            log.info("[ConnectorHolder]:connector is evicted from holder's cache. cause={}, key={}", cause,
                    key);
            if (connector != null) {
                connector.close();
            }
        }
    }

}
