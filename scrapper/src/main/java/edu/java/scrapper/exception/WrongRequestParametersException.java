package edu.java.scrapper.exception;

public class WrongRequestParametersException extends ScrapperServiceException {
    public WrongRequestParametersException() {
    }

    public WrongRequestParametersException(String message) {
        super(message);
    }

    public WrongRequestParametersException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongRequestParametersException(Throwable cause) {
        super(cause);
    }
}
