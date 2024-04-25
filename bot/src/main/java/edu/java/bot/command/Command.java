package edu.java.bot.command;

import edu.java.bot.TelegramBotComponent;
import edu.java.dto.exception.APIException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ListUserLinkResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class Command {
    public static final String UNREGISTERED_USER_ERROR = "You must register before use bot!";
    public static final String NO_TRACKING_LINKS_ERROR = "There is no tracking links!";

    @NotEmpty
    private final String name;
    @NotEmpty
    private final String description;
    @NotNull
    private final CommandFunction function;

    protected static boolean isRegistered(final TelegramBotComponent bot, long chatId) {
        try {
            return bot.getScrapperHttpClient().getChat(chatId).getId() == chatId;
        } catch (APIException e) {
            bot.sendMessage(chatId, UNREGISTERED_USER_ERROR);
            return false;
        }
    }

    protected static boolean containsLinks(final TelegramBotComponent bot, long chatId) {
        try {
            final ListUserLinkResponse userLinks = bot.getScrapperHttpClient().getAllLinks(chatId);
            if (userLinks.getSize() == 0) {
                bot.sendMessage(chatId, NO_TRACKING_LINKS_ERROR);
            }
            return userLinks.getSize() > 0;
        } catch (APIException e) {
            if (e.getCause() instanceof WrongParametersException) {
                bot.sendMessage(chatId, NO_TRACKING_LINKS_ERROR);
            } else {
                bot.sendMessage(chatId, UNREGISTERED_USER_ERROR);
            }
            return false;
        }
    }

    public Command(
        String name,
        String description,
        @NotNull CommandFunction function
    ) {
        this.name = name;
        this.description = description;
        this.function = function;
    }
}
