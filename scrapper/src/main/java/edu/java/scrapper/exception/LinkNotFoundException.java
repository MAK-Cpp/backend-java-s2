package edu.java.scrapper.exception;

public class LinkNotFoundException extends ScrapperServiceException {
    public LinkNotFoundException() {
    }

    public LinkNotFoundException(String message) {
        super(message);
    }

    public LinkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinkNotFoundException(Throwable cause) {
        super(cause);
    }
}
