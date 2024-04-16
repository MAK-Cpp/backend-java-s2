package edu.java.bot.command;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.client.ScrapperHttpClient;
import edu.java.dto.exception.DTOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import static edu.java.bot.request.chains.SendMessageChains.SM_DISABLE_PREVIEW;
import static edu.java.bot.request.chains.SendMessageChains.SM_MARKDOWN;

@Component
public class Track extends Command {
    public static final String DESCRIPTION_MESSAGE =
        "Send link(s) for tracking\nFormat:\nlink_alias1 - link1\nlink_alias2 - link2\n...";

    public static String createResult(final List<Map.Entry<String, Optional<String>>> results) {
        final StringBuilder message = new StringBuilder("Result:\n");
        int succeed = 0;
        for (Map.Entry<String, Optional<String>> result : results) {
            final String line = result.getKey();
            final Optional<String> exceptionMessage = result.getValue();
            if (exceptionMessage.isEmpty()) {
                succeed++;
                message.append(line).append(" now tracking");
            } else {
                message.append(line).append(" ERROR: ").append(exceptionMessage.get());
            }
            message.append('\n');
        }
        message.append("\nSUCCEED: ").append(succeed).append("\nFAILED: ").append(results.size() - succeed);
        return message.toString();
    }

    /*package-private*/
    static CommandFunction parseLinks(TelegramBotComponent bot, Update update) {
        final ScrapperHttpClient scrapperHttpClient = bot.getScrapperHttpClient();
        final long chatId = update.message().chat().id();
        final String messageText = update.message().text();
        List<Map.Entry<String, Optional<String>>> results = new ArrayList<>();
        messageText.lines().forEach(line -> {
            Optional<Link> optionalLink = Link.parse(line);
            if (optionalLink.isEmpty()) {
                results.add(Map.entry(line, Optional.of("cannot be parsed, read instruction again")));
            } else {
                Link link = optionalLink.get();
                try {
                    scrapperHttpClient.addLinkToTracking(chatId, link.getUri().toString(), link.getAlias());
                    results.add(Map.entry(link.toString(), Optional.empty()));
                } catch (DTOException e) {
                    results.add(Map.entry(link.toString(), Optional.of(e.getMessage())));
                }
            }
        });
        bot.sendMessage(chatId, createResult(results), SM_MARKDOWN, SM_DISABLE_PREVIEW);
        return CommandFunction.END;
    }

    /*package-private*/
    static CommandFunction track(TelegramBotComponent bot, Update update) {
        long chatId = update.message().chat().id();
        if (!isRegistered(bot, chatId)) {
            return CommandFunction.END;
        }
        ScrapperHttpClient scrapperHttpClient = bot.getScrapperHttpClient();
        scrapperHttpClient.getChat(chatId);
        bot.sendMessage(chatId, DESCRIPTION_MESSAGE);
        return Track::parseLinks;
    }

    public Track() {
        super("track", "start tracking link(s)", Track::track);
    }
}
