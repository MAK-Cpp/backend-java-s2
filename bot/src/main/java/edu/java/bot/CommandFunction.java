package edu.java.bot;

import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.requests.chains.Chains;
import edu.java.bot.requests.chains.SendMessageChains;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static edu.java.bot.requests.chains.EditMessageTextChains.*;
import static edu.java.bot.requests.chains.SendMessageChains.*;

@FunctionalInterface
public interface CommandFunction {
    static boolean isRegistered(final TelegramBotComponent bot, long chatId) {
        if (!bot.containUser(chatId)) {
            bot.sendMessage(chatId, TelegramBotComponent.UNREGISTERED_USER_ERROR);
            return false;
        }
        return true;
    }

    private static boolean isNoLinks(final TelegramBotComponent bot, long chatId) {
        if (bot.getUser(chatId).hasNoLinks()) {
            bot.sendMessage(chatId, TelegramBotComponent.NO_TRACKING_LINKS_ERROR);
            return true;
        }
        return false;
    }

    static String createHelp(final TelegramBotComponent bot) {
        final BotCommand[] commands = bot.execute(new GetMyCommands()).commands();
        if (commands.length == 0) {
            return TelegramBotComponent.NO_BOT_COMMANDS_ERROR;
        }
        final StringBuilder help = new StringBuilder("Usage:\n");
        for (BotCommand command : commands) {
            help.append('/').append(command.command()).append(" - ").append(command.description()).append('\n');
        }
        return help.toString();
    }

    static String createParseResult(final String link, boolean isSuccess) {
        return (isSuccess ? link + " now tracking" : "Cannot parse `" + link + "`");
    }

    static String createLinksList(final Link... links) {
        final StringBuilder text = new StringBuilder("Tracked links:\n");
        int i = 1;
        for (Link link : links) {
            text.append(i++).append(") ").append(link.toString()).append('\n');
        }
        return text.toString();
    }

    private static InlineKeyboardMarkup getLinksButtons(final Set<String> aliasSet) {
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
        return result.addRow(new InlineKeyboardButton("Cancel").callbackData("Cancel"));
    }

    // END - when function does not have any more steps
    CommandFunction END = new CommandFunction() {
        @Override
        public @NotNull CommandFunction apply(TelegramBotComponent bot, Update update) {
            return END;
        }
    };
    // START - register user
    CommandFunction START = (bot, update) -> {
        final Chat chat = update.message().chat();
        long chatId = chat.id();
        final String username = chat.username();
        final String text;
        if (bot.containUser(chatId)) {
            text = "User " + username + " already registered!";
        } else {
            bot.addUser(chatId, new User());
            text = "User " + username + " was registered!";
        }
        bot.sendMessage(chatId, text);
        return END;
    };

    // HELP - get list of commands
    CommandFunction HELP = (bot, update) -> {
        final Chat chat = update.message().chat();
        long chatId = chat.id();
        bot.sendMessage(chatId, createHelp(bot));
        return END;
    };

    // TRACK - start tracking link
    // 2) get link(s) with alias(es)
    CommandFunction TRACK_PARSE_LINKS = (bot, update) -> {
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
        return END;
    };
    // 1) call /track
    CommandFunction TRACK = (bot, update) -> {
        long chatId = update.message().chat().id();
        if (!isRegistered(bot, chatId)) {
            return END;
        }
        bot.sendMessage(chatId, TelegramBotComponent.TRACK_DESCRIPTION);
        return TRACK_PARSE_LINKS;
    };

    // UNTRACK - stop tracking link
    // 3) confirm untrack
    static CommandFunction UNTRACK_CONFIRM_DELETE(int messageId, final String linkAlias) {
        return (bot, update) -> {
            final String chose = update.callbackQuery().data();
            final long chatId = update.callbackQuery().from().id();
            final User user = bot.getUser(chatId);
            if (Objects.equals(chose, "Yes")) {
                bot.editMessageText(
                    chatId, messageId,
                    "Link " + user.removeLink(linkAlias) + " now untracked",
                    EMT_MARKDOWN, EMT_DISABLE_PREVIEW
                );
                return END;
            }
            final InlineKeyboardMarkup buttons = getLinksButtons(user.aliasSet());
            bot.editMessageText(
                chatId, messageId,
                "choose link to untrack",
                EMT_REPLY_MARKUP(buttons), EMT_DISABLE_PREVIEW
            );
            return UNTRACK_CHOOSE_LINK(messageId);
        };
    }

    // 2) stop tracking link
    static CommandFunction UNTRACK_CHOOSE_LINK(int messageId) {
        return (bot, update) -> {
            final String alias = update.callbackQuery().data();
            final long chatId = update.callbackQuery().from().id();
            if (Objects.equals(alias, "Cancel")) {
                bot.editMessageText(chatId, messageId, "Cancelled untrack command");
                return END;
            }
            final Link link = bot.getUser(chatId).getLink(alias);
            final InlineKeyboardMarkup yesNo =
                new InlineKeyboardMarkup(
                    new InlineKeyboardButton("Yes").callbackData("Yes"),
                    new InlineKeyboardButton("No").callbackData("No")
                );
            bot.editMessageText(
                chatId, messageId,
                "Are you sure you want to untrack link " + link + "?",
                EMT_MARKDOWN, EMT_DISABLE_PREVIEW, EMT_REPLY_MARKUP(yesNo)
            );
            return UNTRACK_CONFIRM_DELETE(messageId, alias);
        };
    }

    // 1) call /untrack
    CommandFunction UNTRACK = (bot, update) -> {
        final long chatId = update.message().chat().id();
        if (!isRegistered(bot, chatId) || isNoLinks(bot, chatId)) {
            return END;
        }
        final InlineKeyboardMarkup buttons = getLinksButtons(bot.getUser(chatId).aliasSet());
        SendMessageChains chains = Chains.allOf(SM_REPLY_MARKUP(buttons), SM_DISABLE_PREVIEW);
        SendResponse response = bot.sendMessage(chatId, TelegramBotComponent.UNTRACK_DESCRIPTION, chains);
        return UNTRACK_CHOOSE_LINK(response.message().messageId());
    };
    // LIST - show list of tracked links
    CommandFunction LIST = (bot, update) -> {
        final long chatId = update.message().chat().id();
        if (!(!isRegistered(bot, chatId) || isNoLinks(bot, chatId))) {
            final Link[] links = bot.getUser(chatId).allLinks().toArray(Link[]::new);
            bot.sendMessage(chatId, createLinksList(links), SM_MARKDOWN, SM_DISABLE_PREVIEW);
        }
        return END;
    };

    @NotNull
    CommandFunction apply(final TelegramBotComponent bot, final Update update);
}
