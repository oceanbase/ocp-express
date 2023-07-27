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

package com.oceanbase.ocp.bootstrap.config.env;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.oceanbase.ocp.bootstrap.util.AESEncryptor;

public class Functions {

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public static Set<Object> toSet(Collection<Object> objects) {
        return new LinkedHashSet<>(objects);
    }

    public static List<Object> chain(List<Object>... lists) {
        int total = 0;
        for (List<Object> list : lists) {
            total += list.size();
        }
        List<Object> ret = new ArrayList<>(total);
        for (List<Object> list : lists) {
            if (list != null) {
                ret.addAll(list);
            }
        }
        return ret;
    }

    public static String concat(String... strings) {
        return String.join("", strings);
    }

    public static String join(String delimiter, Collection<String> strings) {
        return String.join(delimiter, strings);
    }

    public static Map<String, Object> flattenMap(String field, Map<String, Object> m) {
        Object toFlatten = m.get(field);
        if (!(toFlatten instanceof Map)) {
            throw new IllegalArgumentException(
                    "not a Map, got: " + (toFlatten == null ? "null" : toFlatten.getClass().getName()));
        }
        Map<String, Object> ret = new LinkedHashMap<>();
        m.forEach((key, value) -> {
            if (field.equals(key)) {
                Map<String, Object> toFlattenMap = (Map<String, Object>) toFlatten;
                ret.putAll(toFlattenMap);
            } else {
                ret.put(key, value);
            }
        });
        return ret;
    }

    public static org.apache.el.stream.Stream flatten(String field, Map<String, Object> m) {
        Object toFlatten = m.get(field);
        if (!(toFlatten instanceof Collection)) {
            throw new IllegalArgumentException(
                    "not a Collection, got: " + (toFlatten == null ? "null" : toFlatten.getClass().getName()));
        }
        Collection<Object> collection = (Collection<Object>) toFlatten;
        Iterator<Object> colItr = collection.iterator();
        return new org.apache.el.stream.Stream(new Iterator<Object>() {

            @Override
            public boolean hasNext() {
                return colItr.hasNext();
            }

            @Override
            public Object next() {
                Map<String, Object> ret = new HashMap<>(m.size());
                m.forEach((key, value) -> {
                    if (field.equals(key)) {
                        ret.put(key, colItr.next());
                    } else {
                        ret.put(key, value);
                    }
                });
                return ret;
            }
        });
    }

    public static Object debugPrint(Object o) {
        System.err.println("DEBUG: " + jsonEncode(o)); // NOPMD
        return o;
    }

    public static String jsonEncode(Object o) {
        return GSON.toJson(o);
    }

    public static Object jsonDecode(String s) {
        return GSON.fromJson(s, Object.class);
    }

    public static String bcryptHash(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    public static String aesEncrypt(String key, String data) {
        AESEncryptor encryptor = new AESEncryptor(key);
        return encryptor.encrypt(data);
    }

    public static String aesDecrypt(String key, String data) {
        AESEncryptor encryptor = new AESEncryptor(key);
        return encryptor.decrypt(data);
    }

    public static String systemProperty(String name) {
        return System.getProperty(name);
    }

    public static LinkedHashMap<Object, Object> emptyMap() {
        return new LinkedHashMap<>();
    }

    public static LinkedHashSet<Object> emptySet() {
        return new LinkedHashSet<>();
    }

    public static Date now() {
        return new Date();
    }

    public static Date date(long timestamp) {
        return new Date(timestamp);
    }

    public static String formatDate(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }

}
