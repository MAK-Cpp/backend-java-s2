package edu.java.dto.exception;

public class NonExistentLinkAliasException extends WrongParametersException {
    public NonExistentLinkAliasException(String message) {
        super(message);
    }

    public NonExistentLinkAliasException(String message, Throwable cause) {
        super(message, cause);
    }
}
