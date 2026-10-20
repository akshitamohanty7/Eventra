package com.eventra.model;

/**
 * Defines the user authorization roles in Eventra.
 */
public enum UserRole {
    ATTENDEE,
    ADMIN;

    /**
     * Safely parses a UserRole from a string, ignoring case.
     * Defaults to ATTENDEE if invalid or null.
     */
    public static UserRole fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return ATTENDEE;
        }
        try {
            return UserRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ATTENDEE;
        }
    }
}
