package edu.java.scrapper.client.bot;

import edu.java.dto.exception.ServiceException;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.dto.response.ApiErrorResponse;
import edu.java.scrapper.client.ExternalServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class BotHttpClientImpl implements BotHttpClient {
    private static final String BASE_BOT_URI = "http://localhost:8090";
    private final WebClient botWebClient;
    private final Retry retryBackoffSpec;

    public BotHttpClientImpl(WebClient.Builder webClientBuilder, String baseUrl, Retry retryBackoffSpec) {
        this.retryBackoffSpec = retryBackoffSpec;
        botWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public BotHttpClientImpl(WebClient.Builder webClientBuilder, Retry retry) {
        this(webClientBuilder, BASE_BOT_URI, retry);
    }

    private static Mono<? extends ServiceException> apiError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> new ServiceException(
                x.getExceptionMessage(),
                HttpStatus.valueOf(clientResponse.statusCode().value())
            ));
    }

    @Override
    public final void sendUpdates(LinkUpdateRequest request) {
        botWebClient.post()
            .uri("/updates")
            .body(Mono.just(request), LinkUpdateRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, BotHttpClientImpl::apiError)
            .onStatus(HttpStatusCode::is4xxClientError, ExternalServiceClient::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ExternalServiceClient::serverError)
            .bodyToMono(String.class)
            .retryWhen(retryBackoffSpec)
            .block();
    }
}
