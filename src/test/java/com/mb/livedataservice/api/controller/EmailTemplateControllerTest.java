package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.EmailTemplateRequest;
import com.mb.livedataservice.api.response.EmailTemplateResponse;
import com.mb.livedataservice.exception.RestResponseExceptionHandler;
import com.mb.livedataservice.service.EmailTemplateService;
import com.mb.livedataservice.util.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {EmailTemplateController.class, RestResponseExceptionHandler.class})
class EmailTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailTemplateService emailTemplateService;

    private EmailTemplateRequest validRequest;
    private EmailTemplateResponse templateResponse;

    static Stream<Arguments> invalidTemplateRequests() {
        // Load messages using MessageUtils
        String codeNotBlank = MessageUtils.getMessageFromBundle("validation.template.code.notBlank", Locale.ENGLISH);
        String codeSize = MessageUtils.getMessageFromBundle("validation.template.code.size", Locale.ENGLISH);
        String bodyNotBlank = MessageUtils.getMessageFromBundle("validation.template.body.notBlank", Locale.ENGLISH);
        String nameSize = MessageUtils.getMessageFromBundle("validation.template.name.size", Locale.ENGLISH);
        String subjectSize = MessageUtils.getMessageFromBundle("validation.template.subject.size", Locale.ENGLISH);
        String descriptionSize = MessageUtils.getMessageFromBundle("validation.template.description.size", Locale.ENGLISH);

        // Code is blank
        EmailTemplateRequest requestWithBlankCode = new EmailTemplateRequest();
        requestWithBlankCode.setCode("");
        requestWithBlankCode.setBody("Test Body");

        // Code is null
        EmailTemplateRequest requestWithNullCode = new EmailTemplateRequest();
        requestWithNullCode.setCode(null);
        requestWithNullCode.setBody("Test Body");

        // Code exceeds 100 characters
        EmailTemplateRequest requestWithLongCode = new EmailTemplateRequest();
        requestWithLongCode.setCode("C".repeat(101));
        requestWithLongCode.setBody("Test Body");

        // Body is blank
        EmailTemplateRequest requestWithBlankBody = new EmailTemplateRequest();
        requestWithBlankBody.setCode("VALID_CODE");
        requestWithBlankBody.setBody("");

        // Body is null
        EmailTemplateRequest requestWithNullBody = new EmailTemplateRequest();
        requestWithNullBody.setCode("VALID_CODE");
        requestWithNullBody.setBody(null);

        // Name exceeds 255 characters
        EmailTemplateRequest requestWithLongName = new EmailTemplateRequest();
        requestWithLongName.setCode("VALID_CODE");
        requestWithLongName.setBody("Test Body");
        requestWithLongName.setName("N".repeat(256));

        // Subject exceeds 255 characters
        EmailTemplateRequest requestWithLongSubject = new EmailTemplateRequest();
        requestWithLongSubject.setCode("VALID_CODE");
        requestWithLongSubject.setBody("Test Body");
        requestWithLongSubject.setSubject("S".repeat(256));

        // Description exceeds 500 characters
        EmailTemplateRequest requestWithLongDescription = new EmailTemplateRequest();
        requestWithLongDescription.setCode("VALID_CODE");
        requestWithLongDescription.setBody("Test Body");
        requestWithLongDescription.setDescription("D".repeat(501));

        return Stream.of(
                Arguments.of("Code is blank", requestWithBlankCode, codeNotBlank),
                Arguments.of("Code is null", requestWithNullCode, codeNotBlank),
                Arguments.of("Code exceeds 100 characters", requestWithLongCode, codeSize),
                Arguments.of("Body is blank", requestWithBlankBody, bodyNotBlank),
                Arguments.of("Body is null", requestWithNullBody, bodyNotBlank),
                Arguments.of("Name exceeds 255 characters", requestWithLongName, nameSize),
                Arguments.of("Subject exceeds 255 characters", requestWithLongSubject, subjectSize),
                Arguments.of("Description exceeds 500 characters", requestWithLongDescription, descriptionSize)
        );
    }

    static Stream<Arguments> invalidTemplateRequestsTurkish() {
        Locale trLocale = Locale.forLanguageTag("tr");

        // Load Turkish messages using MessageUtils
        String codeNotBlank = MessageUtils.getMessageFromBundle("validation.template.code.notBlank", trLocale);
        String codeSize = MessageUtils.getMessageFromBundle("validation.template.code.size", trLocale);
        String bodyNotBlank = MessageUtils.getMessageFromBundle("validation.template.body.notBlank", trLocale);
        String nameSize = MessageUtils.getMessageFromBundle("validation.template.name.size", trLocale);
        String subjectSize = MessageUtils.getMessageFromBundle("validation.template.subject.size", trLocale);
        String descriptionSize = MessageUtils.getMessageFromBundle("validation.template.description.size", trLocale);

        // Code is blank
        EmailTemplateRequest requestWithBlankCode = new EmailTemplateRequest();
        requestWithBlankCode.setCode("");
        requestWithBlankCode.setBody("Test Body");

        // Code exceeds 100 characters
        EmailTemplateRequest requestWithLongCode = new EmailTemplateRequest();
        requestWithLongCode.setCode("C".repeat(101));
        requestWithLongCode.setBody("Test Body");

        // Body is blank
        EmailTemplateRequest requestWithBlankBody = new EmailTemplateRequest();
        requestWithBlankBody.setCode("VALID_CODE");
        requestWithBlankBody.setBody("");

        // Name exceeds 255 characters
        EmailTemplateRequest requestWithLongName = new EmailTemplateRequest();
        requestWithLongName.setCode("VALID_CODE");
        requestWithLongName.setBody("Test Body");
        requestWithLongName.setName("N".repeat(256));

        // Subject exceeds 255 characters
        EmailTemplateRequest requestWithLongSubject = new EmailTemplateRequest();
        requestWithLongSubject.setCode("VALID_CODE");
        requestWithLongSubject.setBody("Test Body");
        requestWithLongSubject.setSubject("S".repeat(256));

        // Description exceeds 500 characters
        EmailTemplateRequest requestWithLongDescription = new EmailTemplateRequest();
        requestWithLongDescription.setCode("VALID_CODE");
        requestWithLongDescription.setBody("Test Body");
        requestWithLongDescription.setDescription("D".repeat(501));

        return Stream.of(
                Arguments.of("Code is blank (TR)", requestWithBlankCode, codeNotBlank),
                Arguments.of("Code exceeds 100 characters (TR)", requestWithLongCode, codeSize),
                Arguments.of("Body is blank (TR)", requestWithBlankBody, bodyNotBlank),
                Arguments.of("Name exceeds 255 characters (TR)", requestWithLongName, nameSize),
                Arguments.of("Subject exceeds 255 characters (TR)", requestWithLongSubject, subjectSize),
                Arguments.of("Description exceeds 500 characters (TR)", requestWithLongDescription, descriptionSize)
        );
    }

    @BeforeEach
    void setUp() {
        validRequest = new EmailTemplateRequest();
        validRequest.setCode("WELCOME_EMAIL");
        validRequest.setName("Welcome Email");
        validRequest.setSubject("Welcome {{name}}!");
        validRequest.setBody("Hello {{name}}, welcome!");
        validRequest.setDescription("Welcome email template");
        validRequest.setActive(true);

        templateResponse = new EmailTemplateResponse();
        templateResponse.setId(1L);
        templateResponse.setCode("WELCOME_EMAIL");
        templateResponse.setName("Welcome Email");
        templateResponse.setSubject("Welcome {{name}}!");
        templateResponse.setBody("Hello {{name}}, welcome!");
        templateResponse.setDescription("Welcome email template");
        templateResponse.setActive(true);
    }

    @Test
    void create_ShouldReturnCreatedTemplate_WhenRequestIsValid() throws Exception {
        // Arrange
        when(emailTemplateService.create(any(EmailTemplateRequest.class))).thenReturn(templateResponse);

        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/email/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(emailTemplateService).create(any(EmailTemplateRequest.class));
    }

    @Test
    void update_ShouldReturnUpdatedTemplate_WhenRequestIsValid() throws Exception {
        // Arrange
        when(emailTemplateService.update(eq(1L), any(EmailTemplateRequest.class))).thenReturn(templateResponse);

        // Act
        // Assertions
        mockMvc.perform(put("/api/v1/email/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(emailTemplateService).update(eq(1L), any(EmailTemplateRequest.class));
    }

    @Test
    void getById_ShouldReturnTemplate_WhenTemplateExists() throws Exception {
        // Arrange
        when(emailTemplateService.getById(1L)).thenReturn(templateResponse);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/email/templates/1"))
                .andExpect(status().isOk());

        verify(emailTemplateService).getById(1L);
    }

    @Test
    void getByCode_ShouldReturnTemplate_WhenTemplateExists() throws Exception {
        // Arrange
        when(emailTemplateService.getByCode("WELCOME_EMAIL")).thenReturn(templateResponse);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/email/templates/code/WELCOME_EMAIL"))
                .andExpect(status().isOk());

        verify(emailTemplateService).getByCode("WELCOME_EMAIL");
    }

    @Test
    void getAll_ShouldReturnPageOfTemplates_WhenTemplatesExist() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<EmailTemplateResponse> page = new PageImpl<>(List.of(templateResponse), pageable, 1);
        when(emailTemplateService.getAll(any(Pageable.class))).thenReturn(page);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/email/templates")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(emailTemplateService).getAll(any(Pageable.class));
    }

    @Test
    void getAll_ShouldReturnEmptyPage_WhenNoTemplatesExist() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<EmailTemplateResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(emailTemplateService.getAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        // Assertions
        mockMvc.perform(get("/api/v1/email/templates"))
                .andExpect(status().isOk());

        verify(emailTemplateService).getAll(any(Pageable.class));
    }

    @Test
    void delete_ShouldReturnNotFound_WhenTemplateDeleted() throws Exception {
        // Arrange
        doNothing().when(emailTemplateService).delete(1L);

        // Act
        // Assertions
        mockMvc.perform(delete("/api/v1/email/templates/1"))
                .andExpect(status().isNotFound());

        verify(emailTemplateService).delete(1L);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTemplateRequests")
    void create_ShouldReturn400WithValidationMessage_WhenRequestIsInvalid(String testName, EmailTemplateRequest request, String expectedMessage) throws Exception {
        // Arrange
        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/email/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(expectedMessage)));

        verifyNoInteractions(emailTemplateService);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTemplateRequests")
    void update_ShouldReturn400WithValidationMessage_WhenRequestIsInvalid(String testName, EmailTemplateRequest request, String expectedMessage) throws Exception {
        // Arrange
        // Act
        // Assertions
        mockMvc.perform(put("/api/v1/email/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(expectedMessage)));

        verifyNoInteractions(emailTemplateService);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTemplateRequestsTurkish")
    void create_ShouldReturn400WithTurkishValidationMessage_WhenRequestIsInvalidAndLocaleIsTurkish(String testName, EmailTemplateRequest request, String expectedMessage) throws Exception {
        // Arrange
        // Act
        // Assertions
        mockMvc.perform(post("/api/v1/email/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "tr")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(expectedMessage)));

        verifyNoInteractions(emailTemplateService);
    }
}
