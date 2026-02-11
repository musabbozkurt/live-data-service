package com.mb.livedataservice.queue.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
public class EmailEventDto {

    private UUID id;
    private String subject;
    private String body;
    private Set<String> to;
    private Set<String> cc;
    private Set<String> bcc;
    private Long createdBy;
    private String templateCode;
    private Map<String, Object> templateParameters;
    private List<EmailAttachment> attachments = new ArrayList<>();

    public EmailEventDto() {
        this.id = UUID.randomUUID();
    }

    public boolean hasTemplate() {
        return StringUtils.hasText(templateCode);
    }
}
