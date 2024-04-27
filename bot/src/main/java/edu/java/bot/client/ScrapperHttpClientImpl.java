package edu.java.bot.client;

import edu.java.dto.exception.APIException;
import edu.java.dto.exception.AliasAlreadyTakenException;
import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.LinkAlreadyTrackedException;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.ServiceException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.ApiErrorResponse;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class ScrapperHttpClientImpl implements ScrapperHttpClient {
    private static final String BASE_SCRAPPER_URI = "http://localhost:8080";
    private static final String TG_CHAT_ID_URI = "/tg-chat/{id}";
    private static final String LINKS_URI = "/links";
    private static final String LINK_URI = "/link";
    private static final String CHAT_ID_HEADER = "tgChatId";
    private static final String ALIAS_HEADER = "alias";
    private final WebClient scrapperWebClient;
    private final Retry retry;

    public ScrapperHttpClientImpl(WebClient.Builder webClientBuilder, String baseUrl, Retry retry) {
        this.retry = retry;
        scrapperWebClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public ScrapperHttpClientImpl(WebClient.Builder webClientBuilder, Retry retry) {
        this(webClientBuilder, BASE_SCRAPPER_URI, retry);
    }

    private static Mono<APIException> badRequest(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> new APIException(
                HttpStatus.valueOf(clientResponse.statusCode().value()),
                new WrongParametersException(x.getExceptionMessage())
            ));
    }

    private static Mono<APIException> notFound(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> new APIException(
                HttpStatus.valueOf(clientResponse.statusCode().value()),
                new NonExistentChatException(x.getExceptionMessage())
            ));
    }

    static Mono<ServiceException> clientError(ClientResponse clientResponse) {
        return Mono.error(new ServiceException(
            "Unexpected Client error: " + clientResponse.toString(),
            HttpStatus.valueOf(clientResponse.statusCode().value())
        ));
    }

    static Mono<ServiceException> serverError(ClientResponse clientResponse) {
        return Mono.error(new ServiceException(
            "Unexpected Server error: " + clientResponse.toString(),
            HttpStatus.valueOf(clientResponse.statusCode().value())
        ));
    }

    @Override
    public void registerChat(long id) {
        scrapperWebClient.post()
            .uri(TG_CHAT_ID_URI, id)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequest)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFound)
            .onStatus(HttpStatusCode::is4xxClientError, ScrapperHttpClientImpl::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ScrapperHttpClientImpl::serverError)
            .bodyToMono(Void.class)
            .retryWhen(retry)
            .block();
    }

    @Override
    public void deleteChat(long id) {
        scrapperWebClient.delete()
            .uri(TG_CHAT_ID_URI, id)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequest)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFound)
            .onStatus(HttpStatusCode::is4xxClientError, ScrapperHttpClientImpl::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ScrapperHttpClientImpl::serverError)
            .bodyToMono(Void.class)
            .retryWhen(retry)
            .block();
    }

    @Override
    public ChatResponse getChat(long id) throws DTOException {
        return scrapperWebClient.get()
            .uri(TG_CHAT_ID_URI, id)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequest)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFound)
            .onStatus(HttpStatusCode::is4xxClientError, ScrapperHttpClientImpl::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ScrapperHttpClientImpl::serverError)
            .bodyToMono(ChatResponse.class)
            .retryWhen(retry)
            .block();
    }

    @Override
    public ListUserLinkResponse getAllLinks(long id) {
        return scrapperWebClient.get()
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequest)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFound)
            .onStatus(HttpStatusCode::is4xxClientError, ScrapperHttpClientImpl::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ScrapperHttpClientImpl::serverError)
            .bodyToMono(ListUserLinkResponse.class)
            .retryWhen(retry)
            .block();
    }

    @Override
    public UserLinkResponse getLinkByChatIdAndAlias(long chatId, String alias) throws DTOException {
        return scrapperWebClient.get()
            .uri(LINK_URI)
            .header(CHAT_ID_HEADER, String.valueOf(chatId))
            .header(ALIAS_HEADER, alias)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequest)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFound)
            .onStatus(HttpStatusCode::is4xxClientError, ScrapperHttpClientImpl::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ScrapperHttpClientImpl::serverError)
            .bodyToMono(UserLinkResponse.class)
            .retryWhen(retry)
            .block();
    }

    private static Mono<APIException> addLinkToTrackingBadRequest(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class).map(apiErrorResponse -> {
            final Throwable cause;
            if (Objects.equals(apiErrorResponse.getExceptionName(), "LinkAlreadyTrackedException")) {
                cause = new LinkAlreadyTrackedException("Link already tracked");
            } else if (Objects.equals(apiErrorResponse.getExceptionName(), "AliasAlreadyTakenException")) {
                cause = new AliasAlreadyTakenException("Alias already taken, please choose another alias");
            } else {
                cause = new WrongParametersException(apiErrorResponse.getExceptionMessage());
            }
            return new APIException(
                HttpStatus.valueOf(clientResponse.statusCode().value()),
                cause
            );
        });
    }

    @Override
    public UserLinkResponse addLinkToTracking(long id, String uri, String alias) {
        return scrapperWebClient.post()
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .body(Mono.just(new AddLinkRequest(uri, alias)), AddLinkRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::addLinkToTrackingBadRequest)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::notFound)
            .onStatus(HttpStatusCode::is4xxClientError, ScrapperHttpClientImpl::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ScrapperHttpClientImpl::serverError)
            .bodyToMono(UserLinkResponse.class)
            .retryWhen(retry)
            .block();
    }

    private static Mono<APIException> removeLinkFromTrackingNotFound(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ApiErrorResponse.class)
            .map(x -> {
                final Throwable cause;
                if (Objects.equals(x.getExceptionName(), "LinkNotFoundException")) {
                    cause = new LinkNotFoundException(x.getExceptionMessage());
                } else {
                    cause = new NonExistentChatException(x.getExceptionMessage());
                }
                return new APIException(
                    HttpStatus.valueOf(clientResponse.statusCode().value()),
                    cause
                );
            });
    }

    @Override
    public UserLinkResponse removeLinkFromTracking(long id, String alias) {
        return scrapperWebClient.method(HttpMethod.DELETE)
            .uri(LINKS_URI)
            .header(CHAT_ID_HEADER, String.valueOf(id))
            .body(Mono.just(new RemoveLinkRequest(alias)), RemoveLinkRequest.class)
            .retrieve()
            .onStatus(HttpStatus.BAD_REQUEST::equals, ScrapperHttpClientImpl::badRequest)
            .onStatus(HttpStatus.NOT_FOUND::equals, ScrapperHttpClientImpl::removeLinkFromTrackingNotFound)
            .onStatus(HttpStatusCode::is4xxClientError, ScrapperHttpClientImpl::clientError)
            .onStatus(HttpStatusCode::is5xxServerError, ScrapperHttpClientImpl::serverError)
            .bodyToMono(UserLinkResponse.class)
            .retryWhen(retry)
            .block();
    }
}
