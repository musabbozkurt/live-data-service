package com.mb.livedataservice.config.kafka;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.kafka.dlt")
public class KafkaDeadLetterTopicProperties {

    @Valid
    @Getter
    @Setter
    @NotNull
    private DeadLetter deadletter;

    @Valid
    @Getter
    @Setter
    @NotNull
    private Backoff backoff;
}

record DeadLetter(@NotNull Duration retention,
                  @Nullable String suffix) {
}

record Backoff(@NotNull Duration initialInterval,
               @NotNull Duration maxInterval,
               @Positive int maxRetries,
               @Positive double multiplier) {
}
