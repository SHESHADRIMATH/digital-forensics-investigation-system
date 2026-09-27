package com.digitalforensics.exception;

/**
 * Unchecked wrapper around SQLException. DAOs throw this so that
 * every method does not have to declare "throws SQLException".
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
