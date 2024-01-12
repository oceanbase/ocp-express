package com.oceanbase.ocp.common.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class PasswordChecker {

    private static final Set<Character> OCP_SUPPORTED_SPECIAL_CHARACTERS = new HashSet<>(
            Arrays.asList('~', '!', '@', '#', '%', '^', '&', '*', '_', '-', '+', '=', '|', '(', ')', '{', '}', '[', ']',
                    ':', ';', ',', '.', '?', '/', '$', '`', '\'', '"', '<', '>', '\\'));

    /**
     * Check whether a password is valid or not. The password must meet the
     * following requirements:
     * <ul>
     * <li>The length of the password must be between 8 and 32 characters.</li>
     * <li>containing at least three or more types of four types: numbers (0-9),
     * uppercase letters (A-Z), lowercase letters (a-z), and special symbols</li>
     * </ul>
     *
     * @param password the password to be checked
     * @return true if the password is valid, otherwise false
     */
    public static boolean checkOcpPassword(String password) {
        return checkPassword(password, OCP_SUPPORTED_SPECIAL_CHARACTERS);
    }

    private static boolean checkPassword(String password, Set<Character> supportedChars) {
        if (password == null || password.length() > 32 || password.length() < 8) {
            return false;
        }
        char[] arr = password.toCharArray();
        int digitCount = 0, lowerCount = 0, upperCount = 0, specialCount = 0;
        boolean allCharLegal = true;
        Predicate<Character> supportedPredicate = supportedChars::contains;
        for (char c : arr) {
            if (Character.isDigit(c)) {
                digitCount = 1;
            } else if (Character.isLowerCase(c)) {
                lowerCount = 1;
            } else if (Character.isUpperCase(c)) {
                upperCount = 1;
            } else if (supportedPredicate.test(c)) {
                specialCount = 1;
            } else {
                allCharLegal = false;
                break;
            }
        }
        return allCharLegal && (digitCount + lowerCount + upperCount + specialCount) >= 3;
    }


}
