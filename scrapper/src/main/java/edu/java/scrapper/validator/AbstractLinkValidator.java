package edu.java.scrapper.validator;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

public abstract class AbstractLinkValidator implements LinkValidator {
    abstract protected Map<String, String> getArguments(Matcher matcher);

    @Override
    public final Optional<Map<String, String>> getRequestArguments(String link) {
        final Matcher matcher = match(link);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(getArguments(matcher));
    }
}
