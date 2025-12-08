package com.mb.livedataservice.queue.consumer;

import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.util.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
public class CustomOrderListener {

    @KafkaListener(topics = Topics.CUSTOM_ORDERS, groupId = "${spring.kafka.consumer.group-id}")
    void listen(@Payload @Validated Order order) {
        log.info("Received custom order. listen - order: {}.", order);
        throw new BaseException(LiveDataErrorCode.UNEXPECTED_ERROR);
    }
}
