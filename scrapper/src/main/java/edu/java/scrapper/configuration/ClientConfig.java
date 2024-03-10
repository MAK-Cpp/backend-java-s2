package edu.java.scrapper.configuration;

import edu.java.scrapper.client.BotHttpClient;
import edu.java.scrapper.client.BotHttpClientImpl;
import edu.java.scrapper.client.GithubClient;
import edu.java.scrapper.client.GithubClientImpl;
import edu.java.scrapper.client.ScrapperHttpClient;
import edu.java.scrapper.client.ScrapperHttpClientImpl;
import edu.java.scrapper.client.StackOverflowClient;
import edu.java.scrapper.client.StackOverflowClientImpl;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;


@Validated
@ConfigurationProperties(prefix = "client", ignoreUnknownFields = false)
public record ClientConfig(
    @NotEmpty
    String githubUrl,
    @NotEmpty
    String stackOverflowUrl
) {
    private static final String UNSET = "unset";

    @Bean
    public GithubClient githubClient(WebClient.Builder webClientBuilder) {
        if (Objects.equals(githubUrl, UNSET)) {
            return new GithubClientImpl(webClientBuilder);
        }
        return new GithubClientImpl(webClientBuilder, githubUrl);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(WebClient.Builder webClientBuilder) {
        if (Objects.equals(stackOverflowUrl, UNSET)) {
            return new StackOverflowClientImpl(webClientBuilder);
        }
        return new StackOverflowClientImpl(webClientBuilder, stackOverflowUrl);
    }


    @Bean
    public ScrapperHttpClient scrapperHttpClient() {
        return new ScrapperHttpClientImpl();
    }

    @Bean
    public BotHttpClient botHttpClient() {
        return new BotHttpClientImpl();
    }
}
