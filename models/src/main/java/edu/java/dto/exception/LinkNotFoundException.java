package edu.java.dto.exception;


public class LinkNotFoundException extends DTOException {
    public LinkNotFoundException(String message) {
        super(message);
    }

    public LinkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
