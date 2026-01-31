package com.mb.livedataservice.api.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EmailTemplateResponse {

    private Long id;
    private String code;
    private String name;
    private String subject;
    private String body;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
