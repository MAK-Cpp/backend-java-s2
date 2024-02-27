package edu.java.bot.requests.chains;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;

@SuppressWarnings("checkstyle:MethodName")
public class EditMessageTextChains extends AbstractChains<EditMessageTextChains, EditMessageText> {
    // chain functions
    private static final ChainFunction<EditMessageText> MARKDOWN_FUNC =
        (request) -> request.parseMode(ParseMode.Markdown);
    private static final ChainFunction<EditMessageText> DISABLE_PREVIEW_FUNC =
        (request) -> request.disableWebPagePreview(true);
    // chains
    public static final EditMessageTextChains EMT_MARKDOWN = new EditMessageTextChains(MARKDOWN_FUNC);
    public static final EditMessageTextChains EMT_DISABLE_PREVIEW = new EditMessageTextChains(DISABLE_PREVIEW_FUNC);

    // chains with parameters
    public static EditMessageTextChains EMT_REPLY_MARKUP(InlineKeyboardMarkup replyMarkup) {
        return new EditMessageTextChains((request) -> request.replyMarkup(replyMarkup));
    }

    public EditMessageTextChains(ChainFunction<EditMessageText> function) {
        super(function);
    }

    @Override
    protected EditMessageTextChains instance(ChainFunction<EditMessageText> function) {
        return new EditMessageTextChains(function);
    }
}
