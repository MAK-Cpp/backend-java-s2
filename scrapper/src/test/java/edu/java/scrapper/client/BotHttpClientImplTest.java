package edu.java.scrapper.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.configuration.HttpClientConfig;
import edu.java.dto.exception.ServiceException;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.scrapper.client.bot.BotHttpClient;
import edu.java.scrapper.client.bot.BotHttpClientImpl;
import edu.java.test.client.ClientTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotHttpClientImplTest extends ClientTest {
    private WireMockServer wireMockServer;
    private static final int HTTP_ENDPOINT_PORT = getPort();
    public static final String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;
    private static final BotHttpClient
        BOT_HTTP_CLIENT = new BotHttpClientImpl(WebClient.builder(), URL, RETRY);

    public static Stream<Arguments> testSendUpdates() {
        return Stream.of(
            Arguments.of(
                3L,
                "GitHub.com",
                "remote repositories",
                List.of(
                    Map.entry(1L, "link1"),
                    Map.entry(2L, "link2"),
                    Map.entry(3L, "link3"),
                    Map.entry(4L, "link4"),
                    Map.entry(5L, "link5")
                ),
                HttpStatus.OK,
                null
            ),
            Arguments.of(
                -1L,
                "GitHub.com",
                "remote repositories",
                List.of(
                    Map.entry(1L, "link1"),
                    Map.entry(2L, "link2"),
                    Map.entry(3L, "link3"),
                    Map.entry(4L, "link4"),
                    Map.entry(5L, "link5")
                ),
                HttpStatus.BAD_REQUEST,
                """
                    {
                      "description": "Некорректные параметры запроса",
                      "code": "400",
                      "exceptionName": "WrongParametersException",
                      "exceptionMessage": "id cannot be negate",
                      "stacktrace": [
                      ]
                    }"""
            ),
            Arguments.of(
                1L,
                "",
                "",
                List.of(),
                HttpStatus.BAD_REQUEST,
                """
                    {
                      "description": "Некорректные параметры запроса",
                      "code": "400",
                      "exceptionName": "WrongParametersException",
                      "exceptionMessage": "link cannot be empty",
                      "stacktrace": [
                      ]
                    }"""
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
        Long id,
        String url,
        String description,
        List<Map.Entry<Long, String>> chatsAndAliases,
        HttpStatus status,
        String body
    ) {
        LinkUpdateRequest request = new LinkUpdateRequest(
            id, url, description, chatsAndAliases.stream()
            .map(x -> new LinkUpdateRequest.ChatAndAlias(x.getKey(), x.getValue()))
            .toArray(LinkUpdateRequest.ChatAndAlias[]::new)
        );
        MappingBuilder builder = post("/updates");
        if (status == HttpStatus.OK) {
            stubFor(builder.willReturn(aResponse().withStatus(200)));
            assertDoesNotThrow(() -> BOT_HTTP_CLIENT.sendUpdates(request));
        } else {
            stubFor(builder.willReturn(aResponse().withStatus(status.value())
                .withHeader("Content-Type", "application/json")
                .withBody(body)));
            assertThrows(
                ServiceException.class,
                () -> BOT_HTTP_CLIENT.sendUpdates(request)
            );
        }
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetrySendUpdates(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final Long id = 3L;
        final String url = "GitHub.com";
        final String description = "remote repositories";
        final List<Map.Entry<Long, String>> chatsAndAliases = List.of(
            Map.entry(1L, "link1"),
            Map.entry(2L, "link2"),
            Map.entry(3L, "link3"),
            Map.entry(4L, "link4"),
            Map.entry(5L, "link5")
        );
        LinkUpdateRequest request = new LinkUpdateRequest(
            id, url, description, chatsAndAliases.stream()
            .map(x -> new LinkUpdateRequest.ChatAndAlias(x.getKey(), x.getValue()))
            .toArray(LinkUpdateRequest.ChatAndAlias[]::new)
        );
        final BotHttpClient botHttpClient = new BotHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            post("/updates"),
            response -> response,
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> botHttpClient.sendUpdates(request),
            failStatus
        );
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }
}
