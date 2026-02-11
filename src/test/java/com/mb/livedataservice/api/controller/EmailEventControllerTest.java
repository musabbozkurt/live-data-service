package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.EmailAttachmentRequest;
import com.mb.livedataservice.api.request.EmailEventRequest;
import com.mb.livedataservice.exception.RestResponseExceptionHandler;
import com.mb.livedataservice.mapper.EmailEventDtoMapper;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import com.mb.livedataservice.queue.producer.impl.EmailEventProducer;
import com.mb.livedataservice.util.MessageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {EmailEventController.class, RestResponseExceptionHandler.class})
class EmailEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailEventProducer emailEventProducer;

    @MockitoBean
    private EmailEventDtoMapper emailEventDtoMapper;

    static Stream<Arguments> invalidEmailRequests() {
        // Load messages using MessageUtils
        String subjectSize = MessageUtils.getMessageFromBundle("validation.subject.size", Locale.ENGLISH);
        String bodyNotEmpty = MessageUtils.getMessageFromBundle("validation.body.notEmpty", Locale.ENGLISH);
        String toSize = MessageUtils.getMessageFromBundle("validation.to.size", Locale.ENGLISH);
        String templateCodeSize = MessageUtils.getMessageFromBundle("validation.templateCode.size", Locale.ENGLISH);
        String attachmentFileNameNotBlank = MessageUtils.getMessageFromBundle("validation.attachment.fileName.notBlank", Locale.ENGLISH);
        String attachmentFileNameSize = MessageUtils.getMessageFromBundle("validation.attachment.fileName.size", Locale.ENGLISH);
        String attachmentContentNotNull = MessageUtils.getMessageFromBundle("validation.attachment.content.notNull", Locale.ENGLISH);
        String attachmentContentTypeSize = MessageUtils.getMessageFromBundle("validation.attachment.contentType.size", Locale.ENGLISH);

        // Subject is empty
        EmailEventRequest requestWithEmptySubject = new EmailEventRequest();
        requestWithEmptySubject.setSubject("");
        requestWithEmptySubject.setBody("Test Body");
        requestWithEmptySubject.setTo(Set.of("test@example.com"));

        // Subject exceeds 100 characters
        EmailEventRequest requestWithLongSubject = new EmailEventRequest();
        requestWithLongSubject.setSubject("A".repeat(101));
        requestWithLongSubject.setBody("Test Body");
        requestWithLongSubject.setTo(Set.of("test@example.com"));

        // Body is empty
        EmailEventRequest requestWithEmptyBody = new EmailEventRequest();
        requestWithEmptyBody.setSubject("Test Subject");
        requestWithEmptyBody.setBody("");
        requestWithEmptyBody.setTo(Set.of("test@example.com"));

        // To is empty
        EmailEventRequest requestWithEmptyTo = new EmailEventRequest();
        requestWithEmptyTo.setSubject("Test Subject");
        requestWithEmptyTo.setBody("Test Body");
        requestWithEmptyTo.setTo(new HashSet<>());

        // Template code exceeds 100 characters
        EmailEventRequest requestWithLongTemplateCode = new EmailEventRequest();
        requestWithLongTemplateCode.setSubject("Test Subject");
        requestWithLongTemplateCode.setBody("Test Body");
        requestWithLongTemplateCode.setTo(Set.of("test@example.com"));
        requestWithLongTemplateCode.setTemplateCode("T".repeat(101));

        // Attachment with empty file name
        EmailEventRequest requestWithEmptyAttachmentFileName = getEventRequest("", "dGVzdA==");

        // Attachment with file name exceeding 255 characters
        EmailEventRequest requestWithLongAttachmentFileName = getEventRequest("A".repeat(256) + ".pdf", "dGVzdA==");

        // Attachment with null content
        EmailEventRequest requestWithNullAttachmentContent = getEventRequest("test.pdf", null);

        // Attachment with content type exceeding 100 characters
        EmailEventRequest requestWithLongAttachmentContentType = getEmailEventRequest();

        return Stream.of(
                Arguments.of("Subject is empty", requestWithEmptySubject, subjectSize),
                Arguments.of("Subject exceeds 100 characters", requestWithLongSubject, subjectSize),
                Arguments.of("Body is empty", requestWithEmptyBody, bodyNotEmpty),
                Arguments.of("To is empty", requestWithEmptyTo, toSize),
                Arguments.of("Template code exceeds 100 characters", requestWithLongTemplateCode, templateCodeSize),
                Arguments.of("Attachment file name is empty", requestWithEmptyAttachmentFileName, attachmentFileNameNotBlank),
                Arguments.of("Attachment file name exceeds 255 characters", requestWithLongAttachmentFileName, attachmentFileNameSize),
                Arguments.of("Attachment content is null", requestWithNullAttachmentContent, attachmentContentNotNull),
                Arguments.of("Attachment content type exceeds 100 characters", requestWithLongAttachmentContentType, attachmentContentTypeSize)
        );
    }

    static Stream<Arguments> invalidEmailRequestsTurkish() {
        Locale trLocale = Locale.forLanguageTag("tr");

        // Load Turkish messages using MessageUtils
        String subjectSize = MessageUtils.getMessageFromBundle("validation.subject.size", trLocale);
        String bodyNotEmpty = MessageUtils.getMessageFromBundle("validation.body.notEmpty", trLocale);
        String toSize = MessageUtils.getMessageFromBundle("validation.to.size", trLocale);
        String templateCodeSize = MessageUtils.getMessageFromBundle("validation.templateCode.size", trLocale);
        String attachmentFileNameNotBlank = MessageUtils.getMessageFromBundle("validation.attachment.fileName.notBlank", trLocale);
        String attachmentFileNameSize = MessageUtils.getMessageFromBundle("validation.attachment.fileName.size", trLocale);
        String attachmentContentNotNull = MessageUtils.getMessageFromBundle("validation.attachment.content.notNull", trLocale);
        String attachmentContentTypeSize = MessageUtils.getMessageFromBundle("validation.attachment.contentType.size", trLocale);

        // Subject is empty
        EmailEventRequest requestWithEmptySubject = new EmailEventRequest();
        requestWithEmptySubject.setSubject("");
        requestWithEmptySubject.setBody("Test Body");
        requestWithEmptySubject.setTo(Set.of("test@example.com"));

        // Body is empty
        EmailEventRequest requestWithEmptyBody = new EmailEventRequest();
        requestWithEmptyBody.setSubject("Test Subject");
        requestWithEmptyBody.setBody("");
        requestWithEmptyBody.setTo(Set.of("test@example.com"));

        // To is empty
        EmailEventRequest requestWithEmptyTo = new EmailEventRequest();
        requestWithEmptyTo.setSubject("Test Subject");
        requestWithEmptyTo.setBody("Test Body");
        requestWithEmptyTo.setTo(new HashSet<>());

        // Template code exceeds 100 characters
        EmailEventRequest requestWithLongTemplateCode = new EmailEventRequest();
        requestWithLongTemplateCode.setSubject("Test Subject");
        requestWithLongTemplateCode.setBody("Test Body");
        requestWithLongTemplateCode.setTo(Set.of("test@example.com"));
        requestWithLongTemplateCode.setTemplateCode("T".repeat(101));

        // Attachment with empty file name
        EmailEventRequest requestWithEmptyAttachmentFileName = getEventRequest("", "dGVzdA==");

        // Attachment with file name exceeding 255 characters
        EmailEventRequest requestWithLongAttachmentFileName = getEventRequest("A".repeat(256) + ".pdf", "dGVzdA==");

        // Attachment with null content
        EmailEventRequest requestWithNullAttachmentContent = getEventRequest("test.pdf", null);

        // Attachment with content type exceeding 100 characters
        EmailEventRequest requestWithLongAttachmentContentType = getEmailEventRequest();

        return Stream.of(
                Arguments.of("Subject is empty (TR)", requestWithEmptySubject, subjectSize),
                Arguments.of("Body is empty (TR)", requestWithEmptyBody, bodyNotEmpty),
                Arguments.of("To is empty (TR)", requestWithEmptyTo, toSize),
                Arguments.of("Template code exceeds 100 characters (TR)", requestWithLongTemplateCode, templateCodeSize),
                Arguments.of("Attachment file name is empty (TR)", requestWithEmptyAttachmentFileName, attachmentFileNameNotBlank),
                Arguments.of("Attachment file name exceeds 255 characters (TR)", requestWithLongAttachmentFileName, attachmentFileNameSize),
                Arguments.of("Attachment content is null (TR)", requestWithNullAttachmentContent, attachmentContentNotNull),
                Arguments.of("Attachment content type exceeds 100 characters (TR)", requestWithLongAttachmentContentType, attachmentContentTypeSize)
        );
    }

    private static EmailEventRequest getEventRequest(String fileName, String contentBase64) {
        EmailEventRequest requestWithEmptyAttachmentFileName = new EmailEventRequest();
        requestWithEmptyAttachmentFileName.setSubject("Test Subject");
        requestWithEmptyAttachmentFileName.setBody("Test Body");
        requestWithEmptyAttachmentFileName.setTo(Set.of("test@example.com"));
        EmailAttachmentRequest attachmentWithEmptyFileName = new EmailAttachmentRequest();
        attachmentWithEmptyFileName.setFileName(fileName);
        attachmentWithEmptyFileName.setContentBase64(contentBase64);
        requestWithEmptyAttachmentFileName.setAttachments(List.of(attachmentWithEmptyFileName));
        return requestWithEmptyAttachmentFileName;
    }

    private static EmailEventRequest getEmailEventRequest() {
        EmailEventRequest requestWithLongAttachmentContentType = new EmailEventRequest();
        requestWithLongAttachmentContentType.setSubject("Test Subject");
        requestWithLongAttachmentContentType.setBody("Test Body");
        requestWithLongAttachmentContentType.setTo(Set.of("test@example.com"));
        EmailAttachmentRequest attachmentWithLongContentType = new EmailAttachmentRequest();
        attachmentWithLongContentType.setFileName("test.pdf");
        attachmentWithLongContentType.setContentBase64("dGVzdA==");
        attachmentWithLongContentType.setContentType("A".repeat(101));
        requestWithLongAttachmentContentType.setAttachments(List.of(attachmentWithLongContentType));
        return requestWithLongAttachmentContentType;
    }

    @Test
    void produce_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        // Arrange
        EmailEventRequest request = new EmailEventRequest();
        request.setSubject("Test Subject");
        request.setBody("Test Body");
        request.setTo(Set.of("test@example.com"));
        request.setCc(Set.of("cc@example.com"));
        request.setBcc(Set.of("bcc@example.com"));
        request.setTemplateParameters(Map.of("key", "value"));

        EmailEventDto dto = new EmailEventDto();
        when(emailEventDtoMapper.toDto(any(EmailEventRequest.class))).thenReturn(dto);

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(emailEventProducer, times(1)).produce(any(EmailEventDto.class));
        verify(emailEventDtoMapper, times(1)).toDto(any(EmailEventRequest.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidEmailRequests")
    void produce_ShouldReturn400WithValidationMessage_WhenRequestIsInvalid(String testName, EmailEventRequest request, String expectedMessage) throws Exception {
        // Arrange
        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(expectedMessage)));

        verifyNoInteractions(emailEventProducer);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidEmailRequestsTurkish")
    void produce_ShouldReturn400WithTurkishValidationMessage_WhenRequestIsInvalidAndLocaleIsTurkish(String testName, EmailEventRequest request, String expectedMessage) throws Exception {
        // Arrange
        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "tr")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(expectedMessage)));

        verifyNoInteractions(emailEventProducer);
    }
}
