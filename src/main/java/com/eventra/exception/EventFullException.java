package com.eventra.exception;

/**
 * Thrown when an attendee attempts to register for an event that has reached full capacity.
 */
public class EventFullException extends Exception {
    private static final long serialVersionUID = 1L;

    private int eventId;

    public EventFullException(String message) {
        super(message);
    }

    public EventFullException(int eventId, String message) {
        super(message);
        this.eventId = eventId;
    }

    public EventFullException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getEventId() {
        return eventId;
    }
}
