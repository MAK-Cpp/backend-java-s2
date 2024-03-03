package edu.java.scrapper.exception;

public class NonExistentChatException extends ScrapperServiceException {
    public NonExistentChatException() {
    }

    public NonExistentChatException(String message) {
        super(message);
    }

    public NonExistentChatException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonExistentChatException(Throwable cause) {
        super(cause);
    }
}
