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
import edu.java.bot.client.ScrapperHttpClient;
import edu.java.bot.command.Command;
import edu.java.bot.command.CommandFunction;
import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.request.chains.Chains;
import edu.java.bot.request.chains.EditMessageTextChains;
import edu.java.bot.request.chains.SendMessageChains;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class TelegramBotComponent extends TelegramBot {
    private final ConcurrentMap<String, CommandFunction> commandFunctions = new ConcurrentHashMap<>();
    // private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, CommandFunction> waitingFunctions = new ConcurrentHashMap<>();
    @Getter private final String usage;
    @Getter private final ScrapperHttpClient scrapperHttpClient;

    public static <T> Optional<T> maybe(final T value) {
        return Optional.ofNullable(value);
    }

    @Autowired
    public TelegramBotComponent(
        ApplicationConfig config,
        List<Command> commands,
        ScrapperHttpClient scrapperHttpClient
    ) {
        super(config.telegramToken());
        this.scrapperHttpClient = scrapperHttpClient;
        setUpdatesListener(this::updateListener, this::exceptionHandler);
        usage = setCommands(commands);
        log.debug("Created bot with token {}", getToken());
    }

/*    public void addUser(long id) {
        log.debug("Adding user {}", id);
        scrapperHttpClient.registerChat(id);
    }

    public Optional<User> deleteUser(long id) {
        log.debug("Deleting user {}", id);
        return maybe(users.remove(id));
    }*/

    // public void deleteAllUsers() {
    //     users.clear();
    // }

    // public boolean containUser(long id) {
    //     return users.containsKey(id);
    // }

    // public Optional<User> getUser(long id) {
    //     return maybe(users.get(id));
    // }

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
        final Long chatId = update.callbackQuery().from().id();
        final CommandFunction commandFunction = waitingFunctions.get(chatId);
        final CommandFunction nextFunction = commandFunction.apply(this, update);
        waitingFunctions.put(chatId, nextFunction);
    }

    private void messageParse(Update update) {
        final Message message = update.message();
        final String command = message.text();
        final long chatId = message.chat().id();
        final Optional<CommandFunction> optionalFunction = maybe(commandFunctions.get(command));
        if (optionalFunction.isPresent()) {
            final CommandFunction commandFunction = optionalFunction.get();
            final CommandFunction nextFunction = commandFunction.apply(this, update);
            waitingFunctions.put(chatId, nextFunction);
        } else if (waitingFunctions.containsKey(chatId) && waitingFunctions.get(chatId) != CommandFunction.END) {
            final CommandFunction commandFunction = waitingFunctions.get(chatId);
            if (commandFunction != CommandFunction.END) {
                final CommandFunction nextFunction = commandFunction.apply(this, update);
                waitingFunctions.put(chatId, nextFunction);
            } else {
                unknownCommand(update);
            }
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
            log.debug("added command {} to bot", command.getName());
        }
        execute(new SetMyCommands(botCommands));
        return result.toString();
    }
}
