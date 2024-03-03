package edu.java.bot.exception;

public class WrongRequestParametersException extends BotServiceException {
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
