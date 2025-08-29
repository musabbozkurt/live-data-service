package com.mb.livedataservice.queue.consumer;

import com.mb.livedataservice.queue.dto.consumer.Order;
import com.mb.livedataservice.util.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
public class OrderListener {

    @KafkaListener(topics = KafkaTopics.ORDERS)
    void listen(@Payload @Validated Order order) {
        log.info("Received an order message: {}", order);
    }
}
