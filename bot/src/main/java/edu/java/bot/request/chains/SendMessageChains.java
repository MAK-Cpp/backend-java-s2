package edu.java.bot.request.chains;

import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

@SuppressWarnings("checkstyle:MethodName")
public class SendMessageChains extends AbstractChains<SendMessageChains, SendMessage> {
    // chain functions
    private static final ChainFunction<SendMessage> DISABLE_PREVIEW_FUNC =
        (request) -> request.disableWebPagePreview(true);
    private static final ChainFunction<SendMessage> MARKDOWN_FUNC = (request) -> request.parseMode(ParseMode.Markdown);
    // chains
    public static final SendMessageChains SM_MARKDOWN = new SendMessageChains(MARKDOWN_FUNC);
    public static final SendMessageChains SM_DISABLE_PREVIEW = new SendMessageChains(DISABLE_PREVIEW_FUNC);

    // chains with parameters
    public static SendMessageChains SM_REPLY_MARKUP(Keyboard replyMarkup) {
        return new SendMessageChains((request) -> request.replyMarkup(replyMarkup));
    }

    public SendMessageChains(ChainFunction<SendMessage> function) {
        super(function);
    }

    @Override
    protected SendMessageChains instance(ChainFunction<SendMessage> function) {
        return new SendMessageChains(function);
    }
}
