/*
 * oceanbase.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.oceanbase.ocp.common.util.encrypt;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class BlowFishBytesEncryptorTest {

    @Test
    public void encryptAndDecryptOkay() {
        String originalTxt = "whatever";
        String key = "ThisIs128BitKey@";
        BlowFishBytesEncryptor blowFishBytesEncryptor = new BlowFishBytesEncryptor(key);
        byte[] encrypt = blowFishBytesEncryptor.encrypt(originalTxt.getBytes(StandardCharsets.UTF_8));
        String decrypted = new String(blowFishBytesEncryptor.decrypt(encrypt), StandardCharsets.UTF_8);
        assertEquals(originalTxt, decrypted);
    }

}
