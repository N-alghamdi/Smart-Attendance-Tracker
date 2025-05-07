/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.attendancetracker.service;

/**
 *
 * @author sulto
 */
// CodeGenerator.java


import java.security.SecureRandom;

/**
 * Utility class for generating short alphanumeric attendance codes.
 */
public class CodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_CODE_LENGTH = 6;

    /**
     * Generates a random alphanumeric code using the default length.
     * @return A randomly generated code.
     */
    public static String generateCode() {
        return generateCode(DEFAULT_CODE_LENGTH);
    }

    /**
     * Generates a random alphanumeric code of the specified length.
     * @param length The desired length of the code.
     * @return A randomly generated code.
     */
    public static String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}

