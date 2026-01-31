package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.EmailTemplateRequest;
import com.mb.livedataservice.api.response.EmailTemplateResponse;
import com.mb.livedataservice.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email/templates")
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @PostMapping
    public ResponseEntity<EmailTemplateResponse> create(@Valid @RequestBody EmailTemplateRequest request) {
        return ResponseEntity.ok(emailTemplateService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplateResponse> update(@PathVariable Long id, @Valid @RequestBody EmailTemplateRequest request) {
        return ResponseEntity.ok(emailTemplateService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailTemplateResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(emailTemplateService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<EmailTemplateResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(emailTemplateService.getByCode(code));
    }

    @GetMapping
    public ResponseEntity<Page<EmailTemplateResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(emailTemplateService.getAll(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        emailTemplateService.delete(id);
        return ResponseEntity.notFound().build();
    }
}
