package edu.java.bot.client;

import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.ApiErrorResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public class ScrapperHttpClientImpl implements ScrapperHttpClient {
    private static final String BASE_SCRAPPER_URI = "https://localhost:8080";
    private static final String TG_CHAT_ID_URI = "/tg-chat/{id}";
    private static final String LINKS_URI = "/links";
    private static final String CHAT_ID_HEADER = "tgChatId";
    private final WebClient scrapperWebClient;

    public ScrapperHttpClientImpl(WebClient.Builder webClientBuilder, String baseUrl) {
        scrapperWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public ScrapperHttpClientImpl(WebClient.Builder webClientBuilder) {
        this(webClientBuilder, BASE_SCRAPPER_URI);
    }

    private static Mono<? extends Throwable> badRequestFunction(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> new WrongParametersException(x.getExceptionMessage()));
    }

    private static Mono<? extends Throwable> notFoundFunction(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> new NonExistentChatException(x.getExceptionMessage()));
    }

    @Override
    public void registerChat(long id) {
        scrapperWebClient.post()
            .uri(TG_CHAT_ID_URI, id)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFoundFunction)
            .bodyToMono(Void.class)
            .block();
    }

    @Override
    public void deleteChat(long id) {
        scrapperWebClient.delete()
            .uri(TG_CHAT_ID_URI, id)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFoundFunction)
            .bodyToMono(Void.class)
            .block();
    }

    @Override
    public ListUserLinkResponse getAllLinks(long id) {
        return scrapperWebClient.get()
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFoundFunction)
            .bodyToMono(ListUserLinkResponse.class)
            .block();
    }

    @Override
    public UserLinkResponse addLinkToTracking(long id, String uri, String alias) {
        return scrapperWebClient.post()
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .body(Mono.just(new AddLinkRequest(uri, alias)), AddLinkRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFoundFunction)
            .bodyToMono(UserLinkResponse.class)
            .block();
    }

    private static Mono<? extends Throwable> specialNotFoundFunction(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> {
                if (Objects.equals(x.getExceptionName(), "LinkNotFoundException")) {
                    return new LinkNotFoundException(x.getExceptionMessage());
                }
                return new NonExistentChatException(x.getExceptionMessage());
            });
    }

    @Override
    public UserLinkResponse removeLinkFromTracking(long id, String alias) {
        return scrapperWebClient.method(HttpMethod.DELETE)
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .body(Mono.just(new RemoveLinkRequest(alias)), RemoveLinkRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::specialNotFoundFunction)
            .bodyToMono(UserLinkResponse.class)
            .block();
    }
}
