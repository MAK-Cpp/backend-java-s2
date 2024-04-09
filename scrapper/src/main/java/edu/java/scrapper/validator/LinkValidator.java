package edu.java.scrapper.validator;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

public interface LinkValidator {
    Matcher match(String rawLink);

    default boolean isValid(String rawLink) {
        return match(rawLink).matches();
    }

    Optional<Map<String, String>> getRequestArguments(String link);
}
