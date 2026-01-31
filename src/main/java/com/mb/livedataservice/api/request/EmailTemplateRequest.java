package com.mb.livedataservice.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailTemplateRequest {

    @NotBlank(message = "{validation.template.code.notBlank}")
    @Size(max = 100, message = "{validation.template.code.size}")
    private String code;

    @Size(max = 255, message = "{validation.template.name.size}")
    private String name;

    @Size(max = 255, message = "{validation.template.subject.size}")
    private String subject;

    @NotBlank(message = "{validation.template.body.notBlank}")
    private String body;

    @Size(max = 500, message = "{validation.template.description.size}")
    private String description;

    private boolean active = true;
}
