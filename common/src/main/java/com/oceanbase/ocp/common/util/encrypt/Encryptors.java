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

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.oceanbase.ocp.common.util.encode.ByteArrayToHexConverter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Encryptors {

    private static final LoadingCache<String, AesBytesEncryptor> AES_ENCRYPTOR_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100L)
                    .build(new CacheLoader<String, AesBytesEncryptor>() {

                        @Override
                        public AesBytesEncryptor load(String key) {
                            return new AesBytesEncryptor(key, key);
                        }
                    });

    public static BytesEncryptor aes(String someThing) {
        try {
            return AES_ENCRYPTOR_CACHE.get(someThing);
        } catch (ExecutionException ex) {
            throw new RuntimeException("get AesBytesEncryptor from cache failed", ex);
        }
    }

    public static BytesEncryptor blowFish(String someThing) {
        return new BlowFishBytesEncryptor(someThing);
    }

    public static TextEncryptor blowFishHex(String someThing) {
        return new TextEncryptorWrapper(blowFish(someThing), new ByteArrayToHexConverter());
    }

    public static TextEncryptor ocpLegacy() {
        return blowFishHex("gQzLk5tTcGYlQ47G");
    }

}
