package edu.java.scrapper.client;

import edu.java.dto.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface ExternalServiceClient {
    static Mono<? extends ServiceException> clientError(ClientResponse clientResponse) {
        return Mono.error(new ServiceException(
            "Unexpected Client error: " + clientResponse.toString(),
            HttpStatus.valueOf(clientResponse.statusCode().value())
        ));
    }

    static Mono<? extends ServiceException> serverError(ClientResponse clientResponse) {
        return Mono.error(new ServiceException(
            "Unexpected Server error: " + clientResponse.toString(),
            HttpStatus.valueOf(clientResponse.statusCode().value())
        ));
    }
}
