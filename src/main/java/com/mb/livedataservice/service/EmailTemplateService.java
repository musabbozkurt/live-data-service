package com.mb.livedataservice.service;

import com.mb.livedataservice.api.request.EmailTemplateRequest;
import com.mb.livedataservice.api.response.EmailTemplateResponse;
import com.mb.livedataservice.data.model.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmailTemplateService {

    EmailTemplateResponse create(EmailTemplateRequest request);

    EmailTemplateResponse update(Long id, EmailTemplateRequest request);

    EmailTemplateResponse getById(Long id);

    EmailTemplateResponse getByCode(String code);

    Page<EmailTemplateResponse> getAll(Pageable pageable);

    void delete(Long id);

    EmailTemplate findActiveByCode(String code);
}
