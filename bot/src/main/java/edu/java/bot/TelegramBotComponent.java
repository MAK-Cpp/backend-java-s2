package edu.java.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import edu.java.bot.configuration.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class TelegramBotComponent extends TelegramBot {
    private final HashMap<String, CommandFunction> commandFunctions = new HashMap<>();
    private final HashMap<Long, User> users = new HashMap<>();

    @Autowired
    public TelegramBotComponent(ApplicationConfig config) {
        super(config.telegramToken());
        setUpdatesListener(updatesList -> {
            updatesList.parallelStream().forEach(update -> {
                final Message message = update.message();
                if (message == null) {
                    final long chatId = update.callbackQuery().from().id();
                    if (!users.containsKey(chatId)) {
                        execute(new SendMessage(chatId, "You must register before use bot!"));
                        return;
                    }
                    CommandFunction waitingFunction = users.get(chatId).getWaitingFunction();
                    users.get(chatId).setWaitingFunction(waitingFunction.apply(this, users, update));
                    return;
                }
                final String command = update.message().text();
                final long chatId = update.message().chat().id();
                if (commandFunctions.containsKey(command)) {
                    CommandFunction waitingFunction = commandFunctions.get(command).apply(this, users, update);
                    if (users.containsKey(chatId)) {
                        users.get(chatId).setWaitingFunction(waitingFunction);
                    }
                } else if (users.containsKey(chatId) && users.get(chatId).getWaitingFunction() != CommandFunction.END) {
                    final CommandFunction waitingFunction = users.get(chatId).getWaitingFunction();
                    users.get(chatId).setWaitingFunction(waitingFunction.apply(this, users, update));
                } else {
                    unknownCommand(update);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        setCommands(Command.START, Command.HELP, Command.TRACK, Command.UNTRACK, Command.LIST);
    }

    void unknownCommand(Update update) {
        final Message message = update.message();
        final long chatId = message.chat().id();
        final String command = message.text();
        execute(new SendMessage(chatId, "Unknown command " + command + ", type /help to get help."));
    }

    void setCommands(final Command... commands) {
        execute(new SetMyCommands());
        addCommands(commands);
    }

    void addCommands(final Command... newCommands) {
        ArrayList<BotCommand> commands = new java.util.ArrayList<>(List.of(execute(new GetMyCommands()).commands()));
        for (Command command : newCommands) {
            commands.add(new BotCommand(command.name(), command.description()));
            commandFunctions.put("/" + command.name(), command.function());
        }
        execute(new SetMyCommands(commands.toArray(BotCommand[]::new)));
    }
}
