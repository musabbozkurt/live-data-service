package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.api.request.EmailTemplateRequest;
import com.mb.livedataservice.api.response.EmailTemplateResponse;
import com.mb.livedataservice.data.model.EmailTemplate;
import com.mb.livedataservice.data.repository.EmailTemplateRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.mapper.EmailTemplateMapper;
import com.mb.livedataservice.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateMapper emailTemplateMapper;

    @Override
    @Transactional
    public EmailTemplateResponse create(EmailTemplateRequest request) {
        if (emailTemplateRepository.existsByCode(request.getCode())) {
            throw new BaseException(LiveDataErrorCode.INVALID_VALUE);
        }

        EmailTemplate template = emailTemplateMapper.toEntity(request);
        EmailTemplate savedTemplate = emailTemplateRepository.save(template);
        log.info("Email template created with id: {}", savedTemplate.getId());
        return emailTemplateMapper.toResponse(savedTemplate);
    }

    @Override
    @Transactional
    public EmailTemplateResponse update(Long id, EmailTemplateRequest request) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));

        // Check if code is being changed to an existing code
        if (!template.getCode().equals(request.getCode()) && emailTemplateRepository.existsByCode(request.getCode())) {
            throw new BaseException(LiveDataErrorCode.INVALID_VALUE);
        }

        emailTemplateMapper.updateEntity(template, request);
        EmailTemplate updatedTemplate = emailTemplateRepository.save(template);
        log.info("Email template updated with id: {}", updatedTemplate.getId());
        return emailTemplateMapper.toResponse(updatedTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse getById(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
        return emailTemplateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse getByCode(String code) {
        EmailTemplate template = emailTemplateRepository.findByCode(code)
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
        return emailTemplateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailTemplateResponse> getAll(Pageable pageable) {
        return emailTemplateRepository.findAll(pageable)
                .map(emailTemplateMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!emailTemplateRepository.existsById(id)) {
            throw new BaseException(LiveDataErrorCode.NOT_FOUND);
        }
        emailTemplateRepository.deleteById(id);
        log.info("Email template deleted with id: {}", id);
    }

    @Override
    public EmailTemplate findActiveByCode(String code) {
        return emailTemplateRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
    }
}
