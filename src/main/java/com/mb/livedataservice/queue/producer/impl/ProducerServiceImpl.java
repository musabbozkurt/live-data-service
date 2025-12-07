package com.mb.livedataservice.queue.producer.impl;

import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.queue.producer.ProducerService;
import com.mb.livedataservice.util.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.jms.core.JmsClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<@NonNull String, @NonNull Object> kafkaTemplate;
    private final JmsClient jmsClient;

    public void publishMessage(String message) {
        kafkaTemplate.send(Topics.TEST_TOPIC, message);
        kafkaTemplate.send(Topics.CUSTOM_ORDERS, new Order(UUID.randomUUID(), UUID.randomUUID(), 100));
        kafkaTemplate.send(Topics.ORDERS, new Order(UUID.randomUUID(), UUID.randomUUID(), 200));
    }

    public void publishJmsMessage() {
        jmsClient.destination(Topics.JMS_CUSTOM_ORDERS).send(new Order(UUID.randomUUID(), UUID.randomUUID(), 300));
        jmsClient
                .destination(Topics.JMS_CUSTOM_ORDERS)
                .withTimeToLive(300000)  // 5 minutes TTL
                .withPriority(9)         // Highest priority
                .withDeliveryDelay(1000) // 1-second delay
                .send(new Order(UUID.randomUUID(), UUID.randomUUID(), 400));
    }
}
