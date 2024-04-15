package edu.java.bot.command;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import java.util.stream.LongStream;
import static org.assertj.core.api.Assertions.assertThat;

public class HelpTest extends CommandTest {
    private static final Help HELP = new Help();
    private static final String USAGE_TEXT = "TEST USAGE TEXT";

    @BeforeAll
    public static void beforeAll() {
        Mockito.when(BOT.getUsage()).thenReturn(USAGE_TEXT);
    }

    public static LongStream testHelp() {
        return LongStream.of(1L, 2L, 3L, 4L, 5L);
    }

    @ParameterizedTest
    @MethodSource
    void testHelp(long chatId) {
        Mockito.when(CHAT.id()).thenReturn(chatId);
        assertThat(HELP.getFunction().apply(BOT, UPDATE)).isEqualTo(CommandFunction.END);
        Mockito.verify(BOT, Mockito.atLeastOnce())
            .sendMessage(Mockito.any(Long.class), STRING_ARGUMENT_CAPTOR.capture());

        assertThat(STRING_ARGUMENT_CAPTOR.getValue()).isEqualTo(USAGE_TEXT);
    }
}
