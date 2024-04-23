package edu.java.scrapper.validator.github;

import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.response.github.PullRequestResponse;
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
public class GitHubPullRequestsLinkValidator extends AbstractGitHubLinkValidator {
    private static final Pattern GITHUB_PULL_REQUESTS_PATTERN = compilePattern(
        "^https?://(?:www\\.)?github\\.com/{owner}/{repo}/pulls/?$", OWNER, REPO
    );

    @Autowired
    public GitHubPullRequestsLinkValidator(GithubClient githubClient) {
        super(githubClient);
    }

    @Override
    public Pattern getPattern() {
        return GITHUB_PULL_REQUESTS_PATTERN;
    }

    @Override
    public Matcher match(String rawLink) {
        return GITHUB_PULL_REQUESTS_PATTERN.matcher(rawLink);
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Map<ValidatorKey, String> getArguments(Matcher matcher) {
        return Map.of(
            OWNER, matcher.group(1),
            REPO, matcher.group(2)
        );
    }

    @Override
    protected Optional<String> getUpdateDescription(Map<ValidatorKey, String> arguments, OffsetDateTime updatesFrom) {
        final List<PullRequestResponse> response = githubClient
            .getPullRequests(arguments.get(OWNER), arguments.get(REPO))
            .stream()
            .filter(resp -> resp.getCreatedAt().isAfter(updatesFrom))
            .toList();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        final StringBuilder builder = new StringBuilder("New pull requests:\n");
        for (int i = 0; i < response.size(); i++) {
            final PullRequestResponse pullRequest = response.get(i);
            processResponse(builder, i + 1,
                Map.entry("Author", pullRequest.getUser()),
                Map.entry("Created at", pullRequest.getCreatedAt()),
                Map.entry("State", pullRequest.getState()),
                Map.entry("Title", pullRequest.getTitle()),
                Map.entry("Body", "{\n" + pullRequest.getBody() + "\n}")
            );
        }
        return Optional.of(builder.toString());
    }
}
