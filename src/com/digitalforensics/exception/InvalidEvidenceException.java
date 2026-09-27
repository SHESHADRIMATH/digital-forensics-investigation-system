package com.digitalforensics.exception;

/** Thrown when an uploaded evidence file is empty, too large or of an unsupported type. */
public class InvalidEvidenceException extends ForensicsException {
    public InvalidEvidenceException(String message) {
        super(message);
    }
}
