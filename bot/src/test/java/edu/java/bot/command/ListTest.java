package edu.java.bot.command;

import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponentTest;
import edu.java.bot.request.chains.SendMessageChains;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListTest extends CommandTest {
    private static final List LIST = new List();

    private static Arguments testList(long chatId, boolean registered, Link... links) {
        ListUserLinkResponse userLinkResponse = new ListUserLinkResponse(
            Arrays.stream(links)
                .map(link ->
                    new UserLinkResponse(new LinkResponse(null, link.getUri(), null), link.getAlias()))
                .toArray(UserLinkResponse[]::new),
            links.length
        );
        if (registered && (links.length > 0)) {
            return Arguments.of(
                chatId,
                true,
                userLinkResponse,
                List.createLinksList(java.util.List.of(links))
            );
        } else if (!registered) {
            return Arguments.of(
                chatId,
                false,
                null,
                List.UNREGISTERED_USER_ERROR
            );
        } else {
            return Arguments.of(
                chatId,
                true,
                null,
                List.NO_TRACKING_LINKS_ERROR
            );
        }
    }

    public static Stream<Arguments> testList() {
        return Stream.of(
            testList(1L, true),
            testList(2L, false),
            testList(
                3L,
                true,
                TelegramBotComponentTest.MAXIM_TELEGRAM,
                TelegramBotComponentTest.GOOGLE,
                TelegramBotComponentTest.THIS_REPO
            )
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testList(long chatId, boolean registered, ListUserLinkResponse links, String result) {
        when(CHAT.id()).thenReturn(chatId);
        if (links != null) {
            Mockito.when(SCRAPPER_HTTP_CLIENT.getAllLinks(Mockito.eq(chatId))).thenReturn(links);
            Mockito.when(SCRAPPER_HTTP_CLIENT.getChat(Mockito.eq(chatId))).thenReturn(new ChatResponse(chatId));
        } else if (registered) {
            Mockito.when(SCRAPPER_HTTP_CLIENT.getAllLinks(Mockito.eq(chatId)))
                .thenThrow(new WrongParametersException(List.NO_TRACKING_LINKS_ERROR));
            Mockito.when(SCRAPPER_HTTP_CLIENT.getChat(Mockito.eq(chatId))).thenReturn(new ChatResponse(chatId));
        } else {
            Mockito.when(SCRAPPER_HTTP_CLIENT.getAllLinks(Mockito.eq(chatId)))
                .thenThrow(new NonExistentChatException(List.UNREGISTERED_USER_ERROR));
            Mockito.when(SCRAPPER_HTTP_CLIENT.getChat(Mockito.eq(chatId)))
                .thenThrow(new NonExistentChatException(List.UNREGISTERED_USER_ERROR));
        }
        assertThat(LIST.getFunction().apply(BOT, UPDATE)).isEqualTo(CommandFunction.END);
        verify(BOT, atLeastOnce()).sendMessage(
            eq(chatId),
            STRING_ARGUMENT_CAPTOR.capture(),
            any(SendMessageChains[].class)
        );
        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(result);
    }
}
