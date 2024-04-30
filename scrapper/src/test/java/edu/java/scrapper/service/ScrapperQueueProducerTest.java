package edu.java.scrapper.service;

import edu.java.dto.request.LinkUpdateRequest;
import edu.java.scrapper.IntegrationTest;
import edu.java.scrapper.configuration.KafkaTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.stream.Stream;

@SpringBootTest
@DirtiesContext
@Slf4j
@Import(KafkaTestConfig.class)
public class ScrapperQueueProducerTest extends IntegrationTest {
    @Autowired
    private ScrapperQueueProducer scrapperQueueProducer;

    @SpyBean
    private KafkaTestConfig.TestConsumer testConsumer;

    public static Stream<Arguments> testScrapperQueueProducer() {
        return Stream.of(
            Arguments.of(new LinkUpdateRequest(
                1L,
                "https://google.com",
                "description",
                new LinkUpdateRequest.ChatAndAlias[] {new LinkUpdateRequest.ChatAndAlias(1L, "alias")}
            )),
            Arguments.of(new LinkUpdateRequest(
                -1L,
                "https://google.com",
                "description",
                new LinkUpdateRequest.ChatAndAlias[] {new LinkUpdateRequest.ChatAndAlias(1L, "alias")}
            ))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testScrapperQueueProducer(LinkUpdateRequest linkUpdateRequest) throws InterruptedException {
        scrapperQueueProducer.send(linkUpdateRequest);
        Thread.sleep(1000);
        Mockito.verify(testConsumer).listen(linkUpdateRequest);
    }
}
