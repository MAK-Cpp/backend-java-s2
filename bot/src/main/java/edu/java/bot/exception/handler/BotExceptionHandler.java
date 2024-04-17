package edu.java.bot.exception.handler;

import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ApiErrorResponse;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BotExceptionHandler {
    private static final String BAD_REQUEST_PARAMETERS = "Некорректные параметры запроса";

    @ExceptionHandler(value = WrongParametersException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiErrorResponse wrongRequestParametersException(
        WrongParametersException exception
    ) {
        String[] stacktrace =
            Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new);
        return new ApiErrorResponse(
            BAD_REQUEST_PARAMETERS,
            "400",
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            stacktrace
        );
    }
}

