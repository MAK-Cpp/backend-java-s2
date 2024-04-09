package edu.java.bot.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
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
                    "      \"id\": 1,\n" +
                    "      \"url\": \"https://github.com/MAK-Cpp/backend-java-s2\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 2,\n" +
                    "      \"url\": \"https://stackoverflow.com/questions/11828270/how-do-i-exit-vim\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"size\": 2\n" +
                    "}",
                new ListLinkResponse(
                    new LinkResponse[] {
                        new LinkResponse(1L, new URI("https://github.com/MAK-Cpp/backend-java-s2")),
                        new LinkResponse(2L, new URI("https://stackoverflow.com/questions/11828270/how-do-i-exit-vim"))
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
                "t.me/MAK_Cpp",
                HttpStatus.OK,
                "{\n" +
                    "  \"id\": 1,\n" +
                    "  \"url\": \"t.me/MAK_Cpp\"\n" +
                    "}",
                new LinkResponse(1L, new URI("t.me/MAK_Cpp"))
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

    public static Stream<Arguments> testRemoveLinkFromTracking() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                1L,
                "YouTube.com",
                HttpStatus.OK,
                "{\n" +
                    "  \"id\": 1,\n" +
                    "  \"url\": \"YouTube.com\"\n" +
                    "}",
                new LinkResponse(1L, new URI("YouTube.com"))
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
    public void testGetAllLinks(long id, HttpStatus status, String body, ListLinkResponse response) {
        MappingBuilder builder = get("/links");
        stubFor(builder.willReturn(aResponse().withStatus(status.value())
            .withHeader("Content-Type", "application/json")
            .withBody(body)));
        if (status == HttpStatus.OK) {
            ListLinkResponse out = scrapperHttpClient.getAllLinks(id);
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
        HttpStatus status,
        String body,
        LinkResponse response
    ) {
        MappingBuilder builder = post("/links");
        stubFor(builder.willReturn(aResponse().withStatus(status.value())
            .withHeader("Content-Type", "application/json")
            .withBody(body)));
        if (status == HttpStatus.OK) {
            LinkResponse out = scrapperHttpClient.addLinkToTracking(id, link);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            assertThrows(WrongParametersException.class, () -> scrapperHttpClient.addLinkToTracking(id, link));
        } else {
            assertThrows(NonExistentChatException.class, () -> scrapperHttpClient.addLinkToTracking(id, link));
        }
    }

    @ParameterizedTest
    @MethodSource
    public void testRemoveLinkFromTracking(
        long id,
        String link,
        HttpStatus status,
        String body,
        LinkResponse response
    ) {
        MappingBuilder builder = delete("/links");
        stubFor(builder.willReturn(aResponse().withStatus(status.value())
            .withHeader("Content-Type", "application/json")
            .withBody(body)));
        if (status == HttpStatus.OK) {
            LinkResponse out = scrapperHttpClient.removeLinkFromTracking(id, link);
            assertThat(out).isEqualTo(response);
        } else if (status == HttpStatus.BAD_REQUEST) {
            assertThrows(WrongParametersException.class, () -> scrapperHttpClient.removeLinkFromTracking(id, link));
        } else {
            if (Objects.equals(link, "not exists")) {
                assertThrows(LinkNotFoundException.class, () -> scrapperHttpClient.removeLinkFromTracking(id, link));
            } else {
                assertThrows(NonExistentChatException.class, () -> scrapperHttpClient.removeLinkFromTracking(id, link));
            }
        }
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }
}
