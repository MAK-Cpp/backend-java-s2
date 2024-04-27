package edu.java.scrapper.client.stackoverflow;

import edu.java.scrapper.client.ExternalServiceClient;
import edu.java.scrapper.response.stackoverflow.AnswerResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

public class StackOverflowClientImpl implements StackOverflowClient {
    public static final String BASE_STACK_OVERFLOW_API_URL = "https://api.stackexchange.com/2.3";
    public static final String FILTER = "!nNPvSNe7Gv";
    public static final String SITE = "stackoverflow.com";
    public static final String ORDER = "desc";
    public static final String SORT = "creation";
    private final WebClient githubWebClient;
    private final Retry retryBackoffSpec;

    public StackOverflowClientImpl(WebClient.Builder webClientBuilder, String baseUrl,
        Retry retryBackoffSpec
    ) {
        this.retryBackoffSpec = retryBackoffSpec;
        githubWebClient = webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }

    public StackOverflowClientImpl(WebClient.Builder webClientBuilder, Retry retryBackoffSpec) {
        this(webClientBuilder, BASE_STACK_OVERFLOW_API_URL, retryBackoffSpec);
    }

    @Override
    public AnswerResponse getQuestionAnswers(String id) {
        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/questions/{id}/answers")
                .queryParam("filter", FILTER)
                .queryParam("order", ORDER)
                .queryParam("site", SITE)
                .queryParam("sort", SORT)
                .build(id))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, ExternalServiceClient::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ExternalServiceClient::serverError)
            .bodyToMono(AnswerResponse.class)
            .retryWhen(retryBackoffSpec)
            .block();
    }
}
