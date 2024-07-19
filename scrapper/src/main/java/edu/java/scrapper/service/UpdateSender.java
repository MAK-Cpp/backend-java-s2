package edu.java.scrapper.service;

import edu.java.dto.request.LinkUpdateRequest;
import edu.java.scrapper.client.bot.BotHttpClient;
import edu.java.scrapper.configuration.ApplicationConfig;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UpdateSender {
    private final Consumer<LinkUpdateRequest> sendFunction;

    @Autowired
    public UpdateSender(
        BotHttpClient botHttpClient,
        ScrapperQueueProducer scrapperQueueProducer,
        ApplicationConfig applicationConfig
    ) {
        sendFunction = applicationConfig.kafka().enable() ? scrapperQueueProducer::send : botHttpClient::sendUpdates;
    }

    public void sendUpdate(
        Long id,
        String url,
        String description,
        List<Map.Entry<Long, String>> chatsAndAliases
    ) {
        LinkUpdateRequest request = new LinkUpdateRequest(
            id, url, description,
            chatsAndAliases.stream()
                .map(x -> new LinkUpdateRequest.ChatAndAlias(x.getKey(), x.getValue()))
                .toArray(LinkUpdateRequest.ChatAndAlias[]::new)
        );
        log.debug("Sending link update: {}", request);
        sendFunction.accept(request);
    }
}
