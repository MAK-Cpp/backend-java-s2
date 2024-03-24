package edu.java.bot.configuration;

import edu.java.bot.client.ScrapperHttpClient;
import edu.java.bot.client.ScrapperHttpClientImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "client", ignoreUnknownFields = false)
public record ClientConfig() {
    @Bean
    public ScrapperHttpClient scrapperHttpClient(WebClient.Builder webClientBuilder) {
        return new ScrapperHttpClientImpl(webClientBuilder);
    }
}
