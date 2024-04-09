package edu.java.scrapper.validator;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

public interface LinkValidator {
    Matcher match(String rawLink);

    boolean isValid(String rawLink);

    Optional<Map<ValidatorKey, String>> getRequestArguments(String link);

    Optional<String> getUpdateDescription(String link, OffsetDateTime updatesFrom);
}
