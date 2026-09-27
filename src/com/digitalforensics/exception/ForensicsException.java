package com.digitalforensics.exception;

/**
 * Base class for all checked exceptions in the application.
 * Every custom exception extends this, so a servlet can catch
 * ForensicsException once and show the message to the user.
 */
public class ForensicsException extends Exception {
    public ForensicsException(String message) {
        super(message);
    }

    public ForensicsException(String message, Throwable cause) {
        super(message, cause);
    }
}
