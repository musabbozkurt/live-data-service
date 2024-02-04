package com.mb.livedataservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka.kafka-topics")
public class KafkaTopics {

    private String testTopic;
}
