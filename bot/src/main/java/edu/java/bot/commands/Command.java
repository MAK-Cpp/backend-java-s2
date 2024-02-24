package edu.java.bot.commands;

import edu.java.bot.TelegramBotComponent;
import edu.java.bot.User;
import jakarta.validation.constraints.NotEmpty;
import java.util.Optional;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class Command {
    public static final String UNREGISTERED_USER_ERROR = "You must register before use bot!";
    public static final String NO_TRACKING_LINKS_ERROR = "There is no tracking links!";
    @NotEmpty private final String name;
    @NotEmpty private final String description;
    @NotNull private final CommandFunction function;

    protected static boolean isRegistered(final TelegramBotComponent bot, long chatId) {
        if (!bot.containUser(chatId)) {
            bot.sendMessage(chatId, UNREGISTERED_USER_ERROR);
            return false;
        }
        return true;
    }

    protected static boolean containsLinks(final TelegramBotComponent bot, long chatId) {
        Optional<User> optUser = bot.getUser(chatId);
        if (optUser.isEmpty()) {
            return false;
        }
        User user = optUser.get();
        if (user.hasNoLinks()) {
            bot.sendMessage(chatId, NO_TRACKING_LINKS_ERROR);
            return false;
        }
        return true;
    }

    public Command(String name, String description, @NotNull CommandFunction function) {
        this.name = name;
        this.description = description;
        this.function = function;
    }
}
