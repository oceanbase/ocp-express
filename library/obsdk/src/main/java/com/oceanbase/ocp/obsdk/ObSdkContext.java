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

package com.oceanbase.ocp.obsdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Data;

public class ObSdkContext {

    private static long CONNECT_STATE_EXPIRE_MILLIS = 120_000;
    private static long CREDENTIAL_EXPIRE_MILLIS = 300_000;

    private static final ThreadLocal<Map<Object, Long>> CONNECT_STATE_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Container<String>>> CREDENTIAL_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Long> QUERY_TIMEOUT_HOLDER = new ThreadLocal<>();

    static void setConnectStateExpireMillis(long connectStateExpireMillis) {
        CONNECT_STATE_EXPIRE_MILLIS = connectStateExpireMillis;
    }

    static void setCredentialExpireMillis(long credentialExpireMillis) {
        CREDENTIAL_EXPIRE_MILLIS = credentialExpireMillis;
    }

    public static void setConnectState(Object key, Boolean success) {
        Map<Object, Long> map = CONNECT_STATE_HOLDER.get();
        if (map == null) {
            map = new HashMap<>();
            CONNECT_STATE_HOLDER.set(map);
        }
        if (success) {
            map.put(key, 0L);
        } else {
            map.put(key, System.currentTimeMillis());
        }
    }

    public static boolean connectFailed(Object key) {
        Map<Object, Long> map = CONNECT_STATE_HOLDER.get();
        long timestamp = Optional.ofNullable(map).map(m -> m.get(key)).orElse(0L);
        if (timestamp == 0L) {
            return false;
        }
        long duration = System.currentTimeMillis() - timestamp;
        if (duration > CONNECT_STATE_EXPIRE_MILLIS) {
            // Exceed 2 minutes, reset.
            map.put(key, 0L);
            return false;
        }
        return duration >= 0;
    }

    public static void putCredential(String key, String credential) {
        Map<String, Container<String>> map = CREDENTIAL_HOLDER.get();
        if (map == null) {
            map = new HashMap<>();
            CREDENTIAL_HOLDER.set(map);
        }
        Container<String> container = new Container<>();
        container.setTimestamp(System.currentTimeMillis());
        container.setValue(credential);
        map.put(key, container);
    }

    public static String getCredential(String key) {
        Map<String, Container<String>> map = CREDENTIAL_HOLDER.get();
        Container<String> container = Optional.ofNullable(map).map(m -> m.get(key)).orElse(null);
        if (container == null) {
            return null;
        }
        long duration = System.currentTimeMillis() - container.getTimestamp();
        if (duration > CREDENTIAL_EXPIRE_MILLIS) {
            map.remove(key);
            return null;
        }
        return container.getValue();
    }

    public static Long getQueryTimeout() {
        return QUERY_TIMEOUT_HOLDER.get();
    }

    public static void clear() {
        CONNECT_STATE_HOLDER.remove();
        CREDENTIAL_HOLDER.remove();
        QUERY_TIMEOUT_HOLDER.remove();
    }

    @Data
    private static class Container<T> {

        long timestamp;
        T value;
    }

}
