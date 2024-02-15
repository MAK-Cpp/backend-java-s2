package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.CommandFunction;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.User;
import org.springframework.stereotype.Component;

@Component
public class Start extends Command {
    public static final String USER_REGISTER_FAILED_MESSAGE_FORMAT = "User %s already registered!";
    public static final String USER_REGISTER_SUCCESS_MESSAGE_FORMAT = "User %s was registered!";

    private static CommandFunction start(TelegramBotComponent bot, Update update) {
        final Chat chat = update.message().chat();
        long chatId = chat.id();
        final String username = chat.username();
        final String resultFormat;
        if (bot.containUser(chatId)) {
            resultFormat = USER_REGISTER_FAILED_MESSAGE_FORMAT;
        } else {
            bot.addUser(chatId, new User());
            resultFormat = USER_REGISTER_SUCCESS_MESSAGE_FORMAT;
        }
        bot.sendMessage(chatId, String.format(resultFormat, username));
        return CommandFunction.END;
    }

    public Start() {
        super("start", "register a user", Start::start);
    }
}
