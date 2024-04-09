package edu.java.scrapper.validator.github;

import edu.java.scrapper.validator.AbstractLinkValidator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class GitHubIssuesLinkValidator extends AbstractLinkValidator {
    private static final Pattern GITHUB_ISSUES_PATTERN = Pattern.compile(
        "^https?://(www\\.)?github\\.com/([^/]+)/([^/]+)/issues/?$",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public Matcher match(String rawLink) {
        return GITHUB_ISSUES_PATTERN.matcher(rawLink);
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Map<String, String> getArguments(Matcher matcher) {
        return Map.of(
            "owner", matcher.group(2),
            "repo", matcher.group(3)
        );
    }
}