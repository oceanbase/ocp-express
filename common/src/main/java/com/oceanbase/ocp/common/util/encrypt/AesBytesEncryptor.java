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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AesBytesEncryptor implements BytesEncryptor {

    private final byte[] ivCode = new byte[16];

    private final SecretKeySpec secretKey;

    public AesBytesEncryptor(String key, String salt) {
        Validate.notEmpty("key is empty or null");
        Validate.notEmpty("salt is empty or null");
        this.secretKey = newSecretKey(key, salt);
    }

    @Override
    public byte[] encrypt(byte[] origin) {
        try {
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE);
            byte[] encrypted = cipher.doFinal(origin);
            return addIVToCipher(encrypted);
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            throw new RuntimeException(rootCauseMessage);
        }
    }

    @Override
    public byte[] decrypt(byte[] encrypted) {
        try {
            Cipher cipher = initCipher(Cipher.DECRYPT_MODE);
            byte[] original = cipher.doFinal(encrypted);
            return Arrays.copyOfRange(original, 16, original.length);
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            throw new RuntimeException(rootCauseMessage);
        }
    }

    private SecretKeySpec newSecretKey(String key, String salt) {
        try {
            String factoryInstance = "PBKDF2WithHmacSHA256";
            SecretKeyFactory factory = SecretKeyFactory.getInstance(factoryInstance);
            KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 128);
            SecretKey secretKey = factory.generateSecret(keySpec);
            String secretKeyType = "AES";
            return new SecretKeySpec(secretKey.getEncoded(), secretKeyType);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Not a valid encryption algorithm", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Not a valid secret key", e);
        }
    }

    private Cipher initCipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        // Generating random IV
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivCode);
        cipher.init(mode, secretKey, new IvParameterSpec(ivCode));
        return cipher;
    }

    private byte[] addIVToCipher(byte[] encrypted) {
        byte[] cipherWithIv = new byte[ivCode.length + encrypted.length];
        System.arraycopy(ivCode, 0, cipherWithIv, 0, ivCode.length);
        System.arraycopy(encrypted, 0, cipherWithIv, ivCode.length, encrypted.length);
        return cipherWithIv;
    }

}
