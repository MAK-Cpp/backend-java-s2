package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import static edu.java.bot.requests.chains.SendMessageChains.SM_DISABLE_PREVIEW;
import static edu.java.bot.requests.chains.SendMessageChains.SM_MARKDOWN;

@Component
public class Track extends Command {
    public static final String DESCRIPTION_MESSAGE =
        "Send link(s) for tracking\nFormat:\nlink_alias1 - link1\nlink_alias2 - link2\n...";

    public static String createResult(final List<Map.Entry<String, Boolean>> results) {
        final StringBuilder message = new StringBuilder("Result:\n");
        int succeed = 0;
        for (Map.Entry<String, Boolean> result : results) {
            final String line = result.getKey();
            final Boolean isSucceed = result.getValue();
            if (isSucceed) {
                succeed++;
                message.append(line).append(" now tracking");
            } else {
                message.append("Cannot parse `").append(line).append("`");
            }
            message.append('\n');
        }
        message.append("\nSUCCEED: ").append(succeed).append("\nFAILED: ").append(results.size() - succeed);
        return message.toString();
    }

    /*package-private*/
    static CommandFunction parseLinks(TelegramBotComponent bot, Update update) {
        final long chatId = update.message().chat().id();
        Optional<User> optUser = bot.getUser(chatId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            List<Map.Entry<String, Boolean>> results = new ArrayList<>();
            update.message().text().lines().forEach(line -> {
                Optional<Link> optionalLink = Link.parse(line);
                if (optionalLink.isEmpty()) {
                    results.add(Map.entry(line, false));
                } else {
                    Link link = optionalLink.get();
                    user.addLink(link);
                    results.add(Map.entry(link.toString(), true));
                }
            });
            bot.sendMessage(chatId, createResult(results), SM_MARKDOWN, SM_DISABLE_PREVIEW);
        }
        return CommandFunction.END;
    }

    /*package-private*/
    static CommandFunction track(TelegramBotComponent bot, Update update) {
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
