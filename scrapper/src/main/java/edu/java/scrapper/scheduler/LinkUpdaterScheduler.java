package edu.java.scrapper.scheduler;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.scrapper.client.bot.BotHttpClient;
import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.validator.LinkValidator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@Slf4j
public class LinkUpdaterScheduler {
    private final LinkService linkService;
    private final ChatService chatService;
    private final BotHttpClient botHttpClient;
    private final LinkUpdater linkUpdater;
    private final List<LinkValidator> linkValidators;

    @Autowired
    public LinkUpdaterScheduler(
        LinkService linkService,
        ChatService chatService,
        BotHttpClient botHttpClient, LinkUpdater linkUpdater,
        List<LinkValidator> linkValidators
    ) {
        this.linkService = linkService;
        this.chatService = chatService;
        this.botHttpClient = botHttpClient;
        this.linkUpdater = linkUpdater;
        this.linkValidators = linkValidators;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    void update() {
        log.debug("scheduled update call");
        ListLinkResponse links = linkService.getAllLinks();
        for (LinkResponse link : links.getLinks()) {
            // TODO: maybe contain in DB Class<? extends LinkValidator> for every link
            for (LinkValidator linkValidator : linkValidators) {
                final Optional<String> update =
                    linkValidator.getUpdateDescription(link.getUri().toString(), link.getLastUpdate());
                if (update.isPresent()) {
                    log.debug("updating link {}", link.getUri().toString());
                    final Long linkId = link.getId();
                    final List<Map.Entry<Long, String>> chatIds =
                        Arrays.stream(chatService.getAllChats(linkId).getChats())
                            .map(chat -> {
                                final Long chatId = chat.getId();
                                final String alias = chatService.getLinkAlias(chatId, linkId).getAlias();
                                return Map.entry(chatId, alias);
                            })
                            .toList();
                    botHttpClient.sendUpdates(linkId, link.getUri().toString(), update.get(), chatIds);
                    linkUpdater.updateLink(linkId);
                }
            }
        }
    }
}
