package edu.java.scrapper.validator.stackoverflow;

import edu.java.scrapper.validator.AbstractLinkValidator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowQuestionLinkValidator extends AbstractLinkValidator {
    private static final Pattern STACKOVERFLOW_QUESTION_PATTERN = Pattern.compile(
        "^https?://(www\\.)?stackoverflow\\.com/questions/(\\d+)/([a-zA-Z0-9-]+)/?$",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Map<String, String> getArguments(Matcher matcher) {
        return Map.of(
            "id", matcher.group(2),
            "title", matcher.group(3)
        );
    }

    @Override
    public Matcher match(String rawLink) {
        return STACKOVERFLOW_QUESTION_PATTERN.matcher(rawLink);
    }
}
