package com.eventra.exception;

/**
 * Thrown when an attendee attempts to register for an event they are already registered for.
 */
public class DuplicateRegistrationException extends Exception {
    private static final long serialVersionUID = 1L;

    private int userId;
    private int eventId;

    public DuplicateRegistrationException(String message) {
        super(message);
    }

    public DuplicateRegistrationException(int userId, int eventId, String message) {
        super(message);
        this.userId = userId;
        this.eventId = eventId;
    }

    public DuplicateRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getUserId() {
        return userId;
    }

    public int getEventId() {
        return eventId;
    }
}
