package edu.java.scrapper.validator.github;

import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.response.github.IssueResponse;
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
public class GitHubIssuesLinkValidator extends AbstractGitHubLinkValidator {
    private static final Pattern GITHUB_ISSUES_PATTERN =
        compilePattern("^https?://(?:www\\.)?github\\.com/{owner}/{repo}/issues/?$", OWNER, REPO);

    @Autowired
    public GitHubIssuesLinkValidator(GithubClient githubClient) {
        super(githubClient);
    }

    @Override
    public Matcher match(String rawLink) {
        return GITHUB_ISSUES_PATTERN.matcher(rawLink);
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
        List<IssueResponse> response = githubClient
            .getIssues(arguments.get(OWNER), arguments.get(REPO))
            .stream()
            .filter(resp -> resp.getCreatedAt().isAfter(updatesFrom))
            .toList();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        final StringBuilder builder = new StringBuilder("New issues:\n");
        for (int i = 0; i < response.size(); i++) {
            final IssueResponse issue = response.get(i);
            processResponse(builder, i + 1,
                Map.entry("Author", issue.getUser()),
                Map.entry("Created at", issue.getCreatedAt()),
                Map.entry("State", issue.getState()),
                Map.entry("Title", issue.getTitle()),
                Map.entry("Body", '"' + issue.getBody() + '"')
            );
        }
        return Optional.of(builder.toString());
    }
}
