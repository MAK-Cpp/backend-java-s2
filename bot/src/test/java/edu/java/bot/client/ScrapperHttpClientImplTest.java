package edu.java.bot.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
class ScrapperHttpClientImplTest {
    private WireMockServer wireMockServer;
    private static final int HTTP_ENDPOINT_PORT = 8123;
    private static final String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;
    private static final ScrapperHttpClient scrapperHttpClient = new ScrapperHttpClientImpl(WebClient.builder(), URL);

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
                "{\n" +
                    "    \"description\": \"Некорректные параметры запроса\",\n" +
                    "    \"code\": \"400\",\n" +
                    "    \"exceptionName\": \"WrongParametersException\",\n" +
                    "    \"exceptionMessage\": \"id cannot be negate\",\n" +
                    "    \"stacktrace\": [\n" +
                    "    ]\n" +
                    "}\n"
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
                "{\n" +
                    "    \"description\": \"Некорректные параметры запроса\",\n" +
                    "    \"code\": \"400\",\n" +
                    "    \"exceptionName\": \"WrongParametersException\",\n" +
                    "    \"exceptionMessage\": \"id cannot be negate\",\n" +
                    "    \"stacktrace\": [\n" +
                    "    ]\n" +
                    "}\n"
            ),
            Arguments.of(
                11L,
                HttpStatus.NOT_FOUND,
                "{\n" +
                    "  \"description\": \"Чат не существует\",\n" +
                    "  \"code\": \"404\",\n" +
                    "  \"exceptionName\": \"NonExistentChatException\",\n" +
                    "  \"exceptionMessage\": \"there is no chat with id=11\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}"
            )
        );
    }

    public static Stream<Arguments> testGetAllLinks() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                1L,
                HttpStatus.OK,
                "{\n" +
                    "  \"links\": [\n" +
                    "    {\n" +
                    "      \"link\": {\n" +
                    "        \"id\": 1,\n" +
                    "        \"uri\": \"https://github.com/MAK-Cpp/backend-java-s2/pulls\",\n" +
                    "        \"lastUpdate\": \"2024-04-11T11:36:01.737284Z\"\n" +
                    "      },\n" +
                    "      \"alias\": \"Tinkoff backend java season 2 pull requests\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"link\": {\n" +
                    "        \"id\": 2,\n" +
                    "        \"uri\": \"https://stackoverflow.com/questions/11828270/how-do-i-exit-vim\",\n" +
                    "        \"lastUpdate\": \"2024-04-11T11:36:36.794418Z\"\n" +
                    "      },\n" +
                    "      \"alias\": \"Answers on question 'how to exit vim'\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"size\": 2\n" +
                    "}",
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
                "{\n" +
                    "  \"description\": \"Некорректные параметры запроса\",\n" +
                    "  \"code\": \"400\",\n" +
                    "  \"exceptionName\": \"WrongParametersException\",\n" +
                    "  \"exceptionMessage\": \"id cannot be negate\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}",
                null
            ),
            Arguments.of(
                11L,
                HttpStatus.NOT_FOUND,
                "{\n" +
                    "  \"description\": \"Чат не существует\",\n" +
                    "  \"code\": \"404\",\n" +
                    "  \"exceptionName\": \"NonExistentChatException\",\n" +
                    "  \"exceptionMessage\": \"there is no chat with id=11\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}",
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
                "{\n" +
                    "  \"link\": {\n" +
                    "    \"id\": 1,\n" +
                    "    \"uri\": \"https://github.com/MAK-Cpp/backend-java-s2/pulls\",\n" +
                    "    \"lastUpdate\": \"2024-04-11T11:36:01.737284Z\"\n" +
                    "  },\n" +
                    "  \"alias\": \"Tinkoff backend java season 2 pull requests\"\n" +
                    "}",
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
                "{\n" +
                    "  \"description\": \"Некорректные параметры запроса\",\n" +
                    "  \"code\": \"400\",\n" +
                    "  \"exceptionName\": \"WrongParametersException\",\n" +
                    "  \"exceptionMessage\": \"id cannot be negate\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}",
                null
            ),
            Arguments.of(
                11L,
                "asd",
                "alias",
                HttpStatus.NOT_FOUND,
                "{\n" +
                    "  \"description\": \"Чат не существует\",\n" +
                    "  \"code\": \"404\",\n" +
                    "  \"exceptionName\": \"NonExistentChatException\",\n" +
                    "  \"exceptionMessage\": \"there is no chat with id=11\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}",
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
                "{\n" +
                    "  \"link\": {\n" +
                    "    \"id\": 1,\n" +
                    "    \"uri\": \"https://github.com/MAK-Cpp/backend-java-s2/pulls\",\n" +
                    "    \"lastUpdate\": \"2024-04-11T11:36:01.737284Z\"\n" +
                    "  },\n" +
                    "  \"alias\": \"Tinkoff backend java season 2 pull requests\"\n" +
                    "}",
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
                "{\n" +
                    "  \"description\": \"Ссылка не найдена\",\n" +
                    "  \"code\": \"404\",\n" +
                    "  \"exceptionName\": \"LinkNotFoundException\",\n" +
                    "  \"exceptionMessage\": \"there is no link not exists\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}",
                null
            ),
            Arguments.of(
                -1L,
                "asd",
                HttpStatus.BAD_REQUEST,
                "{\n" +
                    "  \"description\": \"Некорректные параметры запроса\",\n" +
                    "  \"code\": \"400\",\n" +
                    "  \"exceptionName\": \"WrongParametersException\",\n" +
                    "  \"exceptionMessage\": \"id cannot be negate\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}",
                null
            ),
            Arguments.of(
                11L,
                "asd",
                HttpStatus.NOT_FOUND,
                "{\n" +
                    "  \"description\": \"Чат не существует\",\n" +
                    "  \"code\": \"404\",\n" +
                    "  \"exceptionName\": \"NonExistentChatException\",\n" +
                    "  \"exceptionMessage\": \"there is no chat with id=11\",\n" +
                    "  \"stacktrace\": [\n" +
                    "  ]\n" +
                    "}",
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
            assertDoesNotThrow(() -> scrapperHttpClient.registerChat(id));
        } else {
            stubFor(builder.willReturn(
                aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
            assertThrows(WrongParametersException.class, () -> scrapperHttpClient.registerChat(id));
        }
    }

    @ParameterizedTest
    @MethodSource
    public void testDeleteChat(long id, HttpStatus status, String body) {
        MappingBuilder builder = delete("/tg-chat/" + id);
        if (status == HttpStatus.OK) {
            stubFor(builder.willReturn(aResponse().withStatus(200)));
            assertDoesNotThrow(() -> scrapperHttpClient.deleteChat(id));
        } else if (status == HttpStatus.BAD_REQUEST) {
            stubFor(builder.willReturn(
                aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
            assertThrows(WrongParametersException.class, () -> scrapperHttpClient.deleteChat(id));
        } else {
            stubFor(builder.willReturn(
                aResponse().withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
            assertThrows(NonExistentChatException.class, () -> scrapperHttpClient.deleteChat(id));
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
            ListUserLinkResponse out = scrapperHttpClient.getAllLinks(id);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            assertThrows(WrongParametersException.class, () -> scrapperHttpClient.getAllLinks(id));
        } else {
            assertThrows(NonExistentChatException.class, () -> scrapperHttpClient.getAllLinks(id));
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
            UserLinkResponse out = scrapperHttpClient.addLinkToTracking(id, link, alias);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            assertThrows(WrongParametersException.class, () -> scrapperHttpClient.addLinkToTracking(id, link, alias));
        } else {
            assertThrows(NonExistentChatException.class, () -> scrapperHttpClient.addLinkToTracking(id, link, alias));
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
            UserLinkResponse out = scrapperHttpClient.removeLinkFromTracking(id, alias);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            assertThrows(WrongParametersException.class, () -> scrapperHttpClient.removeLinkFromTracking(id, alias));
        } else {
            if (Objects.equals(alias, "not exists")) {
                assertThrows(LinkNotFoundException.class, () -> scrapperHttpClient.removeLinkFromTracking(id, alias));
            } else {
                assertThrows(
                    NonExistentChatException.class,
                    () -> scrapperHttpClient.removeLinkFromTracking(id, alias)
                );
            }
        }
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }
}
