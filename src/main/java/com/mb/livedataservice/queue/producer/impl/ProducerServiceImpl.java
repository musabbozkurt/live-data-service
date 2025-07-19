package com.mb.livedataservice.queue.producer.impl;

import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.queue.producer.ProducerService;
import com.mb.livedataservice.util.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishMessage(String message) {
        kafkaTemplate.send(KafkaTopics.TEST_TOPIC, message);
        kafkaTemplate.send(KafkaTopics.CUSTOM_ORDERS, new Order(UUID.randomUUID(), UUID.randomUUID(), 100));
    }

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(KafkaTopics.TEST_TOPIC, 3, (short) 1);
    }
}
