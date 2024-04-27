package edu.java.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.client.ScrapperHttpClient;
import edu.java.dto.exception.APIException;
import org.springframework.stereotype.Component;

@Component
public class Start extends Command {
    public static final String USER_REGISTER_FAILED_MESSAGE_FORMAT = "User %s already registered!";
    public static final String USER_REGISTER_SUCCESS_MESSAGE_FORMAT = "User %s was registered!";

    private static CommandFunction start(TelegramBotComponent bot, Update update) {
        final ScrapperHttpClient scrapperHttpClient = bot.getScrapperHttpClient();
        final Chat chat = update.message().chat();
        long chatId = chat.id();
        final String username = chat.username();
        String resultFormat;
        try {
            scrapperHttpClient.registerChat(chatId);
            resultFormat = USER_REGISTER_SUCCESS_MESSAGE_FORMAT;
        } catch (APIException e) {
            resultFormat = USER_REGISTER_FAILED_MESSAGE_FORMAT;
        }
        bot.sendMessage(chatId, String.format(resultFormat, username));
        return CommandFunction.END;
    }

    public Start() {
        super("start", "register a user", Start::start);
    }
}
