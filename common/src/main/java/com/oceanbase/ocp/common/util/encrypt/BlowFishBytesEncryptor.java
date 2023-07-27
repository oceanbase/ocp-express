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

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class BlowFishBytesEncryptor implements BytesEncryptor {

    private final byte[] key;

    /**
     * @param key key string
     */
    public BlowFishBytesEncryptor(String key) {
        Validate.notEmpty("someThing is empty or null");
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] encrypt(byte[] origin) {
        Validate.notNull(origin, "null input for encrypt");
        try {
            Cipher cipher = Cipher.getInstance("Blowfish");
            SecretKeySpec key = new SecretKeySpec(this.key, "Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(origin);
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            throw new RuntimeException(rootCauseMessage);
        }
    }

    @Override
    public byte[] decrypt(byte[] encrypted) {
        Validate.notNull(encrypted, "null input for decrypt");
        try {
            Cipher cipher = Cipher.getInstance("Blowfish");
            SecretKeySpec key = new SecretKeySpec(this.key, "Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            throw new RuntimeException(rootCauseMessage);
        }
    }

}
