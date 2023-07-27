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
package com.oceanbase.ocp.common.util.encrypt;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

import org.apache.commons.lang3.Validate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CryptoUtils {

    private synchronized static byte[] encrypt(byte[] origin, String key) {
        return Base64.getEncoder().encode(Encryptors.aes(key).encrypt(origin));
    }

    private synchronized static byte[] decrypt(byte[] encrypted, String key) {
        return Encryptors.aes(key).decrypt(Base64.getDecoder().decode(encrypted));
    }

    public static String encrypt(String plainText, String key) {
        Validate.notEmpty(plainText, "plain text can't be empty!");
        Validate.notEmpty(key, "secret key can't be empty!");
        return new String(encrypt(plainText.getBytes(UTF_8), key), UTF_8);
    }

    public static String decrypt(String encrypted, String key) {
        Validate.notEmpty(encrypted, "encrypted string can't be empty!");
        Validate.notEmpty(key, "secret key can't be empty!");
        return new String(decrypt(encrypted.getBytes(UTF_8), key), UTF_8);
    }
}
