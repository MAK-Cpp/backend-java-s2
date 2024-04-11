package edu.java.scrapper.client.bot;

import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.dto.response.ApiErrorResponse;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class BotHttpClientImpl implements BotHttpClient {
    private static final String BASE_BOT_URI = "http://localhost:8090";
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
    public final void sendUpdates(
        Long id,
        String url,
        String description,
        List<Map.Entry<Long, String>> chatsAndAliases
    ) {
        botWebClient.post()
            .uri("/updates")
            .body(Mono.just(new LinkUpdateRequest(
                id, url, description,
                chatsAndAliases.stream()
                    .map(x -> new LinkUpdateRequest.ChatAndAlias(x.getKey(), x.getValue()))
                    .toArray(LinkUpdateRequest.ChatAndAlias[]::new)
            )), LinkUpdateRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, BotHttpClientImpl::badRequestFunction)
            .bodyToMono(String.class)
            .block();
    }
}
