package edu.java.dto.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {
    private static final String MESSAGE_CODE_FORMAT = "CODE %s:\n%s";
    private final HttpStatus code;

    public ServiceException(String message, HttpStatus code) {
        super(String.format(MESSAGE_CODE_FORMAT, code, message));
        this.code = code;
    }

    public ServiceException(String message, HttpStatus code, Throwable cause) {
        super(String.format(MESSAGE_CODE_FORMAT, code, message), cause);
        this.code = code;
    }

    public HttpStatus code() {
        return code;
    }

    public int rawCode() {
        return code.value();
    }
}
