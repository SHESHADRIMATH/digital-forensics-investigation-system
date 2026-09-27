package com.digitalforensics.exception;

/** Thrown when case data fails validation (empty title, bad priority...). */
public class InvalidCaseException extends ForensicsException {
    public InvalidCaseException(String message) {
        super(message);
    }
}
