package edu.java.bot.service;

import edu.java.bot.configuration.TestConfig;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.request.LinkUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

@Import(TestConfig.class)
@SpringBootTest(properties = {
    "spring.kafka.admin.auto-create=true"
})
@DirtiesContext
@Slf4j
public class LinkUpdatesKafkaListenerTest extends KafkaIntegrationTest {
    @SpyBean
    private LinkUpdatesKafkaListener linkUpdatesKafkaListener;

    @Qualifier("kafkaTemplate")
    @Autowired
    private KafkaTemplate<String, LinkUpdateRequest> producer;

    @Value("${app.kafka.topic-name}")
    private String topic;

    @MockBean
    private BotService botService;

    private LinkUpdateRequest result;

    public static Stream<Arguments> testLinkUpdatesKafkaListener() {
        return Stream.of(
            Arguments.of(new LinkUpdateRequest(
                1L,
                "https://google.com",
                "description",
                new LinkUpdateRequest.ChatAndAlias[] {new LinkUpdateRequest.ChatAndAlias(1L, "alias")}
            ), true),
            Arguments.of(new LinkUpdateRequest(
                -1L,
                "https://google.com",
                "description",
                new LinkUpdateRequest.ChatAndAlias[] {new LinkUpdateRequest.ChatAndAlias(1L, "alias")}
            ), false)
        );
    }

    @BeforeEach
    public void setUp() {
        result = null;
    }

    @ParameterizedTest
    @MethodSource
    public void testLinkUpdatesKafkaListener(LinkUpdateRequest request, boolean correct) throws Exception {
        if (correct) {
            Mockito.doAnswer(x -> {
                result = x.getArgument(0);
                return null;
            }).when(botService).updateLink(Mockito.any(LinkUpdateRequest.class));
            log.info("sending link update {} to {}", request, topic);
            producer.send(topic, request);
            Thread.sleep(1000);
            log.info("result {}", result);
            Mockito.verify(botService).updateLink(request);
            assertThat(result).isEqualTo(request);
        } else {
            Mockito.doThrow(new WrongParametersException("wrong parameters"))
                .when(botService)
                .updateLink(Mockito.any(LinkUpdateRequest.class));
            log.info("sending link update {} to {}", request, topic);
            producer.send(topic, request);
            Thread.sleep(1000);
            log.info("result {}", result);
            Mockito.verify(linkUpdatesKafkaListener).handleDlt(request);
        }
    }
}
