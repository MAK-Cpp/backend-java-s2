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
    private final Duration increment;

    private LinearRetry(
        int maxAttempts,
        Duration initialDelay,
        Duration increment,
        List<HttpStatus> codes
    ) {
        this.maxAttempts = maxAttempts;
        this.initialDelay = initialDelay;
        this.increment = increment;
        errorFilter = (exception) -> (exception instanceof ServiceException se) && codes.contains(se.code());
    }

    // @Override
    // public Flux<RetrySignal> generateCompanion(Flux<RetrySignal> retrySignals) {
    //     return retrySignals
    //         .filter(rs -> (rs.failure() instanceof ServiceException se) && codes.contains(se.code()))
    //         .zipWith(Flux.range(1, maxAttempts), (signal, index) -> new Object[] {signal, index})
    //         .flatMap(tuple -> {
    //             RetrySignal signal = (RetrySignal) tuple[0];
    //             int attemptNumber = (Integer) tuple[1];
    //             System.out.println(signal);
    //             System.out.println(attemptNumber);
    //             if (attemptNumber < maxAttempts) {
    //                 Duration delay = initialDelay.plus(increment.multipliedBy(attemptNumber - 1));
    //                 System.out.println(delay);
    //                 return Mono.delay(delay).then(Mono.just(signal));
    //             } else {
    //                 return Mono.error(signal.failure());
    //             }
    //         });
    // }

    public static LinearRetry of(
        HttpClientConfig httpClientConfig
    ) {
        return new LinearRetry(
            httpClientConfig.maxAttempts(),
            httpClientConfig.interval(),
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
