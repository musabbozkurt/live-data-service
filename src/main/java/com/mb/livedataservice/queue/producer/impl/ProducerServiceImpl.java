package com.mb.livedataservice.queue.producer.impl;

import com.mb.livedataservice.config.KafkaTopics;
import com.mb.livedataservice.queue.producer.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    public void publishMessage(String message) {
        kafkaTemplate.send(kafkaTopics.getTestTopic(), message);
    }

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(kafkaTopics.getTestTopic(), 3, (short) 1);
    }
}
