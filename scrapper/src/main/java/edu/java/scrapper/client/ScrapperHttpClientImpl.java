package edu.java.scrapper.client;

import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.ApiErrorResponse;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.exception.LinkNotFoundException;
import edu.java.exception.NonExistentChatException;
import edu.java.exception.WrongParametersException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapperHttpClientImpl.class);

    public ScrapperHttpClientImpl(String baseUrl) {
        scrapperWebClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public ScrapperHttpClientImpl() {
        this(BASE_SCRAPPER_URI);
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
    public ListLinkResponse getAllLinks(long id) {
        return scrapperWebClient.get()
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFoundFunction)
            .bodyToMono(ListLinkResponse.class)
            .block();
    }

    @Override
    public LinkResponse addLinkToTracking(long id, String uri) {
        return scrapperWebClient.post()
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .body(Mono.just(new AddLinkRequest(uri)), AddLinkRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFoundFunction)
            .bodyToMono(LinkResponse.class)
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
    public LinkResponse removeLinkFromTracking(long id, String uri) {
        return scrapperWebClient.method(HttpMethod.DELETE)
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .body(Mono.just(new RemoveLinkRequest(uri)), RemoveLinkRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequestFunction)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::specialNotFoundFunction)
            .bodyToMono(LinkResponse.class)
            .block();
    }
}