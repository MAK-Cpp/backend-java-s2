package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.CommandFunction;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import static edu.java.bot.requests.chains.SendMessageChains.SM_DISABLE_PREVIEW;
import static edu.java.bot.requests.chains.SendMessageChains.SM_MARKDOWN;

@Component
public class Track extends Command {
    public static final String DESCRIPTION_MESSAGE =
        "Send link(s) for tracking\nFormat:\nlink_alias1 - link1\nlink_alias2 - link2\n...";

    public static String createParseResult(final String link, boolean isSuccess) {
        return (isSuccess ? link + " now tracking" : "Cannot parse `" + link + "`");
    }

    private static CommandFunction parseLinks(TelegramBotComponent bot, Update update) {
        final long chatId = update.message().chat().id();
        final StringBuilder message = new StringBuilder("Result:\n");
        final AtomicInteger succeed = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        update.message().text().lines().forEach(line -> {
            Optional<Link> optionalLink = Link.parse(line);
            final String result;
            if (optionalLink.isEmpty()) {
                result = line;
                failed.getAndIncrement();
            } else {
                Link link = optionalLink.get();
                bot.getUser(chatId).addLink(link);
                result = link.toString();
                succeed.getAndIncrement();
            }
            message.append(createParseResult(result, optionalLink.isPresent())).append('\n');
        });
        message.append("\nSUCCEED: ").append(succeed).append("\nFAILED: ").append(failed);
        bot.sendMessage(chatId, message.toString(), SM_MARKDOWN, SM_DISABLE_PREVIEW);
        return CommandFunction.END;
    }

    private static CommandFunction track(TelegramBotComponent bot, Update update) {
        long chatId = update.message().chat().id();
        if (!isRegistered(bot, chatId)) {
            return CommandFunction.END;
        }
        bot.sendMessage(chatId, DESCRIPTION_MESSAGE);
        return Track::parseLinks;
    }

    public Track() {
        super("track", "start tracking link(s)", Track::track);
    }
}
