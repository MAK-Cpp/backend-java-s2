package edu.java.dto.exception;

public class UnsupportedLinkException extends WrongParametersException {
    public UnsupportedLinkException(String message) {
        super(message);
    }

    public UnsupportedLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
