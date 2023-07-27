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
package com.oceanbase.ocp.common.util.json;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import lombok.extern.slf4j.Slf4j;

/**
 * Json to String convert, same API as Gson, leverage Jackson, but catch
 * exception, return null instead
 */
@Slf4j
public class JsonUtils {

    public static final ObjectMapper OBJECT_MAPPER = JacksonFactory.jsonMapper();

    public static <T> T fromJson(String json, Class<T> classType) {
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, classType);
        } catch (JsonProcessingException e) {
            log.warn("failed to covert json string to class:{}, ", classType);
            return null;
        }
    }

    /**
     * from jsonString to a List object
     *
     * @param json jsonString which is a json array
     * @param classType classType of the List
     * @return List of classType, null if input json is null or invalid
     */
    public static <T> List<T> fromJsonList(String json, Class<T> classType) {
        if (json == null) {
            return null;
        }
        try {
            CollectionType javaType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, classType);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.warn("failed to covert json string to List type:{}", classType);
            return null;
        }
    }

    public static String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("failed to convert to json string", e);
            return null;
        }
    }

}
