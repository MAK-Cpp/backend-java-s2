package edu.java.configuration;

import edu.java.client.GithubClient;
import edu.java.client.GithubClientImpl;
import edu.java.client.StackOverflowClient;
import edu.java.client.StackOverflowClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {
    @Bean
    public GithubClient githubClient() {
        return new GithubClientImpl();
    }

    @Bean
    public StackOverflowClient stackOverflowClient() {
        return new StackOverflowClientImpl();
    }
}
