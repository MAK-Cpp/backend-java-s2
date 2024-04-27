package edu.java.bot.command;

import edu.java.dto.exception.APIException;
import edu.java.dto.exception.WrongParametersException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartTest extends CommandTest {
    private static final Start START = new Start();

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
        when(CHAT.id()).thenReturn(chatId);
        when(CHAT.username()).thenReturn(username);
        if (contains) {
            Mockito.doThrow(APIException.class).when(SCRAPPER_HTTP_CLIENT).registerChat(Mockito.eq(chatId));
        }
        assertThat(START.getFunction().apply(BOT, UPDATE)).isEqualTo(CommandFunction.END);
        String resultFormat;
        if (contains) {
            resultFormat = Start.USER_REGISTER_FAILED_MESSAGE_FORMAT;
        } else {
            resultFormat = Start.USER_REGISTER_SUCCESS_MESSAGE_FORMAT;
        }
        verify(BOT, atLeastOnce()).sendMessage(any(Long.class), STRING_ARGUMENT_CAPTOR.capture());
        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(String.format(resultFormat, username));
    }
}
