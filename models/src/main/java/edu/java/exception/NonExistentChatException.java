package edu.java.exception;


public class NonExistentChatException extends DTOException {
    public NonExistentChatException(String message) {
        super(message);
    }

    public NonExistentChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
