package edu.java.test.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.java.configuration.HttpClientConfig;
import edu.java.dto.exception.ServiceException;
import edu.java.test.Timer;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.HttpStatus;
import reactor.util.retry.Retry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:ParameterNumber"})
public abstract class ClientTest {
    protected static final List<HttpStatus> CODES = List.of(HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST);
    protected static final Retry RETRY =
        Retry.fixedDelay(5, Duration.ofSeconds(1))
            .filter(status -> (status instanceof ServiceException se)
                && CODES.contains(se.code()))
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());

    protected static int getPort() {
        for (int port = 1024; port <= 49151; ++port) {
            try (ServerSocket ignored = new ServerSocket(port)) {
                return port;
            } catch (IOException ignored) {
            }
        }
        return -1;
    }

    protected static HttpClientConfig constant(int duration, int maxAttempts, HttpStatus... codes) {
        return new HttpClientConfig(
            "ClientTest$constant",
            Duration.ofSeconds(duration),
            maxAttempts,
            HttpClientConfig.BackOff.CONSTANT,
            List.of(codes)
        );
    }

    protected static HttpClientConfig linear(int duration, int maxAttempts, HttpStatus... codes) {
        return new HttpClientConfig(
            "ClientTest$linear",
            Duration.ofSeconds(duration),
            maxAttempts,
            HttpClientConfig.BackOff.LINEAR,
            List.of(codes)
        );
    }

    protected static HttpClientConfig exponential(int duration, int maxAttempts, HttpStatus... trackedCodes) {
        return new HttpClientConfig(
            "ClientTest$exponential",
            Duration.ofSeconds(duration),
            maxAttempts,
            HttpClientConfig.BackOff.EXPONENTIAL,
            List.of(trackedCodes)
        );
    }

    protected static Stream<Arguments> testRetryStream() {
        return Stream.of(
            Arguments.of(
                // 3 + 3 + 3 = 9
                constant(
                    3, 3,
                    HttpStatus.BAD_GATEWAY, HttpStatus.FORBIDDEN
                ),
                8, true, HttpStatus.FORBIDDEN
            ),
            Arguments.of(
                // 6 = 6
                constant(
                    6, 1,
                    HttpStatus.BAD_GATEWAY, HttpStatus.FORBIDDEN
                ),
                10, false, HttpStatus.FORBIDDEN
            ),
            Arguments.of(
                // 3 + 3 + 3 + 3 + 3 = 15
                constant(
                    3, 5,
                    HttpStatus.BAD_GATEWAY, HttpStatus.BAD_REQUEST
                ),
                10, true, HttpStatus.FORBIDDEN
            ),
            Arguments.of(
                // 1 + 2 + 3 = 6
                linear(
                    1, 3,
                    HttpStatus.BAD_GATEWAY, HttpStatus.FORBIDDEN
                ),
                5, true, HttpStatus.BAD_GATEWAY
            ),
            Arguments.of(
                // 1 + 2 + 3 = 6
                linear(
                    1, 3,
                    HttpStatus.BAD_GATEWAY, HttpStatus.FORBIDDEN
                ),
                7, false, HttpStatus.BAD_GATEWAY
            ),
            Arguments.of(
                // 2 + 4 + 6 + 8 = 20
                linear(
                    2, 4,
                    HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST
                ),
                19, true, HttpStatus.BAD_GATEWAY
            ),
            Arguments.of(
                // 1 + 2 + 4 = 7
                exponential(
                    1, 3,
                    HttpStatus.BAD_GATEWAY, HttpStatus.FORBIDDEN
                ),
                4, true, HttpStatus.BAD_GATEWAY
            ),
            Arguments.of(
                // 1 + 2 + 4 = 7
                exponential(
                    1, 3,
                    HttpStatus.BAD_GATEWAY, HttpStatus.FORBIDDEN
                ),
                10, false, HttpStatus.BAD_GATEWAY
            ),
            Arguments.of(
                // 3 + 6 + 12 = 21
                exponential(
                    3, 3,
                    HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN
                ),
                20, true, HttpStatus.BAD_GATEWAY
            )
        );
    }

    protected void testRetry(
        WireMockServer server,
        MappingBuilder endpoint,
        UnaryOperator<ResponseDefinitionBuilder> successResult,
        int duration,
        boolean enoughTime,
        boolean trackedCode,
        Executable operation,
        HttpStatus failStatus
    ) {
        Timer timer = new Timer(duration);
        Thread serverThread = new Thread(() -> {
            server.stubFor(endpoint.willReturn(
                WireMock.aResponse()
                    .withStatus(failStatus.value())
            ));
            while (!timer.finished()) {
            }
            server.stubFor(endpoint.willReturn(
                successResult.apply(WireMock.aResponse()
                    .withStatus(200))
            ));
        });
        Executable fullOperation = () -> {
            serverThread.start();
            timer.start();
            operation.execute();
            timer.stop();
            serverThread.join();
        };
        if (enoughTime && trackedCode) {
            assertDoesNotThrow(fullOperation);
        } else {
            ServiceException exception = assertThrows(ServiceException.class, fullOperation);
            assertThat(exception.code()).isEqualTo(failStatus);
        }
    }
}
