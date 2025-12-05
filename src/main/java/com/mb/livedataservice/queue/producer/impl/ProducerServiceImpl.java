package com.mb.livedataservice.queue.producer.impl;

import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.queue.producer.ProducerService;
import com.mb.livedataservice.util.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<@NonNull String, @NonNull Object> kafkaTemplate;

    public void publishMessage(String message) {
        kafkaTemplate.send(KafkaTopics.TEST_TOPIC, message);
        kafkaTemplate.send(KafkaTopics.CUSTOM_ORDERS, new Order(UUID.randomUUID(), UUID.randomUUID(), 100));
        kafkaTemplate.send(KafkaTopics.ORDERS, new Order(UUID.randomUUID(), UUID.randomUUID(), 500));
    }
}
