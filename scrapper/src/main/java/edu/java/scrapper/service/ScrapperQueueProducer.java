package edu.java.scrapper.service;

import edu.java.dto.request.LinkUpdateRequest;
import edu.java.scrapper.configuration.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScrapperQueueProducer {
    private final String topic;
    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @Autowired
    public ScrapperQueueProducer(
        KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate, ApplicationConfig config
    ) {
        this.kafkaTemplate = kafkaTemplate;
        topic = config.kafka().topicName();
    }

    public void send(LinkUpdateRequest update) {
        kafkaTemplate.send(topic, update);
    }
}
