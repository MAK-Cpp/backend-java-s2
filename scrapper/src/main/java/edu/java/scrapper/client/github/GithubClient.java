package edu.java.scrapper.client.github;

import edu.java.scrapper.client.ExternalServiceClient;
import edu.java.scrapper.response.github.CommitResponse;
import edu.java.scrapper.response.github.IssueCommentResponse;
import edu.java.scrapper.response.github.IssueResponse;
import edu.java.scrapper.response.github.PullRequestResponse;
import java.util.List;

public interface GithubClient extends ExternalServiceClient {
    List<PullRequestResponse> getPullRequests(String owner, String repo);

    List<IssueResponse> getIssues(String owner, String repo);

    List<CommitResponse> getListCommitsOnPullRequest(String owner, String repo, String pullNumber);

    List<IssueCommentResponse> getListIssueComments(String owner, String repo, String issueNumber);
}
