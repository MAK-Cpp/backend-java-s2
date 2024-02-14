package edu.java.bot;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static edu.java.bot.CommandFunction.createHelp;
import static edu.java.bot.CommandFunction.createLinksList;
import static edu.java.bot.CommandFunction.createParseResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TelegramBotComponentTest {
    private static final UserRecord TEST_USER_1 = new UserRecord(123456789L, "TEST_USER_1");
    private static final UserRecord TEST_USER_2 = new UserRecord(1556L, "TEST_USER_2");
    private static final Link MAXIM_TELEGRAM = new Link("Maxim Primakov", "t.me/MAK_Cpp");
    private static final Link GOOGLE = new Link("Google", "google.com");
    private static final Link THIS_REPO =
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
            HELP = createHelp(bot);
        }
    }

    private Update newUpdateMock(final RequestType type, long chatId, final String username, final String text) {
        Update update = mock(Update.class);
        switch (type) {
            case SEND_MESSAGE -> {
                Message message = mock(Message.class);
                Chat chat = mock(Chat.class);
                when(update.message()).thenReturn(message);
                // message
                when(message.chat()).thenReturn(chat);
                when(message.text()).thenReturn(text);
                // chat
                when(chat.id()).thenReturn(chatId);
                when(chat.username()).thenReturn(username);
                // callbackQuery
                when(update.callbackQuery()).thenReturn(null);
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

    private void testSendMessage(final Request request) {
        final Update update =
            newUpdateMock(RequestType.SEND_MESSAGE, request.user.id, request.user.name, request.command);

        bot.updateListener(List.of(update));
        verify(bot, atLeastOnce()).execute(sendMessageArgumentCaptor.capture());
        final Map<String, Object> arguments = sendMessageArgumentCaptor.getValue().getParameters();

        assertThat(arguments.get("chat_id")).isEqualTo(update.message().chat().id());
        assertThat(arguments.get("text")).isEqualTo(request.out);
    }

    private void testEditMessageText(final Request request) {
        final Update update =
            newUpdateMock(RequestType.EDIT_MESSAGE_TEXT, request.user.id, request.user.name, request.command);

        bot.updateListener(List.of(update));
        verify(bot, atLeastOnce()).execute(editMessageTextArgumentCaptor.capture());
        final Map<String, Object> arguments = editMessageTextArgumentCaptor.getValue().getParameters();
        System.out.println(arguments);
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

    private static Request help(final UserRecord userRecord) {
        return new Request(userRecord, "/help", HELP);
    }

    private static Request track(final UserRecord userRecord) {
        return new Request(userRecord, "/track", TelegramBotComponent.TRACK_DESCRIPTION);
    }
    private static Request untrack(final UserRecord userRecord) {
        return new Request(userRecord, "/untrack", TelegramBotComponent.UNTRACK_DESCRIPTION);
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

    private static Request links(final UserRecord userRecord, final LinkWithCorrectness... links) {
        final StringBuilder text = new StringBuilder();
        final StringBuilder result = new StringBuilder("Result:\n");
        int countCorrect = 0;
        for (LinkWithCorrectness link : links) {
            final String line;
            final String parseResult;
            if (link.isCorrect) {
                countCorrect++;
                line = link.link.getAlias() + " - " + link.link.getUri();
                parseResult = link.link.toString();
            } else {
                line = link.link.getAlias() + link.link.getUri();
                parseResult = line;
            }
            text.append(line).append('\n');
            result.append(createParseResult(parseResult, link.isCorrect)).append('\n');
        }
        result.append("\nSUCCEED: ").append(countCorrect).append("\nFAILED: ").append(links.length - countCorrect);
        return new Request(userRecord, text.toString(), result.toString());
    }

    private static Arguments tests(final Request... requests) {
        return Arguments.of(List.of(requests));
    }

    private static Arguments tests(final Pair<Request, RequestType>... requests) {
        return Arguments.of(List.of(requests));
    }

    private static Request register(final UserRecord userRecord) {
        return test(userRecord, "/start", "User " + userRecord.name + " was registered!");
    }

    private static String links(final String... links) {
        final StringBuilder text = new StringBuilder();
        for (final String link : links) {
            text.append(link).append('\n');
        }
        return text.toString();
    }

    public static Stream<Arguments> testCommandStart() {
        return Stream.of(
            tests(
                test(TEST_USER_1, "/start", "User TEST_USER_1 was registered!")
            ),
            tests(
                test(TEST_USER_2, "/start", "User TEST_USER_2 was registered!"),
                test(TEST_USER_1, "/start", "User TEST_USER_1 was registered!"),
                test(TEST_USER_2, "/start", "User TEST_USER_2 already registered!"),
                test(TEST_USER_1, "/start", "User TEST_USER_1 already registered!")
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
                test(TEST_USER_1, "/track", TelegramBotComponent.UNREGISTERED_USER_ERROR),
                test(TEST_USER_1, "/untrack", TelegramBotComponent.UNREGISTERED_USER_ERROR),
                test(TEST_USER_1, "/list", TelegramBotComponent.UNREGISTERED_USER_ERROR)
            ),
            tests(
                test(TEST_USER_2, "/track", TelegramBotComponent.UNREGISTERED_USER_ERROR),
                test(TEST_USER_2, "/untrack", TelegramBotComponent.UNREGISTERED_USER_ERROR),
                test(TEST_USER_2, "/list", TelegramBotComponent.UNREGISTERED_USER_ERROR)
            )
        );
    }

    public static Stream<Arguments> testEmptyLinksMessage() {
        return Stream.of(
            tests(
                register(TEST_USER_1),
                test(TEST_USER_1, "/list", TelegramBotComponent.NO_TRACKING_LINKS_ERROR),
                test(TEST_USER_1, "/untrack", TelegramBotComponent.NO_TRACKING_LINKS_ERROR)
            ),
            tests(
                register(TEST_USER_2),
                test(TEST_USER_2, "/untrack", TelegramBotComponent.NO_TRACKING_LINKS_ERROR),
                test(TEST_USER_2, "/list", TelegramBotComponent.NO_TRACKING_LINKS_ERROR),
                test(TEST_USER_2, "/track", TelegramBotComponent.TRACK_DESCRIPTION),
                test(TEST_USER_2, "/list", TelegramBotComponent.NO_TRACKING_LINKS_ERROR),
                test(TEST_USER_2, "/untrack", TelegramBotComponent.NO_TRACKING_LINKS_ERROR)
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
                test(TEST_USER_2, "/list", TelegramBotComponent.NO_TRACKING_LINKS_ERROR)
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
            testSendMessage(input);
        }
    }

    private static Stream<Arguments> testUntrackCommand() {
        return Stream.of(
            tests(
                test(register(TEST_USER_1), RequestType.SEND_MESSAGE),
                test(track(TEST_USER_1), RequestType.SEND_MESSAGE),
                test(links(TEST_USER_1, correct(MAXIM_TELEGRAM), correct(GOOGLE)), RequestType.SEND_MESSAGE),
                test(untrack(TEST_USER_1), RequestType.SEND_MESSAGE)
            )
        );
    }

    @ParameterizedTest
    @MethodSource({"testUntrackCommand"})
    void testAllCommands(final List<Pair<Request, RequestType>> inputs) {
        for (Pair<Request, RequestType> input : inputs) {
            switch (input.second) {
                case SEND_MESSAGE -> testSendMessage(input.first);
                case EDIT_MESSAGE_TEXT -> testEditMessageText(input.first);
            }
        }
    }

    // TEST RECORDS, ENUMS, CLASSES AND INTERFACES

    private record UserRecord(long id, String name) {
    }

    private record Request(UserRecord user, String command, String out) {
    }

    private record LinkWithCorrectness(Link link, Boolean isCorrect) {
    }

    private enum RequestType {
        SEND_MESSAGE, EDIT_MESSAGE_TEXT;
    }

    private record Pair<T, V>(T first, V second) {
    }
}
