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

package com.oceanbase.ocp.obsdk.connector;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy.Eviction;
import com.github.benmanes.caffeine.cache.Policy.Expiration;
import com.github.benmanes.caffeine.cache.RemovalCause;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ObConnectorHolder {


    private long cacheSize = 100;
    private long maxIdleSeconds = 3600;


    private Cache<ObConnectorKey, ObConnector> cache;

    private List<ObConnector> delayedCloseList;

    ObConnectorHolder() {
        cache = Caffeine.newBuilder()
                .expireAfterAccess(maxIdleSeconds, TimeUnit.SECONDS)
                .maximumSize(cacheSize)
                .writer(new CacheWriter<ObConnectorKey, ObConnector>() {

                    @Override
                    public void write(@NonNull ObConnectorKey key, @NonNull ObConnector obConnector) {

                    }

                    @Override
                    public void delete(@NonNull ObConnectorKey key, ObConnector obConnector,
                            @NonNull RemovalCause cause) {
                        if (RemovalCause.REPLACED == cause) {
                            throw new RuntimeException(
                                    "RemovalCause.REPLACED detected, the ob connector in cache can not be replaced!");
                        }
                        if (RemovalCause.SIZE == cause) {
                            log.warn("[obsdk] the cache of holder is full");
                        }
                        log.info("[obsdk] ob connector is evicted from holder's cache. cause={}, key={}", cause, key);
                        if (obConnector.isActive()) {

                            delayedCloseList.add(obConnector);
                            log.info("[obsdk] the ob connector is still active, put in delayed close list, key={}",
                                    obConnector.key());
                        } else {
                            obConnector.close();
                        }
                    }
                })
                .build();

        delayedCloseList = new CopyOnWriteArrayList<>();
    }


    void put(ObConnector obConnector) {
        ObConnectorKey key = obConnector.key();
        log.debug("[obsdk] put ob connector in holder. key={}", key);
        cache.put(key, obConnector);
    }


    ObConnector getObConnector(ConnectProperties connectProperties) {
        ObConnector obConnector = null;

        if (ConnectionMode.DIRECT.equals(connectProperties.getConnectionMode())
                && CollectionUtils.isNotEmpty(connectProperties.getObsAddrList())) {
            for (ObServerAddr addr : connectProperties.getObsAddrList()) {
                ConnectProperties newConnectProperties =
                        connectProperties.withAddress(addr.getAddress()).withPort(addr.getPort());
                ObConnectorKey key = ObConnectorKey.of(newConnectProperties);
                obConnector = getObConnector(key);
                if (obConnector != null) {
                    break;
                }
            }
        } else {

            ObConnectorKey key = ObConnectorKey.of(connectProperties);
            obConnector = getObConnector(key);
        }
        return obConnector;
    }

    private ObConnector getObConnector(ObConnectorKey key) {
        ObConnector obConnector = cache.getIfPresent(key);
        if (obConnector != null) {
            if (obConnector.isAlive()) {
                log.debug("[obsdk] alive ob connector found in holder, key={}", key);
            } else {

                log.info("[obsdk] the ob connector found in holder is not alive, key={}", key);
                cache.invalidate(key);
                obConnector = null;
            }
        } else {
            log.info("[obsdk] no ob connector found in holder, key={}", key);
        }
        return obConnector;
    }

    void invalidate(String clusterName) {
        log.info("[obsdk] invalidate ob connector, clusterName={}", clusterName);
        invalidate(key -> StringUtils.equals(key.getClusterName(), clusterName));
    }

    void invalidate(String clusterName, String tenantName, String username) {
        log.info("[obsdk] invalidate ob connector, clusterName={}, tenantName={}, username={}", clusterName, tenantName,
                username);
        invalidate(key -> StringUtils.equals(key.getClusterName(), clusterName)
                && StringUtils.equals(key.getTenantName(), tenantName)
                && StringUtils.equals(key.getUsername(), username));
    }


    private synchronized void invalidate(Predicate<ObConnectorKey> predicate) {
        Set<ObConnectorKey> keySet = cache.asMap().keySet();
        keySet.forEach(key -> {
            if (predicate.evaluate(key)) {
                log.info("[obsdk] invalidate ob connector, key={}", key);
                cache.invalidate(key);
            }
        });
    }

    void clearAll() {
        log.info("[obsdk] clear all the ob connectors in holder.");
        cache.invalidateAll();
    }

    void cleanUp() {
        log.info("[obsdk] clean up the expired connectors.");
        cache.cleanUp();

        delayedCloseList.forEach(obConnector -> {
            if (!obConnector.isActive()) {
                obConnector.close();
                delayedCloseList.remove(obConnector);
                log.info("[obsdk] remove from delayed close list, key={}", obConnector.key());
            }
        });
        if (delayedCloseList.size() != 0) {
            log.warn("[obsdk] holder still has active ob connectors in delayed close list. count={}",
                    delayedCloseList.size());
        }
    }

    void setCapacity(long size) {
        cache.policy().eviction().ifPresent(eviction -> eviction.setMaximum(size));
    }

    long getCapacity() {
        Optional<Eviction<ObConnectorKey, ObConnector>> optional = cache.policy().eviction();
        return optional.map(Eviction::getMaximum).orElse(-1L);
    }

    void setMaxIdleSeconds(long seconds) {
        cache.policy().expireAfterAccess()
                .ifPresent(expiration -> expiration.setExpiresAfter(seconds, TimeUnit.SECONDS));
    }

    long getMaxIdleSeconds() {
        Optional<Expiration<ObConnectorKey, ObConnector>> optional = cache.policy().expireAfterAccess();
        return optional.map(expiration -> expiration.getExpiresAfter(TimeUnit.SECONDS)).orElse(-1L);
    }
}
