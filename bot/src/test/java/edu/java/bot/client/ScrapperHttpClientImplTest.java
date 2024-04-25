package edu.java.bot.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.configuration.HttpClientConfig;
import edu.java.dto.exception.APIException;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import edu.java.test.client.ClientTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Stream;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScrapperHttpClientImplTest extends ClientTest {
    private WireMockServer wireMockServer;
    private static final int HTTP_ENDPOINT_PORT = getPort();
    private static final String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;
    private static final ScrapperHttpClient SCRAPPER_HTTP_CLIENT =
        new ScrapperHttpClientImpl(WebClient.builder(), URL, RETRY);

    public static Stream<Arguments> testRegisterChat() {
        return Stream.of(
            Arguments.of(
                1L,
                HttpStatus.OK,
                null
            ),
            Arguments.of(
                -1L,
                HttpStatus.BAD_REQUEST,
                """
                    {
                        "description": "Некорректные параметры запроса",
                        "code": "400",
                        "exceptionName": "WrongParametersException",
                        "exceptionMessage": "id cannot be negate",
                        "stacktrace": [
                        ]
                    }
                    """
            )
        );
    }

    public static Stream<Arguments> testDeleteChat() {
        return Stream.of(
            Arguments.of(
                1L,
                HttpStatus.OK,
                null
            ),
            Arguments.of(
                -1L,
                HttpStatus.BAD_REQUEST,
                """
                    {
                        "description": "Некорректные параметры запроса",
                        "code": "400",
                        "exceptionName": "WrongParametersException",
                        "exceptionMessage": "id cannot be negate",
                        "stacktrace": [
                        ]
                    }
                    """
            ),
            Arguments.of(
                11L,
                HttpStatus.NOT_FOUND,
                """
                    {
                      "description": "Чат не существует",
                      "code": "404",
                      "exceptionName": "NonExistentChatException",
                      "exceptionMessage": "there is no chat with id=11",
                      "stacktrace": [
                      ]
                    }"""
            )
        );
    }

    public static Stream<Arguments> testGetAllLinks() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                1L,
                HttpStatus.OK,
                """
                    {
                      "links": [
                        {
                          "link": {
                            "id": 1,
                            "uri": "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                            "lastUpdate": "2024-04-11T11:36:01.737284Z"
                          },
                          "alias": "Tinkoff backend java season 2 pull requests"
                        },
                        {
                          "link": {
                            "id": 2,
                            "uri": "https://stackoverflow.com/questions/11828270/how-do-i-exit-vim",
                            "lastUpdate": "2024-04-11T11:36:36.794418Z"
                          },
                          "alias": "Answers on question 'how to exit vim'"
                        }
                      ],
                      "size": 2
                    }""",
                new ListUserLinkResponse(
                    new UserLinkResponse[] {
                        new UserLinkResponse(new LinkResponse(
                            1L,
                            new URI("https://github.com/MAK-Cpp/backend-java-s2/pulls"),
                            OffsetDateTime.parse("2024-04-11T11:36:01.737284Z")
                        ), "Tinkoff backend java season 2 pull requests"),
                        new UserLinkResponse(new LinkResponse(
                            2L,
                            new URI("https://stackoverflow.com/questions/11828270/how-do-i-exit-vim"),
                            OffsetDateTime.parse("2024-04-11T11:36:36.794418Z")
                        ), "Answers on question 'how to exit vim'")
                    },
                    2
                )
            ),
            Arguments.of(
                -1L,
                HttpStatus.BAD_REQUEST,
                """
                    {
                      "description": "Некорректные параметры запроса",
                      "code": "400",
                      "exceptionName": "WrongParametersException",
                      "exceptionMessage": "id cannot be negate",
                      "stacktrace": [
                      ]
                    }""",
                null
            ),
            Arguments.of(
                11L,
                HttpStatus.NOT_FOUND,
                """
                    {
                      "description": "Чат не существует",
                      "code": "404",
                      "exceptionName": "NonExistentChatException",
                      "exceptionMessage": "there is no chat with id=11",
                      "stacktrace": [
                      ]
                    }""",
                null
            )
        );
    }

    public static Stream<Arguments> testAddLinkToTracking() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                1L,
                "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                "Tinkoff backend java season 2 pull requests",
                HttpStatus.OK,
                """
                    {
                      "link": {
                        "id": 1,
                        "uri": "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                        "lastUpdate": "2024-04-11T11:36:01.737284Z"
                      },
                      "alias": "Tinkoff backend java season 2 pull requests"
                    }""",
                new UserLinkResponse(new LinkResponse(
                    1L,
                    new URI("https://github.com/MAK-Cpp/backend-java-s2/pulls"),
                    OffsetDateTime.parse("2024-04-11T11:36:01.737284Z")
                ), "Tinkoff backend java season 2 pull requests")
            ),
            Arguments.of(
                -1L,
                "asd",
                "alias",
                HttpStatus.BAD_REQUEST,
                """
                    {
                      "description": "Некорректные параметры запроса",
                      "code": "400",
                      "exceptionName": "WrongParametersException",
                      "exceptionMessage": "id cannot be negate",
                      "stacktrace": [
                      ]
                    }""",
                null
            ),
            Arguments.of(
                11L,
                "asd",
                "alias",
                HttpStatus.NOT_FOUND,
                """
                    {
                      "description": "Чат не существует",
                      "code": "404",
                      "exceptionName": "NonExistentChatException",
                      "exceptionMessage": "there is no chat with id=11",
                      "stacktrace": [
                      ]
                    }""",
                null
            )
        );
    }

    public static Stream<Arguments> testRemoveLinkFromTracking() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                1L,
                "Tinkoff backend java season 2 pull requests",
                HttpStatus.OK,
                """
                    {
                      "link": {
                        "id": 1,
                        "uri": "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                        "lastUpdate": "2024-04-11T11:36:01.737284Z"
                      },
                      "alias": "Tinkoff backend java season 2 pull requests"
                    }""",
                new UserLinkResponse(new LinkResponse(
                    1L,
                    new URI("https://github.com/MAK-Cpp/backend-java-s2/pulls"),
                    OffsetDateTime.parse("2024-04-11T11:36:01.737284Z")
                ), "Tinkoff backend java season 2 pull requests")
            ),
            Arguments.of(
                1L,
                "not exists",
                HttpStatus.NOT_FOUND,
                """
                    {
                      "description": "Ссылка не найдена",
                      "code": "404",
                      "exceptionName": "LinkNotFoundException",
                      "exceptionMessage": "there is no link not exists",
                      "stacktrace": [
                      ]
                    }""",
                null
            ),
            Arguments.of(
                -1L,
                "asd",
                HttpStatus.BAD_REQUEST,
                """
                    {
                      "description": "Некорректные параметры запроса",
                      "code": "400",
                      "exceptionName": "WrongParametersException",
                      "exceptionMessage": "id cannot be negate",
                      "stacktrace": [
                      ]
                    }""",
                null
            ),
            Arguments.of(
                11L,
                "asd",
                HttpStatus.NOT_FOUND,
                """
                    {
                      "description": "Чат не существует",
                      "code": "404",
                      "exceptionName": "NonExistentChatException",
                      "exceptionMessage": "there is no chat with id=11",
                      "stacktrace": [
                      ]
                    }""",
                null
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
    public void testRegisterChat(long id, HttpStatus status, String body) {
        MappingBuilder builder = post("/tg-chat/" + id);
        if (status == HttpStatus.OK) {
            stubFor(builder.willReturn(aResponse().withStatus(200)));
            assertDoesNotThrow(() -> SCRAPPER_HTTP_CLIENT.registerChat(id));
        } else {
            stubFor(builder.willReturn(
                aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
            APIException exception = assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.registerChat(id));
            assertThat(exception.getCause().getClass()).isEqualTo(WrongParametersException.class);
            assertThat(exception.code()).isEqualTo(status);
        }
    }

    @ParameterizedTest
    @MethodSource
    public void testDeleteChat(long id, HttpStatus status, String body) {
        MappingBuilder builder = delete("/tg-chat/" + id);
        if (status == HttpStatus.OK) {
            stubFor(builder.willReturn(aResponse().withStatus(200)));
            assertDoesNotThrow(() -> SCRAPPER_HTTP_CLIENT.deleteChat(id));
        } else if (status == HttpStatus.BAD_REQUEST) {
            stubFor(builder.willReturn(
                aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
            APIException exception = assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.deleteChat(id));
            assertThat(exception.getCause().getClass()).isEqualTo(WrongParametersException.class);
            assertThat(exception.code()).isEqualTo(status);
        } else {
            stubFor(builder.willReturn(
                aResponse().withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
            APIException exception = assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.deleteChat(id));
            assertThat(exception.getCause().getClass()).isEqualTo(NonExistentChatException.class);
            assertThat(exception.code()).isEqualTo(status);
        }
    }

    @ParameterizedTest
    @MethodSource
    public void testGetAllLinks(long id, HttpStatus status, String body, ListUserLinkResponse response) {
        MappingBuilder builder = get("/links");
        stubFor(builder.willReturn(aResponse().withStatus(status.value())
            .withHeader("Content-Type", "application/json")
            .withBody(body)));
        if (status == HttpStatus.OK) {
            ListUserLinkResponse out = SCRAPPER_HTTP_CLIENT.getAllLinks(id);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            APIException exception = assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.getAllLinks(id));
            assertThat(exception.getCause().getClass()).isEqualTo(WrongParametersException.class);
            assertThat(exception.code()).isEqualTo(status);
        } else {
            APIException exception = assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.getAllLinks(id));
            assertThat(exception.getCause().getClass()).isEqualTo(NonExistentChatException.class);
            assertThat(exception.code()).isEqualTo(status);
        }
    }

    @ParameterizedTest
    @MethodSource
    public void testAddLinkToTracking(
        long id,
        String link,
        String alias,
        HttpStatus status,
        String body,
        UserLinkResponse response
    ) {
        MappingBuilder builder = post("/links");
        stubFor(builder.willReturn(aResponse().withStatus(status.value())
            .withHeader("Content-Type", "application/json")
            .withBody(body)));
        if (status == HttpStatus.OK) {
            UserLinkResponse out = SCRAPPER_HTTP_CLIENT.addLinkToTracking(id, link, alias);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            APIException exception =
                assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.addLinkToTracking(id, link, alias));
            assertThat(exception.getCause().getClass()).isEqualTo(WrongParametersException.class);
            assertThat(exception.code()).isEqualTo(status);
        } else {
            APIException exception =
                assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.addLinkToTracking(id, link, alias));
            assertThat(exception.getCause().getClass()).isEqualTo(NonExistentChatException.class);
            assertThat(exception.code()).isEqualTo(status);
        }
    }

    @ParameterizedTest
    @MethodSource
    public void testRemoveLinkFromTracking(
        long id,
        String alias,
        HttpStatus status,
        String body,
        UserLinkResponse response
    ) {
        MappingBuilder builder = delete("/links");
        stubFor(builder.willReturn(aResponse().withStatus(status.value())
            .withHeader("Content-Type", "application/json")
            .withBody(body)));
        if (status == HttpStatus.OK) {
            UserLinkResponse out = SCRAPPER_HTTP_CLIENT.removeLinkFromTracking(id, alias);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            APIException exception =
                assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.removeLinkFromTracking(id, alias));
            assertThat(exception.getCause().getClass()).isEqualTo(WrongParametersException.class);
            assertThat(exception.code()).isEqualTo(status);
        } else {
            APIException exception;
            if (Objects.equals(alias, "not exists")) {
                exception =
                    assertThrows(APIException.class, () -> SCRAPPER_HTTP_CLIENT.removeLinkFromTracking(id, alias));
                assertThat(exception.getCause().getClass()).isEqualTo(LinkNotFoundException.class);
            } else {
                exception = assertThrows(
                    APIException.class,
                    () -> SCRAPPER_HTTP_CLIENT.removeLinkFromTracking(id, alias)
                );
                assertThat(exception.getCause().getClass()).isEqualTo(NonExistentChatException.class);
            }
            assertThat(exception.code()).isEqualTo(status);
        }
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryRegisterChat(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        long chatId = 3L;
        final ScrapperHttpClient scrapperHttpClient =
            new ScrapperHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            post("/tg-chat/" + chatId),
            response -> response,
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> scrapperHttpClient.registerChat(chatId),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryDeleteChat(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        long chatId = 3L;
        final ScrapperHttpClient scrapperHttpClient =
            new ScrapperHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            delete("/tg-chat/" + chatId),
            response -> response,
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> scrapperHttpClient.deleteChat(chatId),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetChat(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        long chatId = 428027805L;
        String body = """
            {
              "id": 428027805
            }""";
        final ScrapperHttpClient scrapperHttpClient =
            new ScrapperHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/tg-chat/" + chatId),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> scrapperHttpClient.getChat(chatId),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetAllLinks(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final long chatId = 1L;
        final String body = """
            {
              "links": [
                {
                  "link": {
                    "id": 1,
                    "uri": "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                    "lastUpdate": "2024-04-11T11:36:01.737284Z"
                  },
                  "alias": "Tinkoff backend java season 2 pull requests"
                },
                {
                  "link": {
                    "id": 2,
                    "uri": "https://stackoverflow.com/questions/11828270/how-do-i-exit-vim",
                    "lastUpdate": "2024-04-11T11:36:36.794418Z"
                  },
                  "alias": "Answers on question 'how to exit vim'"
                }
              ],
              "size": 2
            }""";
        final ScrapperHttpClient scrapperHttpClient =
            new ScrapperHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/links"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> scrapperHttpClient.getAllLinks(chatId),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetLinkByChatIdAndAlias(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final long chatId = 1L;
        final String alias = "Tinkoff backend java season 2 pull requests";
        final String body = """
            {
              "link": {
                "id": 1,
                "uri": "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                "lastUpdate": "2024-04-11T11:36:01.737284Z"
              },
              "alias": "Tinkoff backend java season 2 pull requests"
            }""";
        final ScrapperHttpClient scrapperHttpClient =
            new ScrapperHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/link"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> scrapperHttpClient.getLinkByChatIdAndAlias(chatId, alias),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryAddLinkToTracking(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final long chatId = 1L;
        final String link = "https://github.com/MAK-Cpp/backend-java-s2/pulls";
        final String alias = "Tinkoff backend java season 2 pull requests";
        final String body = """
            {
              "link": {
                "id": 1,
                "uri": "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                "lastUpdate": "2024-04-11T11:36:01.737284Z"
              },
              "alias": "Tinkoff backend java season 2 pull requests"
            }""";
        final ScrapperHttpClient scrapperHttpClient =
            new ScrapperHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            post("/links"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> scrapperHttpClient.addLinkToTracking(chatId, link, alias),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryRemoveLinkFromTracking(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final long chatId = 1L;
        final String alias = "Tinkoff backend java season 2 pull requests";
        final String body = """
            {
              "link": {
                "id": 1,
                "uri": "https://github.com/MAK-Cpp/backend-java-s2/pulls",
                "lastUpdate": "2024-04-11T11:36:01.737284Z"
              },
              "alias": "Tinkoff backend java season 2 pull requests"
            }""";
        final ScrapperHttpClient scrapperHttpClient =
            new ScrapperHttpClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            delete("/links"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> scrapperHttpClient.removeLinkFromTracking(chatId, alias),
            failStatus
        );
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }
}
