package com.mb.livedataservice.api.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class EmailEventRequest {

    @Size(min = 1, max = 100, message = "{validation.subject.size}")
    private String subject;

    @NotEmpty(message = "{validation.body.notEmpty}")
    private String body;

    @NotEmpty(message = "{validation.to.size}")
    private Set<String> to = new HashSet<>();

    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();

    // Template support - when templateCode is provided, subject and body from template will be used
    @Size(max = 100, message = "{validation.templateCode.size}")
    private String templateCode;

    private Map<String, Object> templateParameters = new HashMap<>();
}
