package edu.java.bot.command;

import edu.java.bot.Link;
import edu.java.bot.request.chains.SendMessageChains;
import edu.java.dto.exception.NonExistentChatException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static edu.java.bot.TelegramBotComponentTest.GOOGLE;
import static edu.java.bot.TelegramBotComponentTest.MAXIM_TELEGRAM;
import static edu.java.bot.TelegramBotComponentTest.THIS_REPO;
import static edu.java.bot.command.Track.createResult;
import static edu.java.bot.command.Track.parseLinks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TrackTest extends CommandTest {
    private static final Track TRACK = new Track();

    private static Arguments testTrack(long chatId, boolean registered) {
        if (registered) {
            return Arguments.of(
                chatId,
                true,
                Track.DESCRIPTION_MESSAGE
            );
        } else {
            return Arguments.of(
                chatId,
                false,
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
    void testTrack(long chatId, boolean registered, String result) {
        if (!registered) {
            Mockito.when(SCRAPPER_HTTP_CLIENT.getChat(Mockito.eq(chatId)))
                .thenThrow(new NonExistentChatException(Command.UNREGISTERED_USER_ERROR));
        }
        Mockito.when(CHAT.id()).thenReturn(chatId);
        TRACK.getFunction().apply(BOT, UPDATE);
        Mockito.verify(BOT, Mockito.atLeastOnce()).sendMessage(
            eq(chatId),
            STRING_ARGUMENT_CAPTOR.capture(),
            any(SendMessageChains[].class)
        );
        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(result);
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
        return Arguments.of(chatId, inputBuilder.toString(), createResult(result));
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
    void testParseLinks(long chatId, String input, String output) {
        Mockito.when(CHAT.id()).thenReturn(chatId);
        Mockito.when(MESSAGE.text()).thenReturn(input);
        parseLinks(BOT, UPDATE);
        Mockito.verify(BOT, Mockito.atLeastOnce()).sendMessage(
            eq(chatId),
            STRING_ARGUMENT_CAPTOR.capture(),
            any(SendMessageChains[].class)
        );
        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(output);
    }
}
