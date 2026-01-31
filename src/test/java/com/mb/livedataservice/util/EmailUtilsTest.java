package com.mb.livedataservice.util;

import com.mb.livedataservice.queue.dto.EmailEventDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailUtilsTest {

    static Stream<Arguments> invalidEmailDtoProvider() {
        // Subject is empty
        EmailEventDto emptySubject = new EmailEventDto();
        emptySubject.setSubject("");
        emptySubject.setBody("Test Body");
        emptySubject.setTo(Set.of("test@example.com"));

        // Body is null
        EmailEventDto nullBody = new EmailEventDto();
        nullBody.setSubject("Test Subject");
        nullBody.setBody(null);
        nullBody.setTo(Set.of("test@example.com"));

        // Body is empty
        EmailEventDto emptyBody = new EmailEventDto();
        emptyBody.setSubject("Test Subject");
        emptyBody.setBody("");
        emptyBody.setTo(Set.of("test@example.com"));

        // To is empty
        EmailEventDto emptyTo = new EmailEventDto();
        emptyTo.setSubject("Test Subject");
        emptyTo.setBody("Test Body");
        emptyTo.setTo(Set.of());

        return Stream.of(
                Arguments.of("Subject is empty", emptySubject),
                Arguments.of("Body is null", nullBody),
                Arguments.of("Body is empty", emptyBody),
                Arguments.of("To is empty", emptyTo)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidEmailDtoProvider")
    void isValid_ShouldReturnFalse_WhenRequiredFieldIsInvalid(String testName, EmailEventDto dto) {
        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenAllFieldsAreValid() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("test@example.com"));
        dto.setCc(Set.of("cc@example.com"));
        dto.setBcc(Set.of("bcc@example.com"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTemplateCodeProvidedWithoutSubjectAndBody() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setTemplateCode("WELCOME_EMAIL");
        dto.setTo(Set.of("test@example.com"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenSubjectIsNull() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject(null);
        dto.setBody("Test Body");
        dto.setTo(Set.of("test@example.com"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenToContainsInvalidEmail() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("invalid-email"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenCcContainsInvalidEmail() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("test@example.com"));
        dto.setCc(Set.of("invalid-email"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenBccContainsInvalidEmail() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("test@example.com"));
        dto.setBcc(Set.of("invalid-email"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenCcAndBccAreEmpty() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("test@example.com"));
        dto.setCc(Set.of());
        dto.setBcc(Set.of());

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenCcAndBccAreNull() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("test@example.com"));
        dto.setCc(null);
        dto.setBcc(null);

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenMultipleValidEmailsProvided() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("user1@example.com", "user2@example.com"));
        dto.setCc(Set.of("cc1@example.com", "cc2@example.com"));
        dto.setBcc(Set.of("bcc1@example.com", "bcc2@example.com"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenMixOfValidAndInvalidEmails() {
        // Arrange
        EmailEventDto dto = new EmailEventDto();
        dto.setSubject("Test Subject");
        dto.setBody("Test Body");
        dto.setTo(Set.of("valid@example.com", "invalid-email"));

        // Act
        boolean result = EmailUtils.isValid(dto);

        // Assertions
        assertFalse(result);
    }
}
