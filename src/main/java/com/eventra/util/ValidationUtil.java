package com.eventra.util;

import java.util.regex.Pattern;

/**
 * Utility class for server-side validation of user inputs.
 * All methods are stateless and static — no instantiation allowed.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Returns true if the string is non-null and non-blank.
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Returns true if the email matches a basic RFC-compliant pattern.
     */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Returns true if the password meets minimum length requirement (8+ chars).
     */
    public static boolean isValidPassword(String password) {
        return isNotBlank(password) && password.length() >= 8;
    }

    /**
     * Returns true if two password strings are identical (for confirm-password checks).
     */
    public static boolean passwordsMatch(String password, String confirm) {
        return password != null && password.equals(confirm);
    }

    /**
     * Returns true if the integer value is strictly positive.
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }

    /**
     * Trims and returns the string, or empty string if null.
     */
    public static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
