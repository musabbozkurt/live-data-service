package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.request.EmailTemplateRequest;
import com.mb.livedataservice.api.response.EmailTemplateResponse;
import com.mb.livedataservice.data.model.EmailTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateMapper {

    public EmailTemplate toEntity(EmailTemplateRequest request) {
        EmailTemplate template = new EmailTemplate();
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setDescription(request.getDescription());
        template.setActive(request.isActive());
        return template;
    }

    public void updateEntity(EmailTemplate template, EmailTemplateRequest request) {
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setDescription(request.getDescription());
        template.setActive(request.isActive());
    }

    public EmailTemplateResponse toResponse(EmailTemplate template) {
        EmailTemplateResponse response = new EmailTemplateResponse();
        response.setId(template.getId());
        response.setCode(template.getCode());
        response.setName(template.getName());
        response.setSubject(template.getSubject());
        response.setBody(template.getBody());
        response.setDescription(template.getDescription());
        response.setActive(template.isActive());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }
}
