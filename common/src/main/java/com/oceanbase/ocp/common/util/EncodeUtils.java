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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class EncodeUtils {

    public static byte[] base64DecodeFromString(String encodedString) {
        if (Objects.isNull(encodedString)) {
            return null;
        }
        byte[] bytes = encodedString.getBytes(StandardCharsets.UTF_8);
        return base64Decode(bytes);
    }

    public static byte[] base64Decode(byte[] encoded) {
        if (Objects.isNull(encoded)) {
            return null;
        }
        return Base64.getDecoder().decode(encoded);
    }

    public static String base64EncodeToString(byte[] src) {
        byte[] encoded = base64Encode(src);
        if (Objects.isNull(encoded)) {
            return null;
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static byte[] base64Encode(byte[] src) {
        if (Objects.isNull(src)) {
            return null;
        }
        return Base64.getEncoder().encode(src);
    }
}
