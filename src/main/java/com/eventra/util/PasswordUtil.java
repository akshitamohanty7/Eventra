package com.eventra.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing and verifying passwords using SHA-256.
 * Adheres to AGENTS.md zero-dependency security standards.
 */
public class PasswordUtil {

    private PasswordUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Hashes a plain-text password using SHA-256 and returns a lowercase hex string.
     *
     * @param password the plain-text password
     * @return 64-character lowercase hexadecimal hash
     */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies that a plain text candidate password matches a stored hash.
     *
     * @param plainPassword the plain text candidate password
     * @param storedHash the existing 64-character hex hash
     * @return true if candidate hashes to the stored hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String candidateHash = hashPassword(plainPassword);
        return candidateHash.equalsIgnoreCase(storedHash);
    }
}
