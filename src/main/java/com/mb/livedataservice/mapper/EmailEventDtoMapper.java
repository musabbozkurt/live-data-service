package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.request.EmailAttachmentRequest;
import com.mb.livedataservice.api.request.EmailEventRequest;
import com.mb.livedataservice.data.model.EmailEvent;
import com.mb.livedataservice.queue.dto.EmailAttachment;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Base64;
import java.util.List;
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
        dto.setAttachments(toAttachmentDtoList(request.getAttachments()));
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

    private List<EmailAttachment> toAttachmentDtoList(List<EmailAttachmentRequest> attachmentRequests) {
        if (CollectionUtils.isEmpty(attachmentRequests)) {
            return List.of();
        }

        return attachmentRequests.stream()
                .map(this::toAttachmentDto)
                .toList();
    }

    private EmailAttachment toAttachmentDto(EmailAttachmentRequest request) {
        return EmailAttachment.builder()
                .fileName(request.getFileName())
                .content(Base64.getDecoder().decode(request.getContentBase64()))
                .contentType(request.getContentType())
                .build();
    }
}
