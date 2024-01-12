package com.oceanbase.ocp.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordCheckerTest {

    @Test
    public void checkOcpPassword_normalRule() {
        assertTrue(PasswordChecker.checkOcpPassword("aA123456"));
        assertTrue(PasswordChecker.checkOcpPassword("aA-23456"));
        assertTrue(PasswordChecker.checkOcpPassword("1A-24567"));
        assertTrue(PasswordChecker.checkOcpPassword("aA-bcdef"));
        assertTrue(PasswordChecker.checkOcpPassword("1A~!@#%^&*_-+=|(){}[]:;,.?/"));
        assertTrue(PasswordChecker.checkOcpPassword("1A@#%^&*_-+=|(){}[]:;,.?/$`'\"<>\\"));
        assertTrue(PasswordChecker.checkOcpPassword("1A~!@^&*_-+=|(){}[]:;,.?/$`'\"<>\\"));

        assertFalse(PasswordChecker.checkOcpPassword("abc"));
        assertFalse(PasswordChecker.checkOcpPassword("aA11"));
        assertFalse(PasswordChecker.checkOcpPassword("1234567-"));

        assertFalse(PasswordChecker.checkOcpPassword("aaAA__11 "));
        assertTrue(PasswordChecker.checkOcpPassword("aA012345678901234567890123456789"));
        assertFalse(PasswordChecker.checkOcpPassword("aA0123456789012345678901234567890"));
    }

}
