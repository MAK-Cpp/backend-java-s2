package edu.java.bot.command;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.User;
import java.util.Optional;
import org.springframework.stereotype.Component;
import static edu.java.bot.request.chains.SendMessageChains.SM_DISABLE_PREVIEW;
import static edu.java.bot.request.chains.SendMessageChains.SM_MARKDOWN;

@Component
public class List extends Command {
    public static String createLinksList(final Link... links) {
        final StringBuilder text = new StringBuilder("Tracked links:\n");
        int i = 1;
        for (Link link : links) {
            text.append(i++).append(") ").append(link.toString()).append('\n');
        }
        return text.toString();
    }

    private static CommandFunction list(TelegramBotComponent bot, Update update) {
        final long chatId = update.message().chat().id();
        if (isRegistered(bot, chatId) && containsLinks(bot, chatId)) {
            Optional<User> optUser = bot.getUser(chatId);
            if (optUser.isEmpty()) {
                return CommandFunction.END;
            }
            final Link[] links = optUser.get().allLinks().toArray(Link[]::new);
            bot.sendMessage(chatId, createLinksList(links), SM_MARKDOWN, SM_DISABLE_PREVIEW);
        }
        return CommandFunction.END;
    }

    public List() {
        super("list", "show list of tracked links", List::list);
    }
}
