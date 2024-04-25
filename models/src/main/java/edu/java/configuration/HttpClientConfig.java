package edu.java.configuration;

import edu.java.dto.exception.ServiceException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.http.HttpStatus;
import reactor.util.retry.Retry;

public record HttpClientConfig(
    @NotEmpty String url,
    @NotNull Duration interval,
    @NotNull Integer maxAttempts,
    @NotNull BackOff backOff,
    @NotNull List<HttpStatus> codes
) {
    public enum BackOff {
        CONSTANT, LINEAR, EXPONENTIAL
    }

    public Retry retry() {
        return switch (backOff) {
            case CONSTANT -> Retry.fixedDelay(maxAttempts, interval)
                .filter(status -> (status instanceof ServiceException se) && codes.contains(se.code()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
            case LINEAR -> LinearRetry.of(this);
            case EXPONENTIAL -> Retry.backoff(maxAttempts, interval)
                .jitter(0)
                .filter(status -> (status instanceof ServiceException se) && codes.contains(se.code()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
        };
    }
}
