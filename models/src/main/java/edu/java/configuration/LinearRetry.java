package edu.java.configuration;

import edu.java.dto.exception.ServiceException;
import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public final class LinearRetry extends Retry {
    public final Predicate<Throwable> errorFilter;
    private final int maxAttempts;
    private final Duration initialDelay;

    private LinearRetry(
        int maxAttempts,
        Duration initialDelay,
        List<HttpStatus> codes
    ) {
        this.maxAttempts = maxAttempts;
        this.initialDelay = initialDelay;
        errorFilter = (exception) -> (exception instanceof ServiceException se) && codes.contains(se.code());
    }

    public static LinearRetry of(
        HttpClientConfig httpClientConfig
    ) {
        return new LinearRetry(
            httpClientConfig.maxAttempts(),
            httpClientConfig.interval(),
            httpClientConfig.codes()
        );
    }

    @Override
    public Flux<Long> generateCompanion(Flux<RetrySignal> t) {
        return Flux.deferContextual((cv) -> t.contextWrite(cv).concatMap((retryWhenState) -> {
            Throwable currentFailure = retryWhenState.failure();
            long iteration = retryWhenState.totalRetries();
            if (currentFailure == null) {
                return Mono.error(new IllegalStateException("Retry.RetrySignal#failure() not expected to be null"));
            } else if (!errorFilter.test(currentFailure) || iteration > maxAttempts) {
                return Mono.error(currentFailure);
            } else {
                Duration nextBackoff = initialDelay.multipliedBy(iteration);
                if (nextBackoff.isZero()) {
                    return Mono.just(iteration).contextWrite(cv);
                } else {
                    return Mono.delay(nextBackoff).contextWrite(cv);
                }
            }
        }).onErrorStop());
    }
}
