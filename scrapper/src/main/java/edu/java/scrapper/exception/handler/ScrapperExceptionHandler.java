package edu.java.scrapper.exception.handler;

import edu.java.dto.response.ApiErrorResponse;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice()
public class ScrapperExceptionHandler {
    private static final String BAD_REQUEST_PARAMETERS = "Некорректные параметры запроса";
    private static final String BAD_REQUEST = "400";
    private static final String CHAT_NOT_FOUND = "Чат не существует";
    private static final String LINK_NOT_FOUND = "Ссылка не найдена";
    private static final String NOT_FOUND = "404";

    @ExceptionHandler(value = WrongParametersException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiErrorResponse wrongRequestParametersException(WrongParametersException exception) {
        String[] stacktrace =
            Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new);
        return new ApiErrorResponse(
            BAD_REQUEST_PARAMETERS,
            BAD_REQUEST,
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            stacktrace
        );
    }

    @ExceptionHandler(value = NonExistentChatException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ApiErrorResponse nonExistentChatException(NonExistentChatException exception) {
        String[] stacktrace =
            Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new);
        return new ApiErrorResponse(
            CHAT_NOT_FOUND,
            NOT_FOUND,
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            stacktrace
        );
    }

    @ExceptionHandler(value = LinkNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ApiErrorResponse linkNotFoundException(LinkNotFoundException exception) {
        String[] stacktrace =
            Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new);
        return new ApiErrorResponse(
            LINK_NOT_FOUND,
            NOT_FOUND,
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            stacktrace
        );
    }
}
