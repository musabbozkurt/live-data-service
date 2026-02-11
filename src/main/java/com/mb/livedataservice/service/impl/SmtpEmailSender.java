package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.EmailTemplate;
import com.mb.livedataservice.queue.dto.EmailAttachment;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import com.mb.livedataservice.service.EmailSender;
import com.mb.livedataservice.service.EmailTemplateService;
import com.mb.livedataservice.service.ThymeleafTemplateService;
import com.mb.livedataservice.util.EmailUtils;
import com.mb.livedataservice.util.MimeTypeUtils;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
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

        try {
            String subject;
            String body;

            if (emailEventDto.hasTemplate()) {
                log.info("Using email template: {}", emailEventDto.getTemplateCode());
                EmailTemplate template = emailTemplateService.findActiveByCode(emailEventDto.getTemplateCode());
                Map<String, Object> variables = emailEventDto.getTemplateParameters();

                subject = thymeleafTemplateService.processTemplate(template.getSubject(), variables);
                body = thymeleafTemplateService.processTemplate(template.getBody(), variables);
            } else {
                subject = emailEventDto.getSubject();
                body = emailEventDto.getBody();
            }

            boolean isHtml = isHtmlContent(body);
            boolean hasAttachments = !CollectionUtils.isEmpty(emailEventDto.getAttachments());

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, hasAttachments, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setSubject(subjectPrefix + subject);
            helper.setText(body, isHtml);
            helper.setTo(emailEventDto.getTo().toArray(new String[0]));

            Set<String> cc = emailEventDto.getCc();
            if (!CollectionUtils.isEmpty(cc)) {
                helper.setCc(cc.toArray(new String[0]));
            }

            Set<String> bcc = emailEventDto.getBcc();
            if (!CollectionUtils.isEmpty(bcc)) {
                helper.setBcc(bcc.toArray(new String[0]));
            }

            // Add attachments if present
            if (hasAttachments) {
                for (EmailAttachment attachment : emailEventDto.getAttachments()) {
                    if (attachment.getContent() != null && StringUtils.isNotBlank(attachment.getFileName())) {
                        String contentType = MimeTypeUtils.resolveContentType(attachment.getFileName(), attachment.getContentType());
                        DataSource dataSource = new ByteArrayDataSource(attachment.getContent(), contentType);
                        helper.addAttachment(attachment.getFileName(), dataSource);
                        log.debug("Added attachment: {} with content type: {}", attachment.getFileName(), contentType);
                    }
                }
            }

            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully with id: {}", emailEventDto.getId());
        } catch (Exception e) {
            log.error("Failed to send email with id: {}, exception: {}", emailEventDto.getId(), ExceptionUtils.getStackTrace(e));
            throw new MailSendException("Failed to send email", e);
        }
    }

    private boolean isHtmlContent(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        String trimmed = content.trim().toLowerCase();
        return trimmed.startsWith("<!doctype html") ||
                trimmed.startsWith("<html") ||
                trimmed.contains("<body") ||
                trimmed.contains("<table") ||
                trimmed.contains("<div");
    }
}
