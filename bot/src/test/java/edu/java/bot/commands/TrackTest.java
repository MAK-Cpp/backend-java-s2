package edu.java.bot.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.User;
import edu.java.bot.requests.chains.SendMessageChains;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import static edu.java.bot.TelegramBotComponentTest.GOOGLE;
import static edu.java.bot.TelegramBotComponentTest.MAXIM_TELEGRAM;
import static edu.java.bot.TelegramBotComponentTest.THIS_REPO;
import static edu.java.bot.commands.Track.createResult;
import static edu.java.bot.commands.Track.parseLinks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) public class TrackTest {
    private static final Track track = new Track();
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

    private static Arguments testTrack(long chatId, boolean contains) {
        if (contains) {
            return Arguments.of(
                chatId,
                new User(),
                Track.DESCRIPTION_MESSAGE
            );
        } else {
            return Arguments.of(
                chatId,
                null,
                Command.UNREGISTERED_USER_ERROR
            );
        }
    }

    public static Stream<Arguments> testTrack() {
        return Stream.of(
            testTrack(1L, true),
            testTrack(2L, false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testTrack(long chatId, User user, String result) {
        when(chat.id()).thenReturn(chatId);
        when(bot.containUser(eq(chatId))).thenReturn(user != null);
        track.getFunction().apply(bot, update);
        verify(bot, atLeastOnce()).sendMessage(eq(chatId), messageCaptor.capture(), any(SendMessageChains[].class));
        assertThat(messageCaptor.getValue()).isEqualTo(result);
    }

    @SafeVarargs
    private static Arguments testParseLink(long chatId, Map.Entry<Link, Boolean>... links) {
        final StringBuilder inputBuilder = new StringBuilder();
        List<Map.Entry<String, Boolean>> result = Arrays.stream(links).map(entry -> {
            final Link link = entry.getKey();
            final Boolean isCorrect = entry.getValue();
            if (isCorrect) {
                inputBuilder.append(link.getAlias()).append(" - ").append(link.getUri()).append('\n');
                return Map.entry(link.toString(), true);
            } else {
                final String line = link.getAlias() + " @ " + link.getUri();
                inputBuilder.append(line).append('\n');
                return Map.entry(line, false);
            }
        }).toList();
        return Arguments.of(chatId, new User(), inputBuilder.toString(), createResult(result));
    }

    public static Stream<Arguments> testParseLinks() {
        return Stream.of(
            testParseLink(1L, Map.entry(MAXIM_TELEGRAM, true), Map.entry(GOOGLE, true), Map.entry(THIS_REPO, false)),
            testParseLink(2L, Map.entry(MAXIM_TELEGRAM, false), Map.entry(GOOGLE, true), Map.entry(THIS_REPO, false)),
            testParseLink(3L, Map.entry(MAXIM_TELEGRAM, false), Map.entry(GOOGLE, false), Map.entry(THIS_REPO, false))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testParseLinks(long chatId, User user, String input, String output) {
        when(chat.id()).thenReturn(chatId);
        when(bot.getUser(eq(chatId))).thenReturn(Optional.of(user));
        when(message.text()).thenReturn(input);
        parseLinks(bot, update);
        verify(bot, atLeastOnce()).sendMessage(eq(chatId), messageCaptor.capture(), any(SendMessageChains[].class));
        assertThat(messageCaptor.getValue()).isEqualTo(output);
    }
}
