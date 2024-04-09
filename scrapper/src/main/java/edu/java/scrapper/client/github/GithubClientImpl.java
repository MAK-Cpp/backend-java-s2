package edu.java.scrapper.client.github;

import edu.java.scrapper.response.github.CommitResponse;
import edu.java.scrapper.response.github.IssueCommentResponse;
import edu.java.scrapper.response.github.IssueResponse;
import edu.java.scrapper.response.github.PullRequestResponse;
import java.util.List;
import org.springframework.web.reactive.function.client.WebClient;

public class GithubClientImpl implements GithubClient {
    private static final String BASE_GITHUB_API_URL = "https://api.github.com";
    private final WebClient githubWebClient;

    public GithubClientImpl(WebClient.Builder webClientBuilder, String baseUrl) {
        githubWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public GithubClientImpl(WebClient.Builder webClientBuilder) {
        this(webClientBuilder, BASE_GITHUB_API_URL);
    }

    @Override
    public List<PullRequestResponse> getPullRequests(String owner, String repo) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/pulls", owner, repo)
            .retrieve()
            .bodyToFlux(PullRequestResponse.class)
            .collectList()
            .block();
    }

    @Override
    public List<IssueResponse> getIssues(String owner, String repo) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/issues", owner, repo)
            .retrieve()
            .bodyToFlux(IssueResponse.class)
            .collectList()
            .block();
    }

    @Override
    public List<CommitResponse> getListCommitsOnPullRequest(String owner, String repo, String pullNumber) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/pulls/{pull_number}/commits", owner, repo, pullNumber)
            .retrieve()
            .bodyToFlux(CommitResponse.class)
            .collectList()
            .block();
    }

    @Override
    public List<IssueCommentResponse> getListIssueComments(String owner, String repo, String issueNumber) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/issues/{issue_number}/comments", owner, repo, issueNumber)
            .retrieve()
            .bodyToFlux(IssueCommentResponse.class)
            .collectList()
            .block();
    }
}
