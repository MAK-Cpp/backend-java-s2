package edu.java.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.commands.Command;
import edu.java.bot.commands.CommandFunction;
import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.requests.chains.Chains;
import edu.java.bot.requests.chains.EditMessageTextChains;
import edu.java.bot.requests.chains.SendMessageChains;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class TelegramBotComponent extends TelegramBot {
    private final Map<String, CommandFunction> commandFunctions = new HashMap<>();
    private final Map<Long, User> users = new HashMap<>();
    @Getter private final String usage;
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotComponent.class);

    @Autowired
    public TelegramBotComponent(ApplicationConfig config, List<Command> commands) {
        super(config.telegramToken());
        setUpdatesListener(this::updateListener, this::exceptionHandler);
        usage = setCommands(commands);
        LOGGER.debug("Created bot with token " + this.getToken());
    }

    public void addUser(long id, User user) {
        users.put(id, user);
    }

    public void deleteUser(long id) {
        users.remove(id);
    }

    public void deleteAllUsers() {
        users.clear();
    }

    public boolean containUser(long id) {
        return users.containsKey(id);
    }

    public User getUser(long id) {
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
        final User user = users.get(chatId);
        user.setWaitingFunction(user.getWaitingFunction().apply(this, update));
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
            if (update.callbackQuery() != null) {
                callbackParse(update);
            } else if (update.message() != null) {
                messageParse(update);
            } else {
                throw new RuntimeException("unknown update: " + update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    void exceptionHandler(TelegramException e) {
        if (e.response() != null) {
            // got bad response from telegram
            e.response().errorCode();
            e.response().description();
        } else {
            // probably network error
            e.printStackTrace();
        }
    }

    private void unknownCommand(Update update) {
        final Message message = update.message();
        final long chatId = message.chat().id();
        final String command = message.text();
        execute(new SendMessage(chatId, "Unknown command " + command + ", type /help to get help."));
    }

    public String setCommands(List<Command> commands) {
        final StringBuilder result = new StringBuilder("Usage:\n");
        final BotCommand[] botCommands = new BotCommand[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            final Command command = commands.get(i);
            result.append('/').append(command.getName()).append(" - ").append(command.getDescription()).append('\n');
            commandFunctions.put("/" + command.getName(), command.getFunction());
            botCommands[i] = new BotCommand(command.getName(), command.getDescription());
            LOGGER.debug("added command " + command.getName() + " to bot");
        }
        return result.toString();
    }
}
