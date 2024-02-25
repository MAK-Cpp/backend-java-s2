package edu.java.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import edu.java.response.Answer;
import edu.java.response.AnswerOwner;
import edu.java.response.AnswerResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

class StackOverflowClientImplTest {

    private static final StackOverflowClient stackOverflowClient = new StackOverflowClientImpl();

    public static Stream<Arguments> testGetQuestionAnswers() {
        return Stream.of(
            Arguments.of(
                1,
                "{\n" +
                    "    \"items\": [\n" +
                    "        {\n" +
                    "            \"owner\": {\n" +
                    "                \"account_id\": 6691602,\n" +
                    "                \"reputation\": 989,\n" +
                    "                \"user_id\": 5160940,\n" +
                    "                \"user_type\": \"registered\",\n" +
                    "                \"profile_image\": \"https://i.stack.imgur.com/4w35p.jpg?s=256&g=1\",\n" +
                    "                \"display_name\": \"DockYard\",\n" +
                    "                \"link\": \"https://stackoverflow.com/users/5160940/dockyard\"\n" +
                    "            },\n" +
                    "            \"is_accepted\": false,\n" +
                    "            \"score\": 1,\n" +
                    "            \"last_activity_date\": 1693142293,\n" +
                    "            \"creation_date\": 1693142293,\n" +
                    "            \"answer_id\": 76987303,\n" +
                    "            \"question_id\": 65954571,\n" +
                    "            \"content_license\": \"CC BY-SA 4.0\",\n" +
                    "            \"body_markdown\": \"Assuming you are facing this issue in your local Apple M1/M2, and if you don&#39;t want to add any additional dependencies then you can use this below solution:\\r\\n\\r\\nStep 1)Download any IntelX64 based JDK distribution.\\r\\n\\r\\nSample download source:\\r\\nhttps://learn.microsoft.com/en-us/java/openjdk/download(In my case, I was using jdk11 hence I downloaded ```microsoft-jdk-11.0.20-macOS-x64.tar.gz``` from this page)\\r\\n\\r\\n\\r\\nStep 2)Set your java to this new jdk from your IDE(Intellij Idea for my case)\\r\\n\\r\\n\\r\\n\\r\\n\\r\\nThis worked like a charm for me\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"has_more\": false,\n" +
                    "    \"quota_max\": 300,\n" +
                    "    \"quota_remaining\": 241\n" +
                    "}\n",
                new AnswerResponse(List.of(
                    new Answer(
                        76987303,
                        65954571,
                        "Assuming you are facing this issue in your local Apple M1/M2, and if you don&#39;t want to add any additional dependencies then you can use this below solution:\\r\\n\\r\\nStep 1)Download any IntelX64 based JDK distribution.\\r\\n\\r\\nSample download source:\\r\\nhttps://learn.microsoft.com/en-us/java/openjdk/download(In my case, I was using jdk11 hence I downloaded ```microsoft-jdk-11.0.20-macOS-x64.tar.gz``` from this page)\\r\\n\\r\\n\\r\\nStep 2)Set your java to this new jdk from your IDE(Intellij Idea for my case)\\r\\n\\r\\n\\r\\n\\r\\n\\r\\nThis worked like a charm for me",
                        OffsetDateTime.of(2023, 8, 27, 13, 18, 13, 0, ZoneOffset.UTC),
                        new AnswerOwner(
                            "https://stackoverflow.com/users/5160940/dockyard",
                            "DockYard",
                            989,
                            5160940
                        )
                    )
                ))
            )
        );
    }

    @BeforeEach
    public void beforeEach() {

    }

    @ParameterizedTest
    @MethodSource
    public void testGetQuestionAnswers(int questionId, String body, AnswerResponse result) {
        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();
        wireMockServer.stop();
//        stubFor(get("/questions/" + questionId + "/answers")
//            .willReturn(aResponse()
//                .withStatus(200)
//                .withBody(body)));
//        AnswerResponse output = stackOverflowClient.getQuestionAnswers(questionId).block();
//        assertThat(output).isEqualTo(result);
    }

    @AfterEach
    public void afterEach() {
    }
}
