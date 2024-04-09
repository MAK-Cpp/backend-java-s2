package edu.java.scrapper.client;

import edu.java.scrapper.response.github.CommitResponse;
import edu.java.scrapper.response.github.IssueCommentResponse;
import edu.java.scrapper.response.github.IssueResponse;
import edu.java.scrapper.response.github.PullRequestResponse;
import java.util.List;
import reactor.core.publisher.Mono;

public interface GithubClient {
    Mono<List<PullRequestResponse>> getPullRequests(String owner, String repo);

    Mono<List<IssueResponse>> getIssues(String owner, String repo);

    Mono<List<CommitResponse>> getListCommitsOnPullRequest(String owner, String repo, String pullNumber);

    Mono<List<IssueCommentResponse>> getListIssueComments(String owner, String repo, String issueNumber);
}
