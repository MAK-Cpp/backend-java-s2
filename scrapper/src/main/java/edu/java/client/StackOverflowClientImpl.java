package edu.java.client;

import edu.java.response.AnswerResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class StackOverflowClientImpl implements StackOverflowClient {
    private static final String BASE_STACK_OVERFLOW_API_URL = "https://api.stackexchange.com/2.3";
    private static final String FILTER = "!nNPvSNe7Gv";
    private static final String SITE = "stackoverflow.com";
    private static final String ORDER = "desc";
    private static final String SORT = "creation";
    private final WebClient githubWebClient;

    public StackOverflowClientImpl(String baseUrl) {
        githubWebClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    public StackOverflowClientImpl() {
        this(BASE_STACK_OVERFLOW_API_URL);
    }

    @Override
    public Mono<AnswerResponse> getQuestionAnswers(int questionId) {
        return githubWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/questions/{id}/answers")
                .queryParam("order", ORDER)
                .queryParam("sort", SORT)
                .queryParam("site", SITE)
                .queryParam("filter", FILTER)
                .build(questionId))
            .retrieve()
            .bodyToMono(AnswerResponse.class);
    }
}
