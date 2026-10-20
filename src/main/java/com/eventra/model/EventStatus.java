package com.eventra.model;

/**
 * Represents the lifecycle status of an event in Eventra.
 */
public enum EventStatus {
    UPCOMING,
    ALMOST_FULL,
    FULL,
    COMPLETED,
    CANCELLED;

    /**
     * Safely parses an EventStatus from a string, ignoring case.
     * Defaults to UPCOMING if invalid or null.
     */
    public static EventStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return UPCOMING;
        }
        try {
            return EventStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UPCOMING;
        }
    }
}
