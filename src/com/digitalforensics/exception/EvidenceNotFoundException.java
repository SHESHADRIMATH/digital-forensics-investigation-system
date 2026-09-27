package com.digitalforensics.exception;

/** Thrown when an evidence id does not exist, or its file is missing from storage. */
public class EvidenceNotFoundException extends ForensicsException {
    public EvidenceNotFoundException(String message) {
        super(message);
    }
}
