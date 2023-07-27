package com.oceanbase.ocp.bootstrap.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncryptorTest {

    @Test
    public void encryptDecrypt() {
        String text = "hello world";
        String key = "key111";
        AESEncryptor encryptor = new AESEncryptor(key);
        String encrypted = encryptor.encrypt(text);
        System.out.println(encrypted);
        AESEncryptor encryptor2 = new AESEncryptor(key);
        String result = encryptor2.decrypt(encrypted);
        assertEquals(text, result);
    }
}
