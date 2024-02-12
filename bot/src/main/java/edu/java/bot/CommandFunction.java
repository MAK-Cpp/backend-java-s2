package edu.java.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.request.SendMessage;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@FunctionalInterface
public interface CommandFunction {
    // END - when function does not have any more steps
    CommandFunction END = new CommandFunction() {
        @Override
        public @NotNull CommandFunction apply(TelegramBot bot, HashMap<Long, User> users, Update update) {
            return END;
        }
    };
    // START - register user
    CommandFunction START = (bot, users, update) -> {
        final Chat chat = update.message().chat();
        long chatId = chat.id();
        final String username = chat.username();
        final String text;
        if (users.containsKey(chatId)) {
            text = "User " + username + " already registered!";
        } else {
            users.put(chatId, new User());
            text = "User " + username + " was registered!";
        }
        bot.execute(new SendMessage(chatId, text));
        return END;
    };
    // HELP - get list of commands
    CommandFunction HELP = (bot, users, update) -> {
        final Chat chat = update.message().chat();
        final StringBuilder help = new StringBuilder("Usage:\n");
        for (BotCommand command : bot.execute(new GetMyCommands()).commands()) {
            help.append('/').append(command.command()).append(" - ").append(command.description()).append('\n');
        }
        long chatId = chat.id();
        bot.execute(new SendMessage(chatId, help.toString()));
        return END;
    };

    // TRACK - start tracking link
    // 2) get link(s) with alias(es)
    private static Map.Entry<String, String> parseLink(final String line) {
        final String[] parsed = line.split("\\s+-\\s+", 2);
        return Map.entry(parsed[0], parsed[1]);
    }

    CommandFunction TRACK_PARSE_LINKS = (bot, users, update) -> {
        long chatId = update.message().chat().id();
        final StringBuilder message = new StringBuilder("Links:\n");
        update.message().text().lines().forEach(line -> {
            Map.Entry<String, String> parsedLink = parseLink(line);
            users.get(chatId).getLinks().put(parsedLink.getKey(), parsedLink.getValue());
            message.append("[").append(parsedLink.getKey()).append("](").append(parsedLink.getValue()).append(")\n");
        });
        message.append("were added for tracking");
        bot.execute(new SendMessage(chatId, message.toString()).parseMode(ParseMode.Markdown).disableWebPagePreview(true));
        return END;
    };
    // 1) call /track
    CommandFunction TRACK = (bot, users, update) -> {
        long chatId = update.message().chat().id();
        if (isNotRegistered(bot, users, chatId)) {
            return END;
        }
        bot.execute(new SendMessage(
            chatId,
            "Send link(s) for tracking\nFormat:\nlink_alias1 - link1\nlink_alias2 - link2\n..."
        ));
        return TRACK_PARSE_LINKS;
    };

    static boolean isNotRegistered(final TelegramBot bot, final HashMap<Long, User> users, long chatId) {
        if (!users.containsKey(chatId)) {
            bot.execute(new SendMessage(chatId, "You must register before use bot!"));
            return true;
        }
        return false;
    }

    private static boolean isNoLinks(final TelegramBot bot, final HashMap<Long, User> users, long chatId) {
        if (users.get(chatId).getLinks().isEmpty()) {
            bot.execute(new SendMessage(chatId, "There is no tracking links!"));
            return true;
        }
        return false;
    }

    private static InlineKeyboardMarkup getButtonsOfLinks(final HashMap<Long, User> users, long chatId) {
        final HashMap<String, String> links = users.get(chatId).getLinks();
        InlineKeyboardButton[] buttons = new InlineKeyboardButton[2];
        final InlineKeyboardMarkup result = new InlineKeyboardMarkup();
        int i = 0;
        for (String key : links.keySet()) {
            System.out.println(i + " " + (i & 1));
            buttons[i & 1] = new InlineKeyboardButton(key).callbackData(key);
            if ((i & 1) == 1) {
                result.addRow(buttons);
                buttons = new InlineKeyboardButton[2];
            }
//            buttons[i] = new InlineKeyboardButton(key).callbackData(key);
            i++;
        }
        if ((i & 1) == 1) {
            result.addRow(buttons[0]);
        }
        return result/*.addRow(buttons)*/.addRow(new InlineKeyboardButton("Cancel").callbackData("Cancel"));
    }

    // UNTRACK - stop tracking link
    // 3) confirm untrack
    static CommandFunction UNTRACK_CONFIRM_DELETE(int messageId, final String linkAlias) {
        return (bot, users, update) -> {
            final String chose = update.callbackQuery().data();
            final long chatId = update.callbackQuery().from().id();
            if (Objects.equals(chose, "Yes")) {
                final String link = users.get(chatId).getLinks().remove(linkAlias);
                final EditMessageText request =
                    new EditMessageText(chatId, messageId, "Link [" + linkAlias + "](" + link + ") was untracked");
                bot.execute(request.parseMode(ParseMode.Markdown).disableWebPagePreview(true));
                return END;
            }
            bot.execute(new EditMessageText(chatId, messageId, "choose ling to untrack")
                .replyMarkup(getButtonsOfLinks(users, chatId))
                .disableWebPagePreview(true)
            );
            return UNTRACK_CHOOSE_LINK(messageId);
        };
    }

    // 2) stop tracking link
    static CommandFunction UNTRACK_CHOOSE_LINK(int messageId) {
        return (bot, users, update) -> {
            final String linkAlias = update.callbackQuery().data();
            final long chatId = update.callbackQuery().from().id();
            if (Objects.equals(linkAlias, "Cancel")) {
                bot.execute(new EditMessageText(chatId, messageId, "Cancelled untrack command"));
                return END;
            }
            final String link = users.get(chatId).getLinks().get(linkAlias);
            final InlineKeyboardMarkup yesNo =
                new InlineKeyboardMarkup(
                    new InlineKeyboardButton("Yes").callbackData("Yes"),
                    new InlineKeyboardButton("No").callbackData("No")
                );
            final EditMessageText request = new EditMessageText(chatId, messageId,
                "Are you sure you want to untrack link [" + linkAlias + "](" + link + ")?"
            );
            bot.execute(request.parseMode(ParseMode.Markdown).disableWebPagePreview(true).replyMarkup(yesNo));
            return UNTRACK_CONFIRM_DELETE(messageId, linkAlias);
        };
    }

    // 1) call /untrack
    CommandFunction UNTRACK = (bot, users, update) -> {
        final long chatId = update.message().chat().id();
        if (isNotRegistered(bot, users, chatId) || isNoLinks(bot, users, chatId)) {
            return END;
        }
        final SendMessage message =
            new SendMessage(chatId, "choose link to untrack")
                .replyMarkup(getButtonsOfLinks(users, chatId))
                .disableWebPagePreview(true);
        return UNTRACK_CHOOSE_LINK(bot.execute(message).message().messageId());
    };
    // LIST - show list of tracked links
    CommandFunction LIST = (bot, users, update) -> {
        final long chatId = update.message().chat().id();
        if (!(isNotRegistered(bot, users, chatId) || isNoLinks(bot, users, chatId))) {
            final HashMap<String, String> links = users.get(chatId).getLinks();
            final StringBuilder text = new StringBuilder("Tracked links:\n");
            int i = 1;
            for (String linkAlias : links.keySet()) {
                text.append(i++).append(") [").append(linkAlias).append("](").append(links.get(linkAlias))
                    .append(")\n");
            }
            bot.execute(new SendMessage(chatId, text.toString()).parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true));
        }
        return END;
    };

    @NotNull
    CommandFunction apply(final TelegramBot bot, final HashMap<Long, User> users, final Update update);
}
