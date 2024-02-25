package edu.java.client;

import edu.java.response.CommitResponse;
import edu.java.response.IssueResponse;
import edu.java.response.PullRequestResponse;
import java.util.List;
import reactor.core.publisher.Mono;

public interface GithubClient {
    Mono<List<CommitResponse>> getCommits(String owner, String repo);

    Mono<List<PullRequestResponse>> getPullRequests(String owner, String repo);

    Mono<List<IssueResponse>> getIssues(String owner, String repo);
}
