package edu.java.scrapper.client.github;

import edu.java.scrapper.client.ExternalServiceClient;
import edu.java.scrapper.response.github.CommitResponse;
import edu.java.scrapper.response.github.IssueCommentResponse;
import edu.java.scrapper.response.github.IssueResponse;
import edu.java.scrapper.response.github.PullRequestResponse;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

public class GithubClientImpl implements GithubClient {
    private static final String BASE_GITHUB_API_URL = "https://api.github.com";
    private final WebClient githubWebClient;
    private final Retry retryBackoffSpec;

    public GithubClientImpl(WebClient.Builder webClientBuilder, String baseUrl, Retry retryBackoffSpec) {
        this.retryBackoffSpec = retryBackoffSpec;
        githubWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public GithubClientImpl(WebClient.Builder webClientBuilder, Retry retryBackoffSpec) {
        this(webClientBuilder, BASE_GITHUB_API_URL, retryBackoffSpec);
    }

    @Override
    public List<PullRequestResponse> getPullRequests(String owner, String repo) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/pulls", owner, repo)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, ExternalServiceClient::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ExternalServiceClient::serverError)
            .bodyToFlux(PullRequestResponse.class)
            .retryWhen(retryBackoffSpec)
            .collectList()
            .block();
    }

    @Override
    public List<IssueResponse> getIssues(String owner, String repo) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/issues", owner, repo)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, ExternalServiceClient::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ExternalServiceClient::serverError)
            .bodyToFlux(IssueResponse.class)
            .retryWhen(retryBackoffSpec)
            .collectList()
            .block();
    }

    @Override
    public List<CommitResponse> getListCommitsOnPullRequest(String owner, String repo, String pullNumber) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/pulls/{pull_number}/commits", owner, repo, pullNumber)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, ExternalServiceClient::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ExternalServiceClient::serverError)
            .bodyToFlux(CommitResponse.class)
            .retryWhen(retryBackoffSpec)
            .collectList()
            .block();
    }

    @Override
    public List<IssueCommentResponse> getListIssueComments(String owner, String repo, String issueNumber) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/issues/{issue_number}/comments", owner, repo, issueNumber)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, ExternalServiceClient::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ExternalServiceClient::serverError)
            .bodyToFlux(IssueCommentResponse.class)
            .retryWhen(retryBackoffSpec)
            .collectList()
            .block();
    }
}
