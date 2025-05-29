package com.linked.quizbot.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * QuestionListHash is a utility class for generating unique, short, alphanumeric codes
 * based on an input string and a timestamp. It uses SHA-256 hashing and Base-36 encoding
 * to create a code that is guaranteed to be unique for the same input and timestamp.
 * 
 * Most of this code was written by chatgpt
 */
public class QuestionListHash {

    public static final char[] BASE36_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    public static final int DEFAULT_LENGTH = 7;
    public static final Set<String> generatedCodes = new HashSet<>();
    private final int codeLength;

    public QuestionListHash(int codeLength) {
        this.codeLength = codeLength;
    }

    public QuestionListHash() {
        this(DEFAULT_LENGTH);
    }
    public static void addGeneratedCode(String code ){
        QuestionListHash.generatedCodes.add(code);
    }
    public String generate(String input, long timestamp) {
        String code;
        int attempts = 0;

        do {
            String randomComponent = UUID.randomUUID().toString();
            String combined = input + "|" + timestamp + "|" + randomComponent;
            code = createCode(combined);
            attempts++;

            // Just in case something goes wrong
            if (attempts > 10_000) {
                throw new RuntimeException("Too many hash collisions. Aborting.");
            }
        } while (generatedCodes.contains(code));

        QuestionListHash.addGeneratedCode(code);
        return code;
    }

    private String createCode(String combinedInput) {
        try {
            // SHA-256 hashing
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combinedInput.getBytes(StandardCharsets.UTF_8));
            BigInteger hashInt = new BigInteger(1, hashBytes);

            // Base-36 encoding
            String base36 = toBase36(hashInt);

            // Ensure it starts with a letter
            if (base36.length() > 0 && Character.isDigit(base36.charAt(0))) {
                base36 = "a" + base36;
            }

            // Truncate or pad the code
            return base36.length() >= codeLength ? base36.substring(0, codeLength) : padToLength(base36, codeLength);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String toBase36(BigInteger value) {
        StringBuilder sb = new StringBuilder();
        BigInteger base = BigInteger.valueOf(36);

        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = value.divideAndRemainder(base);
            sb.insert(0, BASE36_ALPHABET[divmod[1].intValue()]);
            value = divmod[0];
        }

        return sb.toString();
    }

    private String padToLength(String input, int length) {
        StringBuilder sb = new StringBuilder(input);
        while (sb.length() < length) {
            sb.append('a');
        }
        return sb.toString();
    }
    public static boolean isAlreadyInUse(String listId){
        return QuestionListHash.generatedCodes.contains(listId);
    }
}
