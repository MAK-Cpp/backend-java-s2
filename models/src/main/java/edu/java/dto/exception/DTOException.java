package edu.java.dto.exception;

public abstract class DTOException extends RuntimeException {
    public DTOException(String message) {
        super(message);
    }

    public DTOException(String message, Throwable cause) {
        super(message, cause);
    }
}
