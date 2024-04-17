package edu.java.scrapper.validator.stackoverflow;

import edu.java.scrapper.client.stackoverflow.StackOverflowClient;
import edu.java.scrapper.response.stackoverflow.AnswerResponse;
import edu.java.scrapper.validator.ValidatorKey;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowQuestionLinkValidator extends AbstractStackOverflowLinkValidator {
    private static final Pattern STACKOVERFLOW_QUESTION_PATTERN = compilePattern(
        "^https?://(?:www\\.)?stackoverflow\\.com/questions/{id}/{title}/?$", ID, TITLE
    );

    @Autowired
    public StackOverflowQuestionLinkValidator(StackOverflowClient stackOverflowClient) {
        super(stackOverflowClient);
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Map<ValidatorKey, String> getArguments(Matcher matcher) {
        return Map.of(
            ID, matcher.group(1),
            TITLE, matcher.group(2)
        );
    }

    @Override
    protected Optional<String> getUpdateDescription(Map<ValidatorKey, String> arguments, OffsetDateTime updatesFrom) {
        final List<AnswerResponse.Answer> response = stackOverflowClient
            .getQuestionAnswers(arguments.get(ID))
            .getAnswers()
            .stream()
            .filter(ans -> ans.getCreationDate().isAfter(updatesFrom))
            .toList();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        final StringBuilder builder = new StringBuilder("New answers:\n");
        for (int i = 0; i < response.size(); i++) {
            final AnswerResponse.Answer answer = response.get(i);
            processResponse(builder, i + 1,
                Map.entry("Author", answer.getOwner()),
                Map.entry("Created at", answer.getCreationDate()),
                Map.entry("Text", '"' + answer.getMessage() + '"')
            );
        }
        return Optional.of(builder.toString());
    }

    @Override
    public Matcher match(String rawLink) {
        return STACKOVERFLOW_QUESTION_PATTERN.matcher(rawLink);
    }
}
