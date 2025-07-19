package com.mb.livedataservice.queue.consumer;

import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.util.KafkaTopics;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConsumerFactory<String, Object> consumerFactory;

    @PostConstruct
    public void init() {
        Map<String, Object> configurationProperties = new HashMap<>(consumerFactory.getConfigurationProperties());
        configurationProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerFactory.updateConfigs(configurationProperties);
        kafkaTemplate.setConsumerFactory(consumerFactory);
    }

    /// Disabled @RetryableTopic( attempts = "5",
    ///            backoff = @Backoff(delay = 3000),
    ///            include = RuntimeException.class
    ///    )
    @KafkaListener(topics = KafkaTopics.ORDERS)
    void listen(@Payload @Validated Order order) {
        log.info("Received: {}", order);
    }

    /// Disabled @RetryableTopic( attempts = "5",
    ///            backoff = @Backoff(delay = 3000),
    ///            include = RuntimeException.class
    ///    )
    @KafkaListener(topics = KafkaTopics.CUSTOM_ORDERS)
    void listenCustomOrders(@Payload @Validated Order order) {
        log.info("Received custom order: {}", order);
        throw new BaseException(LiveDataErrorCode.UNEXPECTED_ERROR);
    }

    /// Consume messages from the Dead Letter Topic (DLT).
    ///
    /// This method is invoked when a message fails to process after the specified number of retries.
    ///
    /// It logs the message and its metadata, and attempts to receive the original consumer record.
    ///
    /// KafkaTemplate is set to use the consumerFactory to ensure it can consume from the DLT manually.
    ///
    /// @DltHandler is disabled to allow manual handling of DLT messages.
    public void listenDLT(Order message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset) {
        log.error("DLT message consumed from topic: {}, offset: {}, message: {}", topic, offset, message);
        kafkaTemplate.setConsumerFactory(consumerFactory);

        ConsumerRecord<?, ?> consumerRecord = kafkaTemplate.receive(topic, 0, offset);
        if (consumerRecord != null) {
            log.error("DLT consumer record: {}", consumerRecord);
        } else {
            log.error("No consumer record found for topic: {}, offset: {}", topic, offset);
        }
    }
}
