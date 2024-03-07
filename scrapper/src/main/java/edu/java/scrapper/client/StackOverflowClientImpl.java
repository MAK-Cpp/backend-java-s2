package edu.java.scrapper.client;

import edu.java.scrapper.response.AnswerResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class StackOverflowClientImpl implements StackOverflowClient {
    public static final String BASE_STACK_OVERFLOW_API_URL = "https://api.stackexchange.com/2.3";
    public static final String FILTER = "!nNPvSNe7Gv";
    public static final String SITE = "stackoverflow.com";
    public static final String ORDER = "desc";
    public static final String SORT = "creation";
    private final WebClient githubWebClient;

    public StackOverflowClientImpl(WebClient.Builder webClientBuilder, String baseUrl) {
        githubWebClient = webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }

    public StackOverflowClientImpl(WebClient.Builder webClientBuilder) {
        this(webClientBuilder, BASE_STACK_OVERFLOW_API_URL);
    }

    @Override
    public Mono<AnswerResponse> getQuestionAnswers(int questionId) {
        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/questions/{id}/answers")
                .queryParam("filter", FILTER)
                .queryParam("order", ORDER)
                .queryParam("site", SITE)
                .queryParam("sort", SORT)
                .build(questionId))
            .retrieve()
            .bodyToMono(AnswerResponse.class);
    }
}
