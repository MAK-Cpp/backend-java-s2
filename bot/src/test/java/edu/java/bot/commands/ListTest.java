package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.TelegramBotComponentTest;
import edu.java.bot.User;
import edu.java.bot.requests.chains.SendMessageChains;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import java.util.Optional;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListTest {
    private static final List list = new List();
    private static final TelegramBotComponent bot = mock(TelegramBotComponent.class);
    private static final Update update = mock(Update.class);
    private static final Message message = mock(Message.class);
    private static final Chat chat = mock(Chat.class);
    private static final ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

    @BeforeAll
    public static void beforeAll() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
    }

    private static Arguments testList(long chatId, boolean contains, Link... links) {
        if (contains && (links.length > 0)) {
            return Arguments.of(
                chatId,
                new User(links),
                List.createLinksList(links)
            );
        } else if (!contains) {
            return Arguments.of(
                chatId,
                null,
                List.UNREGISTERED_USER_ERROR
            );
        } else {
            return Arguments.of(
                chatId,
                new User(),
                List.NO_TRACKING_LINKS_ERROR
            );
        }
    }

    public static Stream<Arguments> testList() {
        return Stream.of(
            testList(1L, true),
            testList(2L, false),
            testList(3L, true, TelegramBotComponentTest.MAXIM_TELEGRAM, TelegramBotComponentTest.GOOGLE, TelegramBotComponentTest.THIS_REPO)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testList(long chatId, User user, String result) {
        Optional<User> optionalUser = user == null ? Optional.empty() : Optional.of(user);
        when(chat.id()).thenReturn(chatId);
        when(bot.containUser(anyLong())).thenReturn(user != null);
        when(bot.getUser(anyLong())).thenReturn(optionalUser);
        assertThat(list.getFunction().apply(bot, update)).isEqualTo(CommandFunction.END);
        verify(bot, atLeastOnce()).sendMessage(eq(chatId), messageCaptor.capture(), any(SendMessageChains[].class));
        assertThat(messageCaptor.getValue()).isEqualTo(result);
    }
}
