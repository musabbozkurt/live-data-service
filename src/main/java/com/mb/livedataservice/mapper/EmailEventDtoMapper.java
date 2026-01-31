package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.request.EmailEventRequest;
import com.mb.livedataservice.data.model.EmailEvent;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class EmailEventDtoMapper {

    public EmailEventDto toDto(EmailEventRequest request) {
        EmailEventDto dto = new EmailEventDto();
        dto.setBody(request.getBody());
        dto.setSubject(request.getSubject());
        dto.setTo(request.getTo());
        dto.setCc(request.getCc());
        dto.setBcc(request.getBcc());
        dto.setTemplateCode(request.getTemplateCode());
        dto.setTemplateParameters(request.getTemplateParameters());
        return dto;
    }

    public EmailEvent toEntity(EmailEventDto emailEventDto) {
        EmailEvent emailEvent = new EmailEvent();
        emailEvent.setId(emailEventDto.getId());
        emailEvent.setBody(emailEventDto.getBody());
        emailEvent.setSubject(emailEventDto.getSubject());
        emailEvent.setTo(String.join(",", Objects.requireNonNullElse(emailEventDto.getTo(), Set.of())));
        emailEvent.setCc(String.join(",", Objects.requireNonNullElse(emailEventDto.getCc(), Set.of())));
        emailEvent.setBcc(String.join(",", Objects.requireNonNullElse(emailEventDto.getBcc(), Set.of())));
        return emailEvent;
    }
}
