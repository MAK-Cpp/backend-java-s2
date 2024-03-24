package edu.java.bot.command;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.Link;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.TelegramBotComponentTest;
import edu.java.bot.User;
import edu.java.bot.request.chains.EditMessageTextChains;
import edu.java.bot.request.chains.SendMessageChains;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import java.util.Objects;
import java.util.Optional;
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

public class UntrackTest {
    private static final TelegramBotComponent bot = mock(TelegramBotComponent.class);
    private static final Update update = mock(Update.class);
    private static final Message message = mock(Message.class);
    private static final Chat chat = mock(Chat.class);
    private static final ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    private static final SendResponse sendResponse = mock(SendResponse.class);
    private static final Message botMessage = mock(Message.class);
    private static final CallbackQuery callbackQuery = mock(CallbackQuery.class);
    private static final com.pengrad.telegrambot.model.User telegramUser =
        mock(com.pengrad.telegrambot.model.User.class);

    @BeforeAll
    public static void beforeAll() {
        when(update.message()).thenReturn(message);
        when(update.callbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.from()).thenReturn(telegramUser);
        when(message.chat()).thenReturn(chat);
        when(bot.sendMessage(anyLong(), anyString(), any(SendMessageChains[].class))).thenReturn(sendResponse);
        when(sendResponse.message()).thenReturn(botMessage);
    }

    private static Arguments testUntrack(long chatId, int messageId, boolean contains, Link... links) {
        if (contains && (links.length > 0)) {
            return Arguments.of(
                chatId,
                messageId,
                new User(links),
                DESCRIPTION_MESSAGE
            );
        } else if (!contains) {
            return Arguments.of(
                chatId,
                messageId,
                null,
                UNREGISTERED_USER_ERROR
            );
        } else {
            return Arguments.of(
                chatId,
                messageId,
                new User(),
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
                TelegramBotComponentTest.MAXIM_TELEGRAM,
                GOOGLE,
                TelegramBotComponentTest.THIS_REPO
            )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testUntrack(long chatId, int messageId, User user, String result) {
        Optional<User> optionalUser = user == null ? Optional.empty() : Optional.of(user);
        when(chat.id()).thenReturn(chatId);
        when(bot.containUser(eq(chatId))).thenReturn(user != null);
        when(bot.getUser(eq(chatId))).thenReturn(optionalUser);
        when(botMessage.messageId()).thenReturn(messageId);
        untrack(bot, update);
        verify(bot, atLeastOnce()).sendMessage(eq(chatId), messageCaptor.capture(), any(SendMessageChains[].class));
        assertThat(messageCaptor.getValue()).isEqualTo(result);
    }

    private static Arguments testChooseLink(long chatId, int messageId, String chose, Link... links) {
        if (!Objects.equals(chose, CANCEL_BUTTON_TEXT)) {
            User user = new User(links);
            return Arguments.of(
                chatId,
                messageId,
                user,
                chose,
                String.format(CONFIRM_MESSAGE_FORMAT, user.getLink(chose).get())
            );
        } else {
            return Arguments.of(
                chatId,
                messageId,
                new User(),
                chose,
                ABORTED_MESSAGE
            );
        }
    }

    public static Stream<Arguments> testChooseLink() {
        return Stream.of(
            testChooseLink(1L, 1, CANCEL_BUTTON_TEXT),
            testChooseLink(2L, 2, GOOGLE.getAlias(), GOOGLE, MAXIM_TELEGRAM, THIS_REPO),
            testChooseLink(3L, 3, MAXIM_TELEGRAM.getAlias(), MAXIM_TELEGRAM, GOOGLE)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testChooseLink(long chatId, int messageId, User user, String chose, String result) {
        CommandFunction function = chooseLink(messageId);
        when(callbackQuery.data()).thenReturn(chose);
        when(telegramUser.id()).thenReturn(chatId);
        when(bot.getUser(chatId)).thenReturn(Optional.of(user));
        function.apply(bot, update);
        verify(bot, atLeastOnce()).editMessageText(
            eq(chatId),
            eq(messageId),
            messageCaptor.capture(),
            any(EditMessageTextChains[].class)
        );
        assertThat(messageCaptor.getValue()).isEqualTo(result);
    }

    private static Arguments testConfirmDelete(long chatId, int messageId, boolean isYes, Link toRemove, Link... links) {
        if (isYes) {
            return Arguments.of(
                chatId,
                messageId,
                toRemove,
                new User(links),
                YES_BUTTON_TEXT,
                String.format(SUCCESS_MESSAGE_FORMAT, toRemove)
            );
        } else {
            return Arguments.of(
                chatId,
                messageId,
                toRemove,
                new User(links),
                NO_BUTTON_TEXT,
                DESCRIPTION_MESSAGE
            );
        }
    }

    private static Stream<Arguments> testConfirmDelete() {
        return Stream.of(
            testConfirmDelete(1L, 1, true, GOOGLE, THIS_REPO, GOOGLE, MAXIM_TELEGRAM),
            testConfirmDelete(2L, 2, false, MAXIM_TELEGRAM, GOOGLE)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testConfirmDelete(long chatId, int messageId, Link link, User user, String chose, String result) {
        CommandFunction function = confirmDelete(messageId, link.getAlias());
        when(callbackQuery.data()).thenReturn(chose);
        when(telegramUser.id()).thenReturn(chatId);
        when(bot.getUser(eq(chatId))).thenReturn(Optional.of(user));
        function.apply(bot, update);
        verify(bot, atLeastOnce()).editMessageText(
            eq(chatId),
            eq(messageId),
            messageCaptor.capture(),
            any(EditMessageTextChains[].class)
        );
        assertThat(messageCaptor.getValue()).isEqualTo(result);
    }
}
