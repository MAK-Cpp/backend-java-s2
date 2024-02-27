package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.TelegramBotComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import java.util.stream.LongStream;
import static org.assertj.core.api.Assertions.assertThat;

public class HelpTest {
    private static final Help help = new Help();
    private static final String USAGE_TEXT = "TEST USAGE TEXT";
    private static final TelegramBotComponent bot = Mockito.mock(TelegramBotComponent.class);
    private static final Update update = Mockito.mock(Update.class);
    private static final Message message = Mockito.mock(Message.class);
    private static final Chat chat = Mockito.mock(Chat.class);
    private static final ArgumentCaptor<String> sendMessageArgumentCaptor = ArgumentCaptor.forClass(String.class);

    @BeforeAll
    public static void beforeAll() {
        Mockito.when(bot.getUsage()).thenReturn(USAGE_TEXT);
        Mockito.when(update.message()).thenReturn(message);
        Mockito.when(message.chat()).thenReturn(chat);
    }

    public static LongStream testHelp() {
        return LongStream.of(1L, 2L, 3L, 4L, 5L);
    }

    @ParameterizedTest
    @MethodSource
    void testHelp(long chatId) {
        Mockito.when(chat.id()).thenReturn(chatId);
        assertThat(help.getFunction().apply(bot, update)).isEqualTo(CommandFunction.END);
        Mockito.verify(bot, Mockito.atLeastOnce())
            .sendMessage(Mockito.any(Long.class), sendMessageArgumentCaptor.capture());

        assertThat(sendMessageArgumentCaptor.getValue()).isEqualTo(USAGE_TEXT);
    }
}
