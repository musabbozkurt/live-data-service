package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.EmailTemplate;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import com.mb.livedataservice.service.EmailSender;
import com.mb.livedataservice.service.EmailTemplateService;
import com.mb.livedataservice.service.ThymeleafTemplateService;
import com.mb.livedataservice.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;
    private final ThymeleafTemplateService thymeleafTemplateService;

    @Value("${email.from:noreply@example.com}")
    private String emailFrom;

    @Value("${email.subject.prefix:}")
    private String subjectPrefix;

    @Override
    public void send(EmailEventDto emailEventDto) {
        if (!EmailUtils.isValid(emailEventDto)) {
            log.error("Email is not valid to send: {}", emailEventDto);
            return;
        }

        String subject;
        String body;

        // Resolve template if templateCode is provided
        if (emailEventDto.hasTemplate()) {
            log.info("Using email template: {}", emailEventDto.getTemplateCode());
            EmailTemplate template = emailTemplateService.findActiveByCode(emailEventDto.getTemplateCode());

            Map<String, Object> variables = new HashMap<>();
            if (emailEventDto.getTemplateParameters() != null) {
                variables.putAll(emailEventDto.getTemplateParameters());
            }

            subject = thymeleafTemplateService.processTemplate(template.getSubject(), variables);
            body = thymeleafTemplateService.processTemplate(template.getBody(), variables);
        } else {
            subject = emailEventDto.getSubject();
            body = emailEventDto.getBody();
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setSubject(subjectPrefix + subject);
        message.setText(body);
        message.setTo(emailEventDto.getTo().toArray(new String[0]));

        Set<String> cc = emailEventDto.getCc();
        if (!CollectionUtils.isEmpty(cc)) {
            message.setCc(cc.toArray(new String[0]));
        }

        Set<String> bcc = emailEventDto.getBcc();
        if (!CollectionUtils.isEmpty(bcc)) {
            message.setBcc(bcc.toArray(new String[0]));
        }

        mailSender.send(message);
        log.info("Email sent successfully with id: {}", emailEventDto.getId());
    }
}
