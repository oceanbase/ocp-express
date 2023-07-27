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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyGenerator {

    public static EncodedKeyPair geneRsA(int keySize) {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException ignore) {
            throw new RuntimeException("No RAS algorithm.");
        }
        keyGen.initialize(keySize);
        KeyPair keyPair = keyGen.generateKeyPair();
        Function<byte[], String> encoder = bytes -> Base64.getEncoder().encodeToString(bytes);
        return new EncodedKeyPair(encoder.apply(keyPair.getPublic().getEncoded()),
                encoder.apply(keyPair.getPrivate().getEncoded()));
    }

    @Getter
    @AllArgsConstructor
    public static class EncodedKeyPair {

        private String publicKey;

        private String privateKey;
    }

}
