package edu.java.scrapper.configuration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull KafkaProperties kafka,
    @NotNull AccessType databaseAccessType,
    @NotNull Scheduler scheduler
) {
    public record KafkaProperties(
        @NotNull Boolean enable,
        @NotEmpty String topicName,
        @NotNull Integer partitions,
        @NotNull Integer replicationFactor
    ) {
    }

    public enum AccessType {
        JDBC, JOOQ, JPA
    }

    public record Scheduler(boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay) {
    }
}
