package edu.java.scrapper.scheduler;

import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.UpdateSender;
import edu.java.scrapper.validator.LinkValidator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.scheduler", name = "enable", havingValue = "true")
public class LinkUpdaterScheduler {
    private final LinkService linkService;
    private final ChatService chatService;
    private final UpdateSender updateSender;
    private final LinkUpdater linkUpdater;
    private final List<LinkValidator> linkValidators;

    @Autowired
    public LinkUpdaterScheduler(
        LinkService linkService,
        ChatService chatService,
        UpdateSender updateSender, LinkUpdater linkUpdater,
        List<LinkValidator> linkValidators
    ) {
        this.linkService = linkService;
        this.chatService = chatService;
        this.updateSender = updateSender;
        this.linkUpdater = linkUpdater;
        this.linkValidators = linkValidators;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    void update() {
        log.debug("scheduled update call");
        ListLinkResponse links = linkService.getAllLinks();
        for (LinkResponse link : links.getLinks()) {
            for (LinkValidator linkValidator : linkValidators) {
                final Optional<String> update =
                    linkValidator.getUpdateDescription(link.getUri().toString(), link.getLastUpdate());
                if (update.isPresent()) {
                    final Long linkId = link.getId();
                    final ChatResponse[] chats = chatService.getAllChats(linkId).getChats();
                    final List<Map.Entry<Long, String>> chatIds =
                        Arrays.stream(chats)
                            .map(chat -> {
                                final Long chatId = chat.getId();
                                final String alias = chatService.getLinkAlias(chatId, linkId).getAlias();
                                return Map.entry(chatId, alias);
                            })
                            .toList();
                    updateSender.sendUpdate(linkId, link.getUri().toString(), update.get(), chatIds);
                    linkUpdater.updateLink(linkId);
                }
            }
        }
    }
}
