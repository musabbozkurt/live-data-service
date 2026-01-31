package com.mb.livedataservice.queue.consumer;

import com.mb.livedataservice.data.repository.EmailEventRepository;
import com.mb.livedataservice.mapper.EmailEventDtoMapper;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import com.mb.livedataservice.service.EmailSender;
import com.mb.livedataservice.util.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.MailException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailEventConsumer {

    private final EmailSender emailSender;
    private final EmailEventRepository emailEventRepository;
    private final EmailEventDtoMapper emailEventDtoMapper;

    @RetryableTopic(
            attempts = "5",
            backOff = @BackOff(delay = 300000),
            include = MailException.class
    )
    @KafkaListener(
            groupId = "email-group",
            topics = Topics.EMAIL_TOPIC
    )
    public void listen(EmailEventDto eventDto) {
        log.info("Message consumed id: {}", eventDto.getId());

        emailSender.send(eventDto);

        emailEventRepository.save(emailEventDtoMapper.toEntity(eventDto));
    }

    @DltHandler
    public void listenDLT(EmailEventDto eventDto, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset) {
        log.error("DLT message consumed from topic: {}, offset: {}, eventId: {}", topic, offset, eventDto.getId());
    }
}
