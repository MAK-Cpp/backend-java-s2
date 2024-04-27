package edu.java.dto.exception;

import org.springframework.http.HttpStatus;

public class APIException extends ServiceException {
    public APIException(HttpStatus code, Throwable cause) {
        super("API exception", code, cause);
    }
}
