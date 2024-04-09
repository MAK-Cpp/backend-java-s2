package edu.java.scrapper.validator.github;

import edu.java.scrapper.validator.AbstractLinkValidator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class GitHubCertainPullRequestLinkValidator extends AbstractLinkValidator {
    // https://github.com/{owner}/{repo}/pull/{pull_number}
    private static final Pattern GITHUB_CERTAIN_PULL_REQUEST_PATTERN = Pattern.compile(
        "^https?://(www\\.)?github\\.com/([^/]+)/([^/]+)/pull/([^/]+)/?$",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public Matcher match(String rawLink) {
        return GITHUB_CERTAIN_PULL_REQUEST_PATTERN.matcher(rawLink);
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Map<String, String> getArguments(Matcher matcher) {
        return Map.of(
            "owner", matcher.group(2),
            "repo", matcher.group(3),
            "pull_number", matcher.group(4)
        );
    }
}
