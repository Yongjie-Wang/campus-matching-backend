package com.wang.partner.utils;

import java.security.SecureRandom;

public class RandomUsernameGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz123456789";
    private static final int MIN_LENGTH = 3;



    public static String generateUsername() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("咕噜咕噜");
        int length = MIN_LENGTH + random.nextInt(8);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            char randomChar = ALPHABET.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}