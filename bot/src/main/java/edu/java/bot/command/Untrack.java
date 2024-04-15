package edu.java.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.client.ScrapperHttpClient;
import edu.java.bot.request.chains.Chains;
import edu.java.bot.request.chains.SendMessageChains;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.UserLinkResponse;
import org.springframework.stereotype.Component;
import static edu.java.bot.request.chains.EditMessageTextChains.EMT_DISABLE_PREVIEW;
import static edu.java.bot.request.chains.EditMessageTextChains.EMT_MARKDOWN;
import static edu.java.bot.request.chains.EditMessageTextChains.EMT_REPLY_MARKUP;
import static edu.java.bot.request.chains.SendMessageChains.SM_DISABLE_PREVIEW;
import static edu.java.bot.request.chains.SendMessageChains.SM_REPLY_MARKUP;

@Component
public class Untrack extends Command {
    public static final String DESCRIPTION_MESSAGE = "choose link to untrack";
    public static final String ABORTED_MESSAGE = "Cancelled untrack command";
    public static final String CONFIRM_MESSAGE_FORMAT =
        "Are you sure you want to untrack link %s?";
    public static final String SUCCESS_MESSAGE_FORMAT = "Link %s now untracked";
    public static final String NO_LINK_ERROR_FORMAT = "There is no link %s in tracking!";
    public static final String YES_BUTTON_TEXT = "Yes";
    public static final String NO_BUTTON_TEXT = "No";
    public static final String CANCEL_BUTTON_TEXT = "Cancel";

    /*package-private*/ static InlineKeyboardMarkup getLinksButtons(final Set<String> aliasSet) {
        InlineKeyboardButton[] buttons = new InlineKeyboardButton[2];
        final InlineKeyboardMarkup result = new InlineKeyboardMarkup();
        int i = 0;
        for (String alias : aliasSet) {
            buttons[i & 1] = new InlineKeyboardButton(alias).callbackData(alias);
            if ((i & 1) == 1) {
                result.addRow(buttons);
                buttons = new InlineKeyboardButton[2];
            }
            i++;
        }
        if ((i & 1) == 1) {
            result.addRow(buttons[0]);
        }
        return result.addRow(new InlineKeyboardButton(CANCEL_BUTTON_TEXT).callbackData(CANCEL_BUTTON_TEXT));
    }

    /*package-private*/ static CommandFunction confirmDelete(int messageId, final String alias) {
        return (bot, update) -> {
            final Long chatId = update.callbackQuery().from().id();
            if (!isRegistered(bot, chatId)) {
                return CommandFunction.END;
            }
            final ScrapperHttpClient scrapperHttpClient = bot.getScrapperHttpClient();
            final String chose = update.callbackQuery().data();
            if (Objects.equals(chose, YES_BUTTON_TEXT)) {
                final UserLinkResponse userLinkResponse = scrapperHttpClient.getLinkByChatIdAndAlias(chatId, alias);
                final Link link = new Link(userLinkResponse.getAlias(), userLinkResponse.getLink().getUri().toString());
                scrapperHttpClient.removeLinkFromTracking(chatId, alias);
                bot.editMessageText(
                    chatId, messageId,
                    String.format(SUCCESS_MESSAGE_FORMAT, link),
                    EMT_MARKDOWN, EMT_DISABLE_PREVIEW
                );
                return CommandFunction.END;
            }
            final UserLinkResponse[] userLinks = scrapperHttpClient.getAllLinks(chatId).getLinks();
            final InlineKeyboardMarkup buttons = getLinksButtons(
                Arrays.stream(userLinks)
                    .map(UserLinkResponse::getAlias)
                    .collect(Collectors.toSet())
            );
            bot.editMessageText(
                chatId, messageId,
                DESCRIPTION_MESSAGE,
                EMT_REPLY_MARKUP(buttons), EMT_DISABLE_PREVIEW
            );
            return chooseLink(messageId);
        };
    }

    /*package-private*/ static CommandFunction chooseLink(int messageId) {
        return (bot, update) -> {
            final ScrapperHttpClient scrapperHttpClient = bot.getScrapperHttpClient();
            final String alias = update.callbackQuery().data();
            final long chatId = update.callbackQuery().from().id();
            if (Objects.equals(alias, CANCEL_BUTTON_TEXT)) {
                bot.editMessageText(chatId, messageId, ABORTED_MESSAGE);
                return CommandFunction.END;
            }
            try {
                final UserLinkResponse userLinkResponse = scrapperHttpClient.getLinkByChatIdAndAlias(chatId, alias);
                final Link link = new Link(userLinkResponse.getAlias(), userLinkResponse.getLink().getUri().toString());
                final InlineKeyboardMarkup yesNo =
                    new InlineKeyboardMarkup(
                        new InlineKeyboardButton(YES_BUTTON_TEXT).callbackData(YES_BUTTON_TEXT),
                        new InlineKeyboardButton(NO_BUTTON_TEXT).callbackData(NO_BUTTON_TEXT)
                    );
                bot.editMessageText(
                    chatId, messageId,
                    String.format(CONFIRM_MESSAGE_FORMAT, link),
                    EMT_MARKDOWN, EMT_DISABLE_PREVIEW, EMT_REPLY_MARKUP(yesNo)
                );
                return confirmDelete(messageId, alias);
            } catch (WrongParametersException e) {
                bot.editMessageText(
                    chatId, messageId,
                    String.format(NO_LINK_ERROR_FORMAT, alias)
                );
                return CommandFunction.END;
            }
        };
    }

    /*package-private*/ static CommandFunction untrack(TelegramBotComponent bot, Update update) {
        final long chatId = update.message().chat().id();
        if (!(isRegistered(bot, chatId) && containsLinks(bot, chatId))) {
            return CommandFunction.END;
        }
        final ScrapperHttpClient scrapperHttpClient = bot.getScrapperHttpClient();
        final UserLinkResponse[] userLinks = scrapperHttpClient.getAllLinks(chatId).getLinks();
        final InlineKeyboardMarkup buttons = getLinksButtons(
            Arrays.stream(userLinks)
                .map(UserLinkResponse::getAlias)
                .collect(Collectors.toSet())
        );
        final SendMessageChains chains = Chains.allOf(SM_REPLY_MARKUP(buttons), SM_DISABLE_PREVIEW);
        final SendResponse sendResponse = bot.sendMessage(chatId, DESCRIPTION_MESSAGE, chains);
        return chooseLink(sendResponse.message().messageId());
    }

    public Untrack() {
        super("untrack", "stop tracking link", Untrack::untrack);
    }
}
