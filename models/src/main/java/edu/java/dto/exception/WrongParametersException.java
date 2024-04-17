package edu.java.dto.exception;


public class WrongParametersException extends DTOException {
    public WrongParametersException(String message) {
        super(message);
    }

    public WrongParametersException(String message, Throwable cause) {
        super(message, cause);
    }
}
