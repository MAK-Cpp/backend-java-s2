package edu.java.dto.exception;

public class InvalidLinkException extends WrongParametersException {
    public InvalidLinkException(String message) {
        super(message);
    }

    public InvalidLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
