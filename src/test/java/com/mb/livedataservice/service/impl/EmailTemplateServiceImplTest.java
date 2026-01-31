package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.api.request.EmailTemplateRequest;
import com.mb.livedataservice.api.response.EmailTemplateResponse;
import com.mb.livedataservice.data.model.EmailTemplate;
import com.mb.livedataservice.data.repository.EmailTemplateRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.mapper.EmailTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceImplTest {

    @InjectMocks
    private EmailTemplateServiceImpl emailTemplateService;

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Spy
    private EmailTemplateMapper emailTemplateMapper;

    private EmailTemplateRequest validRequest;
    private EmailTemplate emailTemplate;

    @BeforeEach
    void setUp() {
        validRequest = new EmailTemplateRequest();
        validRequest.setCode("WELCOME_EMAIL");
        validRequest.setName("Welcome Email");
        validRequest.setSubject("Welcome {{name}}!");
        validRequest.setBody("Hello {{name}}, welcome to our platform!");
        validRequest.setDescription("Welcome email template");
        validRequest.setActive(true);

        emailTemplate = new EmailTemplate();
        emailTemplate.setId(1L);
        emailTemplate.setCode("WELCOME_EMAIL");
        emailTemplate.setName("Welcome Email");
        emailTemplate.setSubject("Welcome {{name}}!");
        emailTemplate.setBody("Hello {{name}}, welcome to our platform!");
        emailTemplate.setDescription("Welcome email template");
        emailTemplate.setActive(true);
    }

    @Test
    void create_ShouldCreateTemplate_WhenCodeDoesNotExist() {
        // Arrange
        when(emailTemplateRepository.existsByCode(anyString())).thenReturn(false);
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(emailTemplate);

        // Act
        EmailTemplateResponse response = emailTemplateService.create(validRequest);

        // Assertions
        assertNotNull(response);
        assertEquals("WELCOME_EMAIL", response.getCode());
        verify(emailTemplateRepository).existsByCode("WELCOME_EMAIL");
        verify(emailTemplateRepository).save(any(EmailTemplate.class));
    }

    @Test
    void create_ShouldThrowException_WhenCodeAlreadyExists() {
        // Arrange
        when(emailTemplateRepository.existsByCode(anyString())).thenReturn(true);

        // Act
        // Assertions
        BaseException exception = assertThrows(BaseException.class, () -> emailTemplateService.create(validRequest));

        assertEquals(LiveDataErrorCode.INVALID_VALUE.getMessage(), exception.getMessage());
        verify(emailTemplateRepository).existsByCode("WELCOME_EMAIL");
        verify(emailTemplateRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateTemplate_WhenTemplateExists() {
        // Arrange
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(emailTemplate));
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(emailTemplate);

        // Act
        EmailTemplateResponse response = emailTemplateService.update(1L, validRequest);

        // Assertions
        assertNotNull(response);
        verify(emailTemplateRepository).findById(1L);
        verify(emailTemplateRepository).save(any(EmailTemplate.class));
    }

    @Test
    void update_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        // Assertions
        BaseException exception = assertThrows(BaseException.class, () -> emailTemplateService.update(1L, validRequest));

        assertEquals(LiveDataErrorCode.NOT_FOUND.getMessage(), exception.getMessage());
        verify(emailTemplateRepository).findById(1L);
        verify(emailTemplateRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenChangingToExistingCode() {
        // Arrange
        EmailTemplateRequest updateRequest = new EmailTemplateRequest();
        updateRequest.setCode("DIFFERENT_CODE");
        updateRequest.setBody("Body");

        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(emailTemplate));
        when(emailTemplateRepository.existsByCode("DIFFERENT_CODE")).thenReturn(true);

        // Act
        // Assertions
        BaseException exception = assertThrows(BaseException.class, () -> emailTemplateService.update(1L, updateRequest));

        assertEquals(LiveDataErrorCode.INVALID_VALUE.getMessage(), exception.getMessage());
    }

    @Test
    void update_ShouldNotCheckExistence_WhenCodeNotChanged() {
        // Arrange
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(emailTemplate));
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(emailTemplate);

        // Act
        emailTemplateService.update(1L, validRequest);

        // Assertions
        verify(emailTemplateRepository, never()).existsByCode(anyString());
    }

    @Test
    void getById_ShouldReturnTemplate_WhenTemplateExists() {
        // Arrange
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(emailTemplate));

        // Act
        EmailTemplateResponse response = emailTemplateService.getById(1L);

        // Assertions
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("WELCOME_EMAIL", response.getCode());
    }

    @Test
    void getById_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        // Assertions
        BaseException exception = assertThrows(BaseException.class, () -> emailTemplateService.getById(1L));

        assertEquals(LiveDataErrorCode.NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void getByCode_ShouldReturnTemplate_WhenTemplateExists() {
        // Arrange
        when(emailTemplateRepository.findByCode("WELCOME_EMAIL")).thenReturn(Optional.of(emailTemplate));

        // Act
        EmailTemplateResponse response = emailTemplateService.getByCode("WELCOME_EMAIL");

        // Assertions
        assertNotNull(response);
        assertEquals("WELCOME_EMAIL", response.getCode());
    }

    @Test
    void getByCode_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(emailTemplateRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        // Act
        // Assertions
        BaseException exception = assertThrows(BaseException.class, () -> emailTemplateService.getByCode("UNKNOWN"));

        assertEquals(LiveDataErrorCode.NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void getAll_ShouldReturnPageOfTemplates_WhenTemplatesExist() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<EmailTemplate> templates = List.of(emailTemplate);
        Page<EmailTemplate> templatePage = new PageImpl<>(templates, pageable, 1);

        when(emailTemplateRepository.findAll(pageable)).thenReturn(templatePage);

        // Act
        Page<EmailTemplateResponse> response = emailTemplateService.getAll(pageable);

        // Assertions
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("WELCOME_EMAIL", response.getContent().getFirst().getCode());
    }

    @Test
    void getAll_ShouldReturnEmptyPage_WhenNoTemplatesExist() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmailTemplate> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(emailTemplateRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<EmailTemplateResponse> response = emailTemplateService.getAll(pageable);

        // Assertions
        assertNotNull(response);
        assertEquals(0, response.getTotalElements());
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void delete_ShouldDeleteTemplate_WhenTemplateExists() {
        // Arrange
        when(emailTemplateRepository.existsById(1L)).thenReturn(true);
        doNothing().when(emailTemplateRepository).deleteById(1L);

        // Act
        // Assertions
        assertDoesNotThrow(() -> emailTemplateService.delete(1L));

        verify(emailTemplateRepository).existsById(1L);
        verify(emailTemplateRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenTemplateNotFound() {
        // Arrange
        when(emailTemplateRepository.existsById(1L)).thenReturn(false);

        // Act
        // Assertions
        BaseException exception = assertThrows(BaseException.class, () -> emailTemplateService.delete(1L));

        assertEquals(LiveDataErrorCode.NOT_FOUND.getMessage(), exception.getMessage());
        verify(emailTemplateRepository, never()).deleteById(anyLong());
    }

    @Test
    void findActiveByCode_ShouldReturnTemplate_WhenActiveTemplateExists() {
        // Arrange
        when(emailTemplateRepository.findByCodeAndActiveTrue("WELCOME_EMAIL")).thenReturn(Optional.of(emailTemplate));

        // Act
        EmailTemplate result = emailTemplateService.findActiveByCode("WELCOME_EMAIL");

        // Assertions
        assertNotNull(result);
        assertEquals("WELCOME_EMAIL", result.getCode());
        assertTrue(result.isActive());
    }

    @Test
    void findActiveByCode_ShouldThrowException_WhenTemplateNotFoundOrInactive() {
        // Arrange
        when(emailTemplateRepository.findByCodeAndActiveTrue("INACTIVE")).thenReturn(Optional.empty());

        // Act
        // Assertions
        BaseException exception = assertThrows(BaseException.class, () -> emailTemplateService.findActiveByCode("INACTIVE"));

        assertEquals(LiveDataErrorCode.NOT_FOUND.getMessage(), exception.getMessage());
    }
}
