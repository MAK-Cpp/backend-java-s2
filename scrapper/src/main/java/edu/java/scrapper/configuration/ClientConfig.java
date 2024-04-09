package edu.java.scrapper.configuration;

import edu.java.scrapper.client.bot.BotHttpClient;
import edu.java.scrapper.client.bot.BotHttpClientImpl;
import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.client.github.GithubClientImpl;
import edu.java.scrapper.client.stackoverflow.StackOverflowClient;
import edu.java.scrapper.client.stackoverflow.StackOverflowClientImpl;
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
    public BotHttpClient botHttpClient(WebClient.Builder webClientBuilder) {
        return new BotHttpClientImpl(webClientBuilder);
    }
}
