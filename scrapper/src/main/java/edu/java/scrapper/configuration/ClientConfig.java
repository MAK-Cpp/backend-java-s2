package edu.java.scrapper.configuration;

import edu.java.configuration.HttpClientConfig;
import edu.java.scrapper.client.bot.BotHttpClient;
import edu.java.scrapper.client.bot.BotHttpClientImpl;
import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.client.github.GithubClientImpl;
import edu.java.scrapper.client.stackoverflow.StackOverflowClient;
import edu.java.scrapper.client.stackoverflow.StackOverflowClientImpl;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(
    prefix = "client",
    ignoreUnknownFields = false
)
public record ClientConfig(
    @NotNull HttpClientConfig github,
    @NotNull HttpClientConfig stackOverflow,
    @NotNull HttpClientConfig bot
) {
    private static final String UNSET = "unset";

    @Bean
    public GithubClient githubClient(WebClient.Builder webClientBuilder) {
        if (Objects.equals(github.url(), UNSET)) {
            return new GithubClientImpl(webClientBuilder, github.retry());
        }
        return new GithubClientImpl(webClientBuilder, github.url(), github.retry());
    }

    @Bean
    public StackOverflowClient stackOverflowClient(WebClient.Builder webClientBuilder) {
        if (Objects.equals(stackOverflow.url(), UNSET)) {
            return new StackOverflowClientImpl(webClientBuilder, stackOverflow.retry());
        }
        return new StackOverflowClientImpl(webClientBuilder, stackOverflow.url(), stackOverflow.retry());
    }

    @Bean
    public BotHttpClient botHttpClient(WebClient.Builder webClientBuilder) {
        if (Objects.equals(bot.url(), UNSET)) {
            return new BotHttpClientImpl(webClientBuilder, bot.retry());
        }
        return new BotHttpClientImpl(webClientBuilder, bot.url(), bot.retry());
    }
}
