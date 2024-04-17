package edu.java.bot.service;

import edu.java.bot.TelegramBotComponent;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.request.LinkUpdateRequest;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static edu.java.bot.request.chains.SendMessageChains.SM_DISABLE_PREVIEW;
import static edu.java.bot.request.chains.SendMessageChains.SM_MARKDOWN;

@Service
@Slf4j
public class BotService {
    private final TelegramBotComponent telegramBot;

    @Autowired
    public BotService(TelegramBotComponent telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void updateLink(LinkUpdateRequest request) {
        if (Objects.equals(request.getUrl(), "")) {
            throw new WrongParametersException("link cannot be empty");
        } else if (request.getId() < 0) {
            throw new WrongParametersException("id cannot be negate");
        }
        log.debug("request processed: {}", request);
        for (LinkUpdateRequest.ChatAndAlias chatAndAlias : request.getChatsAndAliases()) {
            final String chatDescription = String.format("New update for link [%s](%s):\n%s",
                request.getUrl(),
                chatAndAlias.getAlias(),
                request.getDescription()
            );
            telegramBot.sendMessage(chatAndAlias.getId(), chatDescription, SM_MARKDOWN, SM_DISABLE_PREVIEW);
        }
    }
}
