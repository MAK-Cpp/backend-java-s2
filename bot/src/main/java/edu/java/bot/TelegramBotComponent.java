package edu.java.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.commands.Command;
import edu.java.bot.commands.CommandFunction;
import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.requests.chains.Chains;
import edu.java.bot.requests.chains.EditMessageTextChains;
import edu.java.bot.requests.chains.SendMessageChains;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class TelegramBotComponent extends TelegramBot {
    private final ConcurrentMap<String, CommandFunction> commandFunctions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    @Getter private final String usage;
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotComponent.class);

    public static <T> Optional<T> maybe(final T value) {
        return Optional.ofNullable(value);
    }

    @Autowired
    public TelegramBotComponent(ApplicationConfig config, List<Command> commands) {
        super(config.telegramToken());
        setUpdatesListener(this::updateListener, this::exceptionHandler);
        usage = setCommands(commands);
        LOGGER.debug("Created bot with token " + this.getToken());
    }

    public Optional<User> addUser(long id, User user) {
        return maybe(users.put(id, user));
    }

    public Optional<User> deleteUser(long id) {
        return maybe(users.remove(id));
    }

    public void deleteAllUsers() {
        users.clear();
    }

    public boolean containUser(long id) {
        return users.containsKey(id);
    }

    public Optional<User> getUser(long id) {
        return maybe(users.get(id));
    }

    public SendResponse sendMessage(long chatId, final String text, final SendMessageChains... operations) {
        SendMessage toExecute = new SendMessage(chatId, text);
        if (operations.length > 0) {
            toExecute = Chains.allOf(operations).apply(toExecute);
        }
        return execute(toExecute);
    }

    public BaseResponse editMessageText(
        long chatId,
        int messageId,
        final String text,
        final EditMessageTextChains... operations
    ) {
        EditMessageText toExecute = new EditMessageText(chatId, messageId, text);
        if (operations.length > 0) {
            toExecute = Chains.allOf(operations).apply(toExecute);
        }
        return execute(toExecute);
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
        final Optional<User> optionalUser = maybe(users.get(chatId));
        final Optional<CommandFunction> optionalFunction = maybe(commandFunctions.get(command));
        if (optionalFunction.isPresent()) {
            final CommandFunction waitingFunction = optionalFunction.get().apply(this, update);
            optionalUser.ifPresent(user -> user.setWaitingFunction(waitingFunction));
        } else if (optionalUser.isPresent() && optionalUser.get().getWaitingFunction() != CommandFunction.END) {
            final User user = optionalUser.get();
            final CommandFunction waitingFunction = user.getWaitingFunction();
            user.setWaitingFunction(waitingFunction.apply(this, update));
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
        execute(new SetMyCommands(botCommands));
        return result.toString();
    }
}
