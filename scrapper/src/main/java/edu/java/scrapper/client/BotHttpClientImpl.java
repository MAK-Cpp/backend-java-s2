package edu.java.scrapper.client;

import edu.java.dto.request.LinkUpdateRequest;
import edu.java.dto.response.ApiErrorResponse;
import edu.java.exception.WrongParametersException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class BotHttpClientImpl implements BotHttpClient {
    private static final String BASE_BOT_URI = "https://localhost:8080";
    private final WebClient botWebClient;

    public BotHttpClientImpl(WebClient.Builder webClientBuilder, String baseUrl) {
        botWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public BotHttpClientImpl(WebClient.Builder webClientBuilder) {
        this(webClientBuilder, BASE_BOT_URI);
    }

    private static Mono<? extends Throwable> badRequestFunction(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> new WrongParametersException(x.getExceptionMessage()));
    }

    @Override
    public void sendUpdates(long id, String url, String description, long... tgChatIds) {
        botWebClient.post()
            .uri("/updates")
            .body(Mono.just(new LinkUpdateRequest(id, url, description, tgChatIds)), LinkUpdateRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, BotHttpClientImpl::badRequestFunction)
            .bodyToMono(Void.class)
            .block();
    }
}
