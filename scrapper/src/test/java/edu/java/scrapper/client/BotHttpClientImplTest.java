package edu.java.scrapper.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.exception.WrongParametersException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import java.util.stream.Stream;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BotHttpClientImplTest {
    private WireMockServer wireMockServer;
    private static final int HTTP_ENDPOINT_PORT = 8123;
    private static final String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;
    private static final BotHttpClient botHttpClient = new BotHttpClientImpl(URL);

    public static Stream<Arguments> testSendUpdates() {
        return Stream.of(
            Arguments.of(
                3L,
                "GitHub.com",
                "remote repositories",
                new long[] {1, 2, 3, 4, 5},
                HttpStatus.OK,
                null
            ),
            Arguments.of(
                -1L,
                "GitHub.com",
                "remote repositories",
                new long[] {1, 2, 3, 4, 5},
                HttpStatus.BAD_REQUEST,
                "{\n" +
                    "  \"description\": \"Некорректные параметры запроса\",\n" +
                    "  \"code\": \"400\",\n" +
                    "  \"exceptionName\": \"WrongParametersException\",\n" +
                    "  \"exceptionMessage\": \"id cannot be negate\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}"
            ),
            Arguments.of(
                1L,
                "",
                "",
                new long[] {},
                HttpStatus.BAD_REQUEST,
                "{\n" +
                    "  \"description\": \"Некорректные параметры запроса\",\n" +
                    "  \"code\": \"400\",\n" +
                    "  \"exceptionName\": \"WrongParametersException\",\n" +
                    "  \"exceptionMessage\": \"link cannot be empty\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}"
            )
        );
    }

    @BeforeEach
    public void beforeEach() {
        wireMockServer = new WireMockServer(HTTP_ENDPOINT_PORT);
        wireMockServer.start();
        configureFor("localhost", HTTP_ENDPOINT_PORT);
    }

    @ParameterizedTest
    @MethodSource
    public void testSendUpdates(
        long id,
        String url,
        String description,
        long[] tgChatIds,
        HttpStatus status,
        String body
    ) {
        MappingBuilder builder = post("/updates");
        if (status == HttpStatus.OK) {
            stubFor(builder.willReturn(aResponse().withStatus(200)));
            assertDoesNotThrow(() -> botHttpClient.sendUpdates(id, url, description, tgChatIds));
        } else {
            stubFor(builder.willReturn(aResponse().withStatus(status.value())
                .withHeader("Content-Type", "application/json")
                .withBody(body)));
            assertThrows(
                WrongParametersException.class,
                () -> botHttpClient.sendUpdates(id, url, description, tgChatIds)
            );
        }
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }
}
