package edu.java.bot;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.commands.Command;
import edu.java.bot.commands.Start;
import edu.java.bot.commands.Track;
import edu.java.bot.commands.Untrack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static edu.java.bot.commands.List.createLinksList;
import static edu.java.bot.commands.Track.createResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TelegramBotComponentTest {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final UserRecord TEST_USER_1 = new UserRecord(123456789L, "TEST_USER_1");
    private static final UserRecord TEST_USER_2 = new UserRecord(1556L, "TEST_USER_2");
    public static final Link MAXIM_TELEGRAM = new Link("Maxim Primakov", "t.me/MAK_Cpp");
    public static final Link GOOGLE = new Link("Google", "google.com");
    public static final Link THIS_REPO =
        new Link("Backend Java season 2 repository", "https://github.com/MAK-Cpp/backend-java-s2");

    @Autowired
    @SpyBean
    private TelegramBotComponent bot;
    private static String HELP = null;

    @Captor
    private ArgumentCaptor<SendMessage> sendMessageArgumentCaptor;

    @Captor
    private ArgumentCaptor<EditMessageText> editMessageTextArgumentCaptor;

    @BeforeEach
    public void init() {
        bot.deleteAllUsers();
        if (HELP == null) {
            HELP = bot.getUsage();
        }
    }

    private Update newUpdateMock(
        final RequestType type,
        final long chatId,
        final int messageId,
        final String username,
        final String text
    ) {
        Update update = mock(Update.class);
        switch (type) {
            case SEND_MESSAGE -> {
                Message message = mock(Message.class);
                Message botMessage = mock(Message.class);
                Chat chat = mock(Chat.class);
                SendResponse response = mock(SendResponse.class);
                when(update.message()).thenReturn(message);
                // message
                when(message.chat()).thenReturn(chat);
                when(message.text()).thenReturn(text);
                // chat
                when(chat.id()).thenReturn(chatId);
                when(chat.username()).thenReturn(username);
                // callbackQuery
                when(update.callbackQuery()).thenReturn(null);
                // response
                Mockito.doReturn(response).when(bot).execute(any(SendMessage.class));
                when(response.message()).thenReturn(botMessage);
                when(botMessage.messageId()).thenReturn(messageId);
            }
            case EDIT_MESSAGE_TEXT -> {
                CallbackQuery callbackQuery = mock(CallbackQuery.class);
                com.pengrad.telegrambot.model.User user = mock(com.pengrad.telegrambot.model.User.class);
                // message
                when(update.message()).thenReturn(null);
                // callbackQuery
                when(update.callbackQuery()).thenReturn(callbackQuery);
                when(callbackQuery.data()).thenReturn(text);
                when(callbackQuery.from()).thenReturn(user);
                // user
                when(user.id()).thenReturn(chatId);
                when(user.username()).thenReturn(username);
            }
        }
        return update;
    }

    private void testSendMessage(final Request request, final int messageId) {
        final Update update =
            newUpdateMock(RequestType.SEND_MESSAGE, request.user.id, messageId, request.user.name, request.command);

        bot.updateListener(List.of(update));
        verify(bot, atLeastOnce()).execute(sendMessageArgumentCaptor.capture());
        final Map<String, Object> arguments = sendMessageArgumentCaptor.getValue().getParameters();

        assertThat(arguments.get("chat_id")).isEqualTo(update.message().chat().id());
        assertThat(arguments.get("text")).isEqualTo(request.out);
    }

    private void testEditMessageText(final Request request, final int messageId) {
        final Update update = newUpdateMock(
            RequestType.EDIT_MESSAGE_TEXT, request.user.id, messageId, request.user.name, request.command
        );
        bot.updateListener(List.of(update));
        verify(bot, atLeastOnce()).execute(editMessageTextArgumentCaptor.capture());
        final Map<String, Object> arguments = editMessageTextArgumentCaptor.getValue().getParameters();
        assertThat(arguments.get("chat_id")).isEqualTo(update.callbackQuery().from().id());
        assertThat(arguments.get("message_id")).isEqualTo(messageId);
        assertThat(arguments.get("text")).isEqualTo(request.out);
    }

    private static Request test(final UserRecord userRecord, final String text, final String result) {
        return new Request(userRecord, text, result);
    }

    private static Pair<Request, RequestType> test(
        final UserRecord userRecord,
        final String text,
        final RequestType type,
        final String result
    ) {
        return new Pair<>(new Request(userRecord, text, result), type);
    }

    private static Pair<Request, RequestType> test(final Request request, final RequestType type) {
        return new Pair<>(request, type);
    }

    private static Request register(final UserRecord userRecord) {
        return test(
            userRecord, "/start",
            String.format(Start.USER_REGISTER_SUCCESS_MESSAGE_FORMAT, userRecord.name)
        );
    }

    private static Request registerFailed(final UserRecord userRecord) {
        return test(
            userRecord, "/start",
            String.format(Start.USER_REGISTER_FAILED_MESSAGE_FORMAT, userRecord.name)
        );
    }

    private static Request help(final UserRecord userRecord) {
        return new Request(userRecord, "/help", HELP);
    }

    private static Request track(final UserRecord userRecord) {
        return new Request(userRecord, "/track", Track.DESCRIPTION_MESSAGE);
    }

    private static Request untrack(final UserRecord userRecord) {
        return new Request(userRecord, "/untrack", Untrack.DESCRIPTION_MESSAGE);
    }

    private static Request list(final UserRecord userRecord, final Link... links) {
        return new Request(userRecord, "/list", createLinksList(links));
    }

    private static LinkWithCorrectness correct(final Link link) {
        return new LinkWithCorrectness(link, true);
    }

    private static LinkWithCorrectness incorrect(final Link link) {
        return new LinkWithCorrectness(link, false);
    }

    public static Request links(final UserRecord userRecord, final LinkWithCorrectness... links) {
        final StringBuilder text = new StringBuilder();
        List<Map.Entry<String, Boolean>> results = new ArrayList<>();
        for (LinkWithCorrectness link : links) {
            final String line;
            if (link.isCorrect) {
                line = link.link.getAlias() + " - " + link.link.getUri();
                results.add(Map.entry(link.link.toString(), true));
            } else {
                line = link.link.getAlias() + link.link.getUri();
                results.add(Map.entry(line, false));
            }
            text.append(line).append('\n');
        }
        return new Request(userRecord, text.toString(), createResult(results));
    }

    private static Arguments tests(final Request... requests) {
        return Arguments.of(List.of(requests));
    }

    private static Arguments tests(final Pair<Request, RequestType>... requests) {
        return Arguments.of(List.of(requests));
    }

    public static Stream<Arguments> testCommandStart() {
        return Stream.of(
            tests(
                register(TEST_USER_1)
            ),
            tests(
                register(TEST_USER_2),
                register(TEST_USER_1),
                registerFailed(TEST_USER_2),
                registerFailed(TEST_USER_1)
            )
        );
    }

    public static Stream<Arguments> testCommandHelp() {
        return Stream.of(
            tests(help(TEST_USER_1)),
            tests(help(TEST_USER_2)),
            tests(help(TEST_USER_2), help(TEST_USER_1))
        );
    }

    public static Stream<Arguments> testCallCommandsWhileUnregistered() {
        return Stream.of(
            tests(
                test(TEST_USER_1, "/track", Command.UNREGISTERED_USER_ERROR),
                test(TEST_USER_1, "/untrack", Command.UNREGISTERED_USER_ERROR),
                test(TEST_USER_1, "/list", Command.UNREGISTERED_USER_ERROR)
            ),
            tests(
                test(TEST_USER_2, "/track", Command.UNREGISTERED_USER_ERROR),
                test(TEST_USER_2, "/untrack", Command.UNREGISTERED_USER_ERROR),
                test(TEST_USER_2, "/list", Command.UNREGISTERED_USER_ERROR)
            )
        );
    }

    public static Stream<Arguments> testEmptyLinksMessage() {
        return Stream.of(
            tests(
                register(TEST_USER_1),
                test(TEST_USER_1, "/list", Command.NO_TRACKING_LINKS_ERROR),
                test(TEST_USER_1, "/untrack", Command.NO_TRACKING_LINKS_ERROR)
            ),
            tests(
                register(TEST_USER_2),
                test(TEST_USER_2, "/untrack", Command.NO_TRACKING_LINKS_ERROR),
                test(TEST_USER_2, "/list", Command.NO_TRACKING_LINKS_ERROR),
                track(TEST_USER_2),
                test(TEST_USER_2, "/list", Command.NO_TRACKING_LINKS_ERROR),
                test(TEST_USER_2, "/untrack", Command.NO_TRACKING_LINKS_ERROR)
            )
        );
    }

    public static Stream<Arguments> testTrackCommand() {
        return Stream.of(
            tests(
                register(TEST_USER_1),
                track(TEST_USER_1),
                links(TEST_USER_1, correct(MAXIM_TELEGRAM), incorrect(GOOGLE), correct(THIS_REPO))
            ),
            tests(
                register(TEST_USER_2),
                track(TEST_USER_2),
                links(TEST_USER_2, incorrect(THIS_REPO))
            )
        );
    }

    public static Stream<Arguments> testListCommand() {
        return Stream.of(
            tests(
                register(TEST_USER_1),
                track(TEST_USER_1),
                links(TEST_USER_1, correct(MAXIM_TELEGRAM), incorrect(GOOGLE), correct(THIS_REPO)),
                list(TEST_USER_1, MAXIM_TELEGRAM, THIS_REPO)
            ),
            tests(
                register(TEST_USER_2),
                track(TEST_USER_2),
                links(TEST_USER_2, incorrect(THIS_REPO)),
                test(TEST_USER_2, "/list", Command.NO_TRACKING_LINKS_ERROR)
            )
        );
    }

    @ParameterizedTest
    @MethodSource({
        "testCommandStart",
        "testCommandHelp",
        "testCallCommandsWhileUnregistered",
        "testEmptyLinksMessage",
        "testTrackCommand",
        "testListCommand"
    })
    void testSendMessageCommands(final List<Request> inputs) {
        for (final Request input : inputs) {
            testSendMessage(input, RANDOM.nextInt());
        }
    }

    private static Stream<Arguments> testUntrackCommand() {
        return Stream.of(
            tests(
                test(register(TEST_USER_1), RequestType.SEND_MESSAGE),
                test(track(TEST_USER_1), RequestType.SEND_MESSAGE),
                test(links(TEST_USER_1, correct(MAXIM_TELEGRAM), correct(GOOGLE)), RequestType.SEND_MESSAGE),
                test(untrack(TEST_USER_1), RequestType.SEND_MESSAGE),
                test(
                    test(TEST_USER_1, Untrack.CANCEL_BUTTON_TEXT, Untrack.ABORTED_MESSAGE),
                    RequestType.EDIT_MESSAGE_TEXT
                )
            ),
            tests(
                test(register(TEST_USER_2), RequestType.SEND_MESSAGE),
                test(track(TEST_USER_2), RequestType.SEND_MESSAGE),
                test(
                    links(TEST_USER_2, incorrect(MAXIM_TELEGRAM), correct(GOOGLE), correct(THIS_REPO)),
                    RequestType.SEND_MESSAGE
                ),
                test(list(TEST_USER_2, GOOGLE, THIS_REPO), RequestType.SEND_MESSAGE),
                test(untrack(TEST_USER_2), RequestType.SEND_MESSAGE),
                test(test(
                    TEST_USER_2,
                    GOOGLE.getAlias(),
                    String.format(Untrack.CONFIRM_MESSAGE_FORMAT, GOOGLE)
                ), RequestType.EDIT_MESSAGE_TEXT),
                test(test(
                    TEST_USER_2,
                    Untrack.YES_BUTTON_TEXT,
                    String.format(Untrack.SUCCESS_MESSAGE_FORMAT, GOOGLE)
                ), RequestType.EDIT_MESSAGE_TEXT),
                test(list(TEST_USER_2, THIS_REPO), RequestType.SEND_MESSAGE)
            )
        );
    }

    @ParameterizedTest
    @MethodSource({"testUntrackCommand"})
    void testAllCommands(final List<Pair<Request, RequestType>> inputs) {
        int lastMessageId = -1;
        for (Pair<Request, RequestType> input : inputs) {
            switch (input.second) {
                case SEND_MESSAGE -> {
                    lastMessageId = RANDOM.nextInt();
                    testSendMessage(input.first, lastMessageId);
                }
                case EDIT_MESSAGE_TEXT -> testEditMessageText(input.first, lastMessageId);
            }
        }
    }

    // TEST RECORDS, ENUMS, CLASSES AND INTERFACES

    public record UserRecord(long id, String name) {
    }

    public record Request(UserRecord user, String command, String out) {
    }

    public record LinkWithCorrectness(Link link, Boolean isCorrect) {
    }

    public enum RequestType {
        SEND_MESSAGE, EDIT_MESSAGE_TEXT;
    }

    public record Pair<T, V>(T first, V second) {
    }
}
