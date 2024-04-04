package edu.java.scrapper.exception;

public class UnexpectedValuesCountException extends RuntimeException {
    public UnexpectedValuesCountException(String message) {
        super(message);
    }

    public UnexpectedValuesCountException(String message, Throwable cause) {
        super(message, cause);
    }
}
