package edu.java.bot.command;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.Link;
import edu.java.bot.request.chains.EditMessageTextChains;
import edu.java.bot.request.chains.SendMessageChains;
import edu.java.dto.exception.APIException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import java.util.Objects;
import java.util.stream.Stream;
import static edu.java.bot.TelegramBotComponentTest.GOOGLE;
import static edu.java.bot.TelegramBotComponentTest.MAXIM_TELEGRAM;
import static edu.java.bot.TelegramBotComponentTest.THIS_REPO;
import static edu.java.bot.command.Command.NO_TRACKING_LINKS_ERROR;
import static edu.java.bot.command.Command.UNREGISTERED_USER_ERROR;
import static edu.java.bot.command.Untrack.ABORTED_MESSAGE;
import static edu.java.bot.command.Untrack.CANCEL_BUTTON_TEXT;
import static edu.java.bot.command.Untrack.CONFIRM_MESSAGE_FORMAT;
import static edu.java.bot.command.Untrack.DESCRIPTION_MESSAGE;
import static edu.java.bot.command.Untrack.NO_BUTTON_TEXT;
import static edu.java.bot.command.Untrack.SUCCESS_MESSAGE_FORMAT;
import static edu.java.bot.command.Untrack.YES_BUTTON_TEXT;
import static edu.java.bot.command.Untrack.chooseLink;
import static edu.java.bot.command.Untrack.confirmDelete;
import static edu.java.bot.command.Untrack.untrack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UntrackTest extends CommandTest {
    private static final SendResponse SEND_RESPONSE = mock(SendResponse.class);
    private static final Message BOT_MESSAGE = mock(Message.class);
    private static final CallbackQuery CALLBACK_QUERY = mock(CallbackQuery.class);
    private static final User TELEGRAM_USER =
        mock(User.class);

    @BeforeAll
    public static void beforeAll() {
        when(UPDATE.callbackQuery()).thenReturn(CALLBACK_QUERY);
        when(CALLBACK_QUERY.from()).thenReturn(TELEGRAM_USER);
        when(BOT.sendMessage(anyLong(), anyString(), any(SendMessageChains[].class))).thenReturn(SEND_RESPONSE);
        when(SEND_RESPONSE.message()).thenReturn(BOT_MESSAGE);
    }

    private static Arguments testUntrack(long chatId, int messageId, boolean contains, Link... links) {
        if (contains && (links.length > 0)) {
            return Arguments.of(
                chatId,
                messageId,
                DESCRIPTION_MESSAGE
            );
        } else if (!contains) {
            return Arguments.of(
                chatId,
                messageId,
                UNREGISTERED_USER_ERROR
            );
        } else {
            return Arguments.of(
                chatId,
                messageId,
                NO_TRACKING_LINKS_ERROR
            );
        }
    }

    public static Stream<Arguments> testUntrack() {
        return Stream.of(
            testUntrack(1L, 1, true),
            testUntrack(2L, 2, false),
            testUntrack(
                3L,
                3,
                true,
                MAXIM_TELEGRAM,
                GOOGLE,
                THIS_REPO
            )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testUntrack(long chatId, int messageId, String result) {
        when(CHAT.id()).thenReturn(chatId);
        when(BOT_MESSAGE.messageId()).thenReturn(messageId);
        untrack(BOT, UPDATE);
        verify(BOT, atLeastOnce()).sendMessage(
            eq(chatId),
            STRING_ARGUMENT_CAPTOR.capture(),
            any(SendMessageChains[].class)
        );
        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(result);
    }

    private static Arguments testChooseLink(long chatId, int messageId, Link chose, boolean containsLink) {
        if (!Objects.equals(chose.getAlias(), CANCEL_BUTTON_TEXT)) {
            if (containsLink) {
                return Arguments.of(
                    chatId,
                    messageId,
                    chose,
                    true,
                    String.format(CONFIRM_MESSAGE_FORMAT, chose)
                );
            } else {
                return Arguments.of(
                    chatId,
                    messageId,
                    chose,
                    false,
                    String.format(Untrack.NO_LINK_ERROR_FORMAT, chose.getAlias())
                );
            }
        } else {
            return Arguments.of(
                chatId,
                messageId,
                chose,
                containsLink,
                ABORTED_MESSAGE
            );
        }
    }

    public static Stream<Arguments> testChooseLink() {
        return Stream.of(
            testChooseLink(1L, 1, new Link(CANCEL_BUTTON_TEXT, "https://example.com"), true),
            testChooseLink(2L, 2, GOOGLE, true),
            testChooseLink(3L, 3, MAXIM_TELEGRAM, false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testChooseLink(long chatId, int messageId, Link chose, boolean containsLink, String result) {
        CommandFunction function = chooseLink(messageId);
        when(CALLBACK_QUERY.data()).thenReturn(chose.getAlias());
        when(TELEGRAM_USER.id()).thenReturn(chatId);
        if (containsLink) {
            when(SCRAPPER_HTTP_CLIENT.getLinkByChatIdAndAlias(eq(chatId), eq(chose.getAlias())))
                .thenReturn(new UserLinkResponse(new LinkResponse(0L, chose.getUri(), null), chose.getAlias()));
        } else {
            when(SCRAPPER_HTTP_CLIENT.getLinkByChatIdAndAlias(eq(chatId), eq(chose.getAlias())))
                .thenThrow(
                    new APIException(
                        HttpStatus.BAD_REQUEST,
                        new WrongParametersException("There is no link")
                    )
                );
        }
        function.apply(BOT, UPDATE);
        verify(BOT, atLeastOnce()).editMessageText(
            eq(chatId),
            eq(messageId),
            STRING_ARGUMENT_CAPTOR.capture(),
            any(EditMessageTextChains[].class)
        );
        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(result);
    }

    private static Arguments testConfirmDelete(long chatId, int messageId, Choose choose, Link toRemove) {
        return switch (choose) {
            case YES -> Arguments.of(
                chatId,
                messageId,
                toRemove,
                YES_BUTTON_TEXT,
                String.format(SUCCESS_MESSAGE_FORMAT, toRemove)
            );
            case NO -> Arguments.of(
                chatId,
                messageId,
                toRemove,
                NO_BUTTON_TEXT,
                DESCRIPTION_MESSAGE
            );
        };
    }

    private static Stream<Arguments> testConfirmDelete() {
        return Stream.of(
            testConfirmDelete(1L, 1, Choose.YES, GOOGLE),
            testConfirmDelete(2L, 2, Choose.NO, MAXIM_TELEGRAM)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testConfirmDelete(long chatId, int messageId, Link link, String chose, String result) {
        CommandFunction function = confirmDelete(messageId, link.getAlias());
        when(CALLBACK_QUERY.data()).thenReturn(chose);
        when(SCRAPPER_HTTP_CLIENT.getChat(eq(chatId))).thenReturn(new ChatResponse(chatId));
        when(SCRAPPER_HTTP_CLIENT.getAllLinks(eq(chatId))).thenReturn(new ListUserLinkResponse(
            new UserLinkResponse[0],
            0
        ));
        when(TELEGRAM_USER.id()).thenReturn(chatId);
        when(SCRAPPER_HTTP_CLIENT.getLinkByChatIdAndAlias(eq(chatId), eq(link.getAlias())))
            .thenReturn(new UserLinkResponse(new LinkResponse(0L, link.getUri(), null), link.getAlias()));
        function.apply(BOT, UPDATE);
        verify(BOT, atLeastOnce()).editMessageText(
            eq(chatId),
            eq(messageId),
            STRING_ARGUMENT_CAPTOR.capture(),
            any(EditMessageTextChains[].class)
        );
        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(result);
    }

    private enum Choose {
        YES, NO
    }
}
