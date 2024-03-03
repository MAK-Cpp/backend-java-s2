package edu.java.bot.exception.handler;

import edu.java.bot.exception.WrongRequestParametersException;
import edu.java.bot.request.LinkUpdateRequest;
import edu.java.bot.response.ApiErrorResponse;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BotExceptionHandler {
    private static final String BAD_REQUEST_PARAMETERS = "Некорректные параметры запроса";

    @ExceptionHandler(value = WrongRequestParametersException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiErrorResponse wrongRequestParametersException(WrongRequestParametersException exception, LinkUpdateRequest request) {
        String[] stacktrace =
            Arrays.stream(exception.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new);
        return new ApiErrorResponse(
            BAD_REQUEST_PARAMETERS,
            "400",
            exception.getClass().getName(),
            exception.getMessage(),
            stacktrace
        );
    }
}

