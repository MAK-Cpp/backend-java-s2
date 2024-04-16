package edu.java.dto.exception;

public class AliasAlreadyTakenException extends WrongParametersException {
    public AliasAlreadyTakenException(String message) {
        super(message);
    }

    public AliasAlreadyTakenException(String message, Throwable cause) {
        super(message, cause);
    }
}
