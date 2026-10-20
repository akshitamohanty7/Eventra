package com.eventra.model;

/**
 * Defines the status of an attendee's event booking.
 */
public enum RegistrationStatus {
    CONFIRMED,
    CANCELLED,
    WAITLIST;

    /**
     * Safely parses a RegistrationStatus from a string, ignoring case.
     * Defaults to CONFIRMED if invalid or null.
     */
    public static RegistrationStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return CONFIRMED;
        }
        try {
            return RegistrationStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CONFIRMED;
        }
    }
}
