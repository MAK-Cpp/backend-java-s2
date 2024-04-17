package edu.java.dto.exception;

public class LinkAlreadyTrackedException extends WrongParametersException {
    public LinkAlreadyTrackedException(String message) {
        super(message);
    }

    public LinkAlreadyTrackedException(String message, Throwable cause) {
        super(message, cause);
    }
}
