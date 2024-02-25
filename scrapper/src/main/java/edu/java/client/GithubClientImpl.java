package edu.java.client;

import edu.java.response.CommitResponse;
import edu.java.response.IssueResponse;
import edu.java.response.PullRequestResponse;
import java.util.List;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class GithubClientImpl implements GithubClient {
    private static final String BASE_GITHUB_API_URL = "https://api.github.com";
    private final WebClient githubWebClient;

    public GithubClientImpl(String baseUrl) {
        githubWebClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public GithubClientImpl() {
        this(BASE_GITHUB_API_URL);
    }

    @Override
    public Mono<List<CommitResponse>> getCommits(String owner, String repo) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/commits", owner, repo)
            .retrieve()
            .bodyToFlux(CommitResponse.class)
            .collectList();
    }

    @Override
    public Mono<List<PullRequestResponse>> getPullRequests(String owner, String repo) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/pulls", owner, repo)
            .retrieve()
            .bodyToFlux(PullRequestResponse.class)
            .collectList();
    }

    @Override
    public Mono<List<IssueResponse>> getIssues(String owner, String repo) {
        return githubWebClient.get()
            .uri("/repos/{owner}/{repo}/issues", owner, repo)
            .retrieve()
            .bodyToFlux(IssueResponse.class)
            .collectList();
    }
}
