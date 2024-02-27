package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.TelegramBotComponent;
import org.springframework.stereotype.Component;

@Component
public class Help extends Command {
    private static CommandFunction help(TelegramBotComponent bot, Update update) {
        final Chat chat = update.message().chat();
        long chatId = chat.id();
        bot.sendMessage(chatId, bot.getUsage());
        return CommandFunction.END;
    }

    public Help() {
        super("help", "display commands list", Help::help);
    }
}
