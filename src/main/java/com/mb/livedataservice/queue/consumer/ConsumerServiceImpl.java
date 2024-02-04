package com.mb.livedataservice.queue.consumer;

import com.mb.livedataservice.queue.dto.QueueRequest;
import com.mb.livedataservice.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl {

    @KafkaListener(topics = "${spring.kafka.kafka-topics.test-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String message) {
        log.info("Received a request to consume a message. consumeMessage - message: {}.", message);
        QueueRequest queueRequest = JsonUtils.deserialize(message, QueueRequest.class);
        log.info("QueueRequest is deserialized. consumeMessage - QueueRequest: {}.", queueRequest);
    }
}
