package edu.java.scrapper.exception;

public class ScrapperServiceException extends RuntimeException {
    public ScrapperServiceException() {
    }

    public ScrapperServiceException(String message) {
        super(message);
    }

    public ScrapperServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrapperServiceException(Throwable cause) {
        super(cause);
    }
}
