package edu.java.bot.exception;

public class BotServiceException extends RuntimeException {
    public BotServiceException() {
    }

    public BotServiceException(String message) {
        super(message);
    }

    public BotServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BotServiceException(Throwable cause) {
        super(cause);
    }
}
