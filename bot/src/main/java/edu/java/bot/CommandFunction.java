package edu.java.bot;

import com.pengrad.telegrambot.model.Update;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandFunction {
    // END - when function does not have any more steps
    CommandFunction END = new CommandFunction() {
        @Override
        public @NotNull CommandFunction apply(TelegramBotComponent bot, Update update) {
            return END;
        }
    };

    @NotNull
    CommandFunction apply(TelegramBotComponent bot, Update update);
}
