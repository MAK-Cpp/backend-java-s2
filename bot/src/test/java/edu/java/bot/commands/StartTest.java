package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartTest {
    private static final Start start = new Start();
    private static final TelegramBotComponent bot = mock(TelegramBotComponent.class);
    private static final Update update = mock(Update.class);
    private static final Message message = mock(Message.class);
    private static final Chat chat = mock(Chat.class);
    private static final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    @BeforeAll
    public static void beforeAll() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
    }

    public static Stream<Arguments> testStart() {
        return Stream.of(
            Arguments.of(1, "maxim", false),
            Arguments.of(2, "dmitriy", false),
            Arguments.of(1, "maxim", true),
            Arguments.of(2, "dmitriy", true),
            Arguments.of(3, "maxim", false),
            Arguments.of(4, "dmitriy", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testStart(long chatId, String username, boolean contains) {
        when(bot.containUser(any(Long.class))).thenReturn(contains);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn(username);
        assertThat(start.getFunction().apply(bot, update)).isEqualTo(CommandFunction.END);
        String resultFormat;
        if (contains) {
            resultFormat = Start.USER_REGISTER_FAILED_MESSAGE_FORMAT;
        } else {
            verify(bot, atLeastOnce()).addUser(any(Long.class), any(User.class));
            resultFormat = Start.USER_REGISTER_SUCCESS_MESSAGE_FORMAT;
        }
        verify(bot, atLeastOnce()).sendMessage(any(Long.class), captor.capture());
        assertThat(captor.getValue()).isEqualTo(String.format(resultFormat, username));
    }
}
