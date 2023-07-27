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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Splitter;

import com.oceanbase.ocp.common.util.json.JacksonFactory;

/**
 * Utils for map operations
 */
public class MapUtils {

    private static final ObjectMapper OBJECT_MAPPER = JacksonFactory.jsonMapper();

    /**
     * A util that maps a list of entries into a <K,V> map.
     */
    public static <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType, Object... entries) {
        if (entries == null || entries.length % 2 == 1) {
            throw new IllegalArgumentException("toMap must be called with an even number of parameters");
        }
        return IntStream.range(0, entries.length / 2).map(i -> i * 2).collect(HashMap::new,
                (m, i) -> m.put(keyType.cast(entries[i]), valueType.cast(entries[i + 1])), Map::putAll);
    }

    public static Map<String, String> fromString(String str, String entrySeparator, String keyToValueSeparator) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyMap();
        }
        return Splitter.on(entrySeparator).withKeyValueSeparator(keyToValueSeparator).split(str);
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return org.apache.commons.collections4.MapUtils.isEmpty(map);
    }

    public static int size(final Map<?, ?> map) {
        return org.apache.commons.collections4.MapUtils.size(map);
    }

    /**
     * Convert json string to map.
     *
     * @param jsonArray json array
     * @param keyClass class type of map key
     * @param valueClass class type of map value
     * @param <K> type of map key
     * @param <V> type of map value
     * @return HashMap
     */
    public static <K, V> Map<K, V> toMap(String jsonArray, Class<K> keyClass, Class<V> valueClass) {
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
        try {
            return OBJECT_MAPPER.readValue(jsonArray, mapType);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
