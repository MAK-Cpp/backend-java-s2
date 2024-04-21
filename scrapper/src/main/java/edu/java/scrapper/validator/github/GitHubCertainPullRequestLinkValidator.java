package edu.java.scrapper.validator.github;

import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.response.github.CommitResponse;
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
public class GitHubCertainPullRequestLinkValidator extends AbstractGitHubLinkValidator {
    // https://github.com/{owner}/{repo}/pull/{pull_number}
    private static final Pattern GITHUB_CERTAIN_PULL_REQUEST_PATTERN = compilePattern(
        "^https?://(?:www\\.)?github\\.com/{owner}/{repo}/pull/{pull_number}/?$", OWNER, REPO, PULL_NUMBER
    );

    @Autowired
    public GitHubCertainPullRequestLinkValidator(GithubClient githubClient) {
        super(githubClient);
    }

    @Override
    public Matcher match(String rawLink) {
        return GITHUB_CERTAIN_PULL_REQUEST_PATTERN.matcher(rawLink);
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Map<ValidatorKey, String> getArguments(Matcher matcher) {
        return Map.of(
            OWNER, matcher.group(1),
            REPO, matcher.group(2),
            PULL_NUMBER, matcher.group(3)
        );
    }

    @Override
    protected Optional<String> getUpdateDescription(Map<ValidatorKey, String> arguments, OffsetDateTime updatesFrom) {
        List<CommitResponse.Commit> response = githubClient
            .getListCommitsOnPullRequest(arguments.get(OWNER), arguments.get(REPO), arguments.get(PULL_NUMBER))
            .stream()
            .map(CommitResponse::getCommit)
            .filter(resp -> resp.getCommitter().getDate().isAfter(updatesFrom))
            .toList();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        final StringBuilder builder = new StringBuilder("New commits in pull request:\n");
        for (int i = 0; i < response.size(); i++) {
            final CommitResponse.Commit commit = response.get(i);
            final CommitResponse.Author author = commit.getAuthor();
            processResponse(builder, i + 1,
                Map.entry("Author", author),
                Map.entry("Commiter", commit.getCommitter()),
                Map.entry("Date", author.getDate()),
                Map.entry("Message", "\n{" + commit.getMessage() + "\n}")
            );
        }
        return Optional.of(builder.toString());
    }
}
