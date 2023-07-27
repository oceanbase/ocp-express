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

package com.oceanbase.ocp.bootstrap.util;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryptor {

    private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_INSTANCE = "AES/CBC/PKCS5PADDING";
    private static final String SECRET_KEY_TYPE = "AES";
    private final SecretKeySpec secretKey;

    public AESEncryptor(String key, String salt) {
        this.secretKey = newSecretKey(key, salt);
    }

    public AESEncryptor(String key) {
        this(key, key);
    }

    public byte[] encrypt(byte[] origin) {
        try {
            final byte[] ivCode = createIv();
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, ivCode);
            byte[] encrypted = cipher.doFinal(origin);
            return addIVToCipher(ivCode, encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(byte[] encrypted) {
        try {
            final byte[] ivCode = createIv();
            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, ivCode);
            byte[] original = cipher.doFinal(encrypted);
            return Arrays.copyOfRange(original, 16, original.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String encrypt(String in) {
        return Base64.getEncoder().withoutPadding().encodeToString(encrypt(in.getBytes()));
    }

    public String decrypt(String in) {
        return new String(decrypt(Base64.getDecoder().decode(in)));
    }

    private SecretKeySpec newSecretKey(String key, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);
            KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey secretKey = factory.generateSecret(keySpec);
            return new SecretKeySpec(secretKey.getEncoded(), SECRET_KEY_TYPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Cipher initCipher(int mode, byte[] ivCode) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
        cipher.init(mode, secretKey, new IvParameterSpec(ivCode));
        return cipher;
    }

    @Nonnull
    private byte[] createIv() {
        final byte[] ivCode = new byte[16];
        // Generating random IV
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivCode);
        return ivCode;
    }

    private byte[] addIVToCipher(byte[] ivCode, byte[] encrypted) {
        byte[] cipherWithIv = new byte[ivCode.length + encrypted.length];
        System.arraycopy(ivCode, 0, cipherWithIv, 0, ivCode.length);
        System.arraycopy(encrypted, 0, cipherWithIv, ivCode.length, encrypted.length);
        return cipherWithIv;
    }
}
