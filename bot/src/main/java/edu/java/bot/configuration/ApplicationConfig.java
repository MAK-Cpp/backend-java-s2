package edu.java.bot.configuration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull KafkaProperties kafka,
    @NotEmpty
    String telegramToken
) {
    public record KafkaProperties(
        @NotNull Boolean enable,
        @NotEmpty String groupId,
        @NotEmpty String topicName,
        @NotNull Integer partitions,
        @NotNull Integer replicationFactor
    ) {
    }
}
