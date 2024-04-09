package edu.java.scrapper.validator;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractLinkValidator implements LinkValidator {
    abstract protected Map<ValidatorKey, String> getArguments(Matcher matcher);

    abstract protected Optional<String> getUpdateDescription(
        Map<ValidatorKey, String> arguments,
        OffsetDateTime updatesFrom
    );

    protected static Pattern compilePattern(String patternFormat, ValidatorKey... keys) {
        String resultPattern = patternFormat;
        for (ValidatorKey key : keys) {
            resultPattern = resultPattern.replace("{" + key.key() + "}", "(" + key.regex() + ")");
        }
        return Pattern.compile(resultPattern, Pattern.CASE_INSENSITIVE);
    }

    @SafeVarargs
    protected static void processResponse(
        StringBuilder result,
        int rowNumber,
        Map.Entry<String, Object>... parameters
    ) {
        final String number = "[" + rowNumber + "] ";
        final String space = " ".repeat(number.length());
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                result.append('\n').append(number);
            } else {
                result.append(space);
            }
            final Map.Entry<String, Object> parameter = parameters[i];
            result.append(parameter.getKey()).append(": ").append(parameter.getValue()).append('\n');
        }
    }

    @Override
    public final Optional<Map<ValidatorKey, String>> getRequestArguments(String link) {
        final Matcher matcher = match(link);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(getArguments(matcher));
    }

    @Override
    public final Optional<String> getUpdateDescription(String link, OffsetDateTime updatesFrom) {
        Optional<Map<ValidatorKey, String>> optionalArguments = getRequestArguments(link);
        if (optionalArguments.isEmpty()) {
            return Optional.empty();
        }
        return getUpdateDescription(optionalArguments.get(), updatesFrom);
    }

    @Override
    public final boolean isValid(String rawLink) {
        return match(rawLink).matches();
    }
}
