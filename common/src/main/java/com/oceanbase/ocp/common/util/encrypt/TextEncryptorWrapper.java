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

import com.oceanbase.ocp.common.util.encode.ByteArrayToStringConverter;

public class TextEncryptorWrapper implements TextEncryptor {

    private final BytesEncryptor encryptDecrypt;
    private final ByteArrayToStringConverter byteArrayToStringConverter;

    public TextEncryptorWrapper(BytesEncryptor encryptDecrypt, ByteArrayToStringConverter byteArrayToStringConverter) {
        this.encryptDecrypt = encryptDecrypt;
        this.byteArrayToStringConverter = byteArrayToStringConverter;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encryptDecrypt.encrypt(bytes);
        return byteArrayToStringConverter.convertToRight(encrypted);
    }

    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        byte[] encrypted = byteArrayToStringConverter.convertToLeft(encryptedText);
        byte[] decrypt = encryptDecrypt.decrypt(encrypted);
        return new String(decrypt, StandardCharsets.UTF_8);
    }

}
