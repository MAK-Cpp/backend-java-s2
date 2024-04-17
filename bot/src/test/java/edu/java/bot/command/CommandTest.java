package edu.java.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.TelegramBotComponent;
import edu.java.bot.client.ScrapperHttpClient;
import edu.java.bot.client.ScrapperHttpClientImpl;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class CommandTest {
    protected static final TelegramBotComponent BOT = Mockito.mock(TelegramBotComponent.class);
    protected static final ScrapperHttpClient SCRAPPER_HTTP_CLIENT = Mockito.mock(ScrapperHttpClientImpl.class);
    protected static final Update UPDATE = Mockito.mock(Update.class);
    protected static final Message MESSAGE = mock(Message.class);
    protected static final Chat CHAT = mock(Chat.class);
    protected static final ArgumentCaptor<String> STRING_ARGUMENT_CAPTOR = ArgumentCaptor.forClass(String.class);

    @BeforeAll
    public static void setUp() {
        Mockito.when(BOT.getScrapperHttpClient()).thenReturn(SCRAPPER_HTTP_CLIENT);
        when(UPDATE.message()).thenReturn(MESSAGE);
        when(MESSAGE.chat()).thenReturn(CHAT);
    }
}
