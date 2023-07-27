/*
 * oceanbase.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.oceanbase.ocp.common.util.encrypt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CryptoUtilsTest {

    @Test
    public void encryptAndDecryptOkay() {
        String originalTxt = "whatever";
        String key = "ThisIs128BitKey@";
        String encrypted = CryptoUtils.encrypt(originalTxt, key);
        String decrypted = CryptoUtils.decrypt(encrypted, key);
        assertEquals(originalTxt, decrypted);
    }

}