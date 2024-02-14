package edu.java.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.requests.chains.Chains;
import edu.java.bot.requests.chains.EditMessageTextChains;
import edu.java.bot.requests.chains.SendMessageChains;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public final class TelegramBotComponent extends TelegramBot {
    private final Map<String, CommandFunction> commandFunctions = new HashMap<>();
    private final Map<Long, User> users = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotComponent.class);
    /*package-private*/ static final String NO_BOT_COMMANDS_ERROR = "There is no commands in bot!";
    /*package-private*/ static final String UNREGISTERED_USER_ERROR = "You must register before use bot!";
    /*package-private*/ static final String NO_TRACKING_LINKS_ERROR = "There is no tracking links!";
    /*package-private*/ static final String TRACK_DESCRIPTION =
        "Send link(s) for tracking\nFormat:\nlink_alias1 - link1\nlink_alias2 - link2\n...";
    /*package-private*/ static final String LINK_MARKDOWN_FORMAT = "[%s](%s)";
    /*package-private*/ static final String LINK_PARSE_REGEX = "^(.+)\\s+-\\s+(.+)$";
    /*package-private*/ static final String UNTRACK_DESCRIPTION = "choose link to untrack";

    @Autowired
    private TelegramBotComponent(ApplicationConfig config) {
        super(config.telegramToken());
        setUpdatesListener(this::updateListener);
        setCommands(Command.START, Command.HELP, Command.TRACK, Command.UNTRACK, Command.LIST);
        LOGGER.debug("Created bot with token " + config.telegramToken());
    }

    /*package-private*/ void addUser(long id, User user) {
        users.put(id, user);
    }

    /*package-private*/ void deleteUser(long id) {
        users.remove(id);
    }

    /*package-private*/ void deleteAllUsers() {
        users.clear();
    }

    /*package-private*/ boolean containUser(long id) {
        return users.containsKey(id);
    }

    /*package-private*/ User getUser(long id) {
        return users.get(id);
    }

    public SendResponse sendMessage(long chatId, final String text) {
        return execute(new SendMessage(chatId, text));
    }

    public SendResponse sendMessage(long chatId, final String text, final SendMessageChains... operations) {
        return execute(Chains.allOf(operations).apply(new SendMessage(chatId, text)));
    }

    public BaseResponse editMessageText(long chatId, int messageId, final String text) {
        return execute(new EditMessageText(chatId, messageId, text));
    }

    public BaseResponse editMessageText(
        long chatId,
        int messageId,
        final String text,
        final EditMessageTextChains... operations
    ) {
        return execute(Chains.allOf(operations).apply(new EditMessageText(chatId, messageId, text)));
    }

    private void callbackParse(Update update) {
        final long chatId = update.callbackQuery().from().id();
        if (CommandFunction.isRegistered(this, chatId)) {
            final User user = users.get(chatId);
            user.setWaitingFunction(user.getWaitingFunction().apply(this, update));
        }
    }

    private void messageParse(Update update) {
        final Message message = update.message();
        final String command = message.text();
        final long chatId = message.chat().id();
        if (commandFunctions.containsKey(command)) {
            CommandFunction waitingFunction = commandFunctions.get(command).apply(this, update);
            if (users.containsKey(chatId)) {
                users.get(chatId).setWaitingFunction(waitingFunction);
            }
        } else if (users.containsKey(chatId) && users.get(chatId).getWaitingFunction() != CommandFunction.END) {
            final CommandFunction waitingFunction = users.get(chatId).getWaitingFunction();
            users.get(chatId).setWaitingFunction(waitingFunction.apply(this, update));
        } else {
            unknownCommand(update);
        }
    }

    /*package-private*/ int updateListener(List<Update> updatesList) {
        updatesList.parallelStream().forEach(update -> {
            if (update.message() == null) {
                callbackParse(update);
            } else {
                messageParse(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void unknownCommand(Update update) {
        final Message message = update.message();
        final long chatId = message.chat().id();
        final String command = message.text();
        execute(new SendMessage(chatId, "Unknown command " + command + ", type /help to get help."));
    }

    public void setCommands(final Command... commands) {
        execute(new SetMyCommands());
        addCommands(commands);
    }

    public void addCommands(final Command... newCommands) {
        ArrayList<BotCommand> commands = new java.util.ArrayList<>(List.of(execute(new GetMyCommands()).commands()));
        for (Command command : newCommands) {
            commands.add(new BotCommand(command.name(), command.description()));
            commandFunctions.put("/" + command.name(), command.function());
        }
        execute(new SetMyCommands(commands.toArray(BotCommand[]::new)));
    }
}
