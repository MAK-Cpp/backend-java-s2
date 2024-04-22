package edu.java.scrapper.validator.github;

import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.response.github.IssueCommentResponse;
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
public class GitHubCertainIssueLinkValidator extends AbstractGitHubLinkValidator {
    // https://github.com/{owner}/{repo}/issues/{issue_number}
    private static final Pattern GITHUB_CERTAIN_ISSUE_PATTERN = compilePattern(
        "^https?://(?:www\\.)?github\\.com/{owner}/{repo}/issues/{issue_number}/?$", OWNER, REPO, ISSUE_NUMBER
    );

    @Autowired
    public GitHubCertainIssueLinkValidator(GithubClient githubClient) {
        super(githubClient);
    }

    @Override
    public Matcher match(String rawLink) {
        return GITHUB_CERTAIN_ISSUE_PATTERN.matcher(rawLink);
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Map<ValidatorKey, String> getArguments(Matcher matcher) {
        return Map.of(
            OWNER, matcher.group(1),
            REPO, matcher.group(2),
            ISSUE_NUMBER, matcher.group(3)
        );
    }

    @Override
    protected Optional<String> getUpdateDescription(Map<ValidatorKey, String> arguments, OffsetDateTime updatesFrom) {
        final List<IssueCommentResponse> response = githubClient
            .getListIssueComments(arguments.get(OWNER), arguments.get(REPO), arguments.get(ISSUE_NUMBER))
            .stream()
            .filter(resp -> resp.getCreatedAt().isAfter(updatesFrom))
            .toList();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        final StringBuilder builder = new StringBuilder("New comments in issue:\n");
        for (int i = 0; i < response.size(); i++) {
            final IssueCommentResponse comment = response.get(i);
            processResponse(builder, i + 1,
                Map.entry("Commentator", comment.getUser()),
                Map.entry("Created at", comment.getCreatedAt()),
                Map.entry("Text", "\n{" + comment.getBody() + "\n}")
            );
        }
        return Optional.of(builder.toString());
    }
}
