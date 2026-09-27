package com.digitalforensics.exception;

/** Thrown when the logged-in user's role is not allowed to perform an action. */
public class UnauthorizedAccessException extends ForensicsException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
