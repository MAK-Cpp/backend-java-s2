package edu.java.bot.service;

import edu.java.dto.exception.DTOException;
import edu.java.dto.request.LinkUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "app.kafka", name = "enable", havingValue = "true")
public class LinkUpdatesKafkaListener {
    private final BotService botService;

    @Autowired
    public LinkUpdatesKafkaListener(BotService botService) {
        log.info("created LinkUpdatesKafkaListener");
        this.botService = botService;
    }

    @RetryableTopic(
        dltTopicSuffix = "_dlq",
        attempts = "1",
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        kafkaTemplate = "dlqKafkaTemplate",
        listenerContainerFactory = "updatesKafkaListenerContainerFactory",
        include = DTOException.class
    )
    @KafkaListener(
        id = "${app.kafka.group-id}",
        topics = "${app.kafka.topic-name}",
        containerFactory = "updatesKafkaListenerContainerFactory"
    )
    public void listen(LinkUpdateRequest update) {
        log.debug("Received link update from kafka: {}", update);
        botService.updateLink(update);
    }

    @DltHandler
    public void handleDlt(LinkUpdateRequest updateRequest) {
        log.debug("Failed update request: {}", updateRequest);
    }
}
