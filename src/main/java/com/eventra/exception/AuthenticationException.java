package com.eventra.exception;

/**
 * Thrown when user authentication fails due to invalid credentials or non-existent accounts.
 */
public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
