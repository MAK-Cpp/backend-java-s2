package edu.java.bot.configuration;

import edu.java.bot.client.ScrapperHttpClient;
import edu.java.bot.client.ScrapperHttpClientImpl;
import edu.java.configuration.HttpClientConfig;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "client", ignoreUnknownFields = false)
public record ClientConfig(
    @NotNull HttpClientConfig scrapper
) {
    private static final String UNSET = "unset";

    @Bean
    public ScrapperHttpClient scrapperHttpClient(WebClient.Builder webClientBuilder) {
        if (Objects.equals(scrapper.url(), UNSET)) {
            return new ScrapperHttpClientImpl(webClientBuilder, scrapper.retry());
        }
        return new ScrapperHttpClientImpl(webClientBuilder, scrapper.url(), scrapper.retry());
    }
}
