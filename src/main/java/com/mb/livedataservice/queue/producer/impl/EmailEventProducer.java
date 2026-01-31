package com.mb.livedataservice.queue.producer.impl;

import com.mb.livedataservice.queue.dto.EmailEventDto;
import com.mb.livedataservice.util.EmailUtils;
import com.mb.livedataservice.util.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventProducer {

    private final KafkaTemplate<String, EmailEventDto> kafkaTemplate;

    public void produce(EmailEventDto emailEventDto) {
        if (!EmailUtils.isValid(emailEventDto)) {
            log.error("Email is not valid to send: {}", emailEventDto);
            return;
        }

        kafkaTemplate.send(Topics.EMAIL_TOPIC, emailEventDto);
        log.info("Message produced id: {}", emailEventDto.getId());
    }
}
