package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.EmailTemplate;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import com.mb.livedataservice.service.EmailTemplateService;
import com.mb.livedataservice.service.ThymeleafTemplateService;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

    @InjectMocks
    private SmtpEmailSender smtpEmailSender;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private ThymeleafTemplateService thymeleafTemplateService;

    @BeforeEach
    void init() {
        smtpEmailSender = new SmtpEmailSender(mailSender, emailTemplateService, thymeleafTemplateService);
        ReflectionTestUtils.setField(smtpEmailSender, "emailFrom", "sender@test.com");
        ReflectionTestUtils.setField(smtpEmailSender, "subjectPrefix", "Prefix: ");
    }

    @Test
    void send_ShouldSendEmail_WhenAllFieldsAreValid() {
        // Arrange
        EmailEventDto emailEventDto = Instancio.of(EmailEventDto.class)
                .set(Select.field(EmailEventDto::getTo), Set.of("to@test.com"))
                .set(Select.field(EmailEventDto::getCc), Set.of("cc@test.com"))
                .set(Select.field(EmailEventDto::getBcc), Set.of("bcc@test.com"))
                .set(Select.field(EmailEventDto::getTemplateCode), null)
                .create();
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_ShouldSendEmail_WhenOptionalFieldsAreEmpty() {
        // Arrange
        EmailEventDto emailEventDto = Instancio.of(EmailEventDto.class)
                .set(Select.field(EmailEventDto::getTo), Set.of("to@test.com"))
                .set(Select.field(EmailEventDto::getCc), new HashSet<>())
                .set(Select.field(EmailEventDto::getBcc), new HashSet<>())
                .set(Select.field(EmailEventDto::getTemplateCode), null)
                .create();
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_ShouldSendEmail_WhenCcAndBccAreNull() {
        // Arrange
        EmailEventDto emailEventDto = Instancio.of(EmailEventDto.class)
                .set(Select.field(EmailEventDto::getTo), Set.of("to@test.com"))
                .set(Select.field(EmailEventDto::getCc), null)
                .set(Select.field(EmailEventDto::getBcc), null)
                .set(Select.field(EmailEventDto::getTemplateCode), null)
                .create();
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void send_ShouldNotSendEmail_WhenSubjectIsBlankOrNull(String subject) {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setSubject(subject);

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void send_ShouldNotSendEmail_WhenBodyIsBlankOrNull(String body) {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setBody(body);

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @Test
    void send_ShouldNotSendEmail_WhenToIsNull() {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setTo(null);

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @Test
    void send_ShouldNotSendEmail_WhenToIsEmpty() {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setTo(new HashSet<>());

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@", "@test.com", "test.com", "test@test@test.com"})
    void send_ShouldNotSendEmail_WhenToEmailIsInvalid(String invalidEmail) {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setTo(Set.of(invalidEmail));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@", "@test.com", "test.com", "test@test@test.com"})
    void send_ShouldNotSendEmail_WhenCcEmailIsInvalid(String invalidEmail) {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setCc(Set.of(invalidEmail));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "test@", "@test.com", "test.com", "test@test@test.com"})
    void send_ShouldNotSendEmail_WhenBccEmailIsInvalid(String invalidEmail) {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setBcc(Set.of(invalidEmail));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @Test
    void send_ShouldNotSendEmail_WhenToHasMixOfValidAndInvalidEmails() {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        emailEventDto.setTo(Set.of("valid@test.com", "invalid-email"));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verifyNoInteractions(mailSender);
    }

    @Test
    void send_ShouldThrowException_WhenMailSenderFails() {
        // Arrange
        EmailEventDto emailEventDto = createValidEmailEventDto();
        doThrow(new MailSendException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        // Assertions
        assertThrows(MailSendException.class, () -> smtpEmailSender.send(emailEventDto));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_ShouldSendEmailWithResolvedTemplate_WhenTemplateCodeIsProvided() {
        // Arrange
        EmailTemplate template = new EmailTemplate();
        template.setCode("WELCOME_EMAIL");
        template.setSubject("Welcome [[${name}]]!");
        template.setBody("Hello [[${name}]], welcome to our platform!");

        EmailEventDto emailEventDto = createTemplateEmailEventDto("WELCOME_EMAIL", Map.of("name", "John"));

        when(emailTemplateService.findActiveByCode("WELCOME_EMAIL")).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(eq("Welcome [[${name}]]!"), anyMap())).thenReturn("Welcome John!");
        when(thymeleafTemplateService.processTemplate(eq("Hello [[${name}]], welcome to our platform!"), anyMap())).thenReturn("Hello John, welcome to our platform!");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(emailTemplateService).findActiveByCode("WELCOME_EMAIL");
        verify(thymeleafTemplateService, times(2)).processTemplate(anyString(), anyMap());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("Prefix: Welcome John!", sentMessage.getSubject());
        assertEquals("Hello John, welcome to our platform!", sentMessage.getText());
    }

    @Test
    @Disabled("Disabled until handling of missing template is implemented")
    void send_ShouldSendEmailWithTemplate_WhenNoParametersProvided() {
        // Arrange
        EmailTemplate template = new EmailTemplate();
        template.setCode("SIMPLE_EMAIL");
        template.setSubject("Simple Subject");
        template.setBody("Simple body without placeholders");

        EmailEventDto emailEventDto = createTemplateEmailEventDto("SIMPLE_EMAIL", null);

        when(emailTemplateService.findActiveByCode("SIMPLE_EMAIL")).thenReturn(template);
        when(thymeleafTemplateService.processTemplate("Simple Subject", null)).thenReturn("Simple Subject");
        when(thymeleafTemplateService.processTemplate("Simple body without placeholders", null)).thenReturn("Simple body without placeholders");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertEquals("Prefix: Simple Subject", sentMessage.getSubject());
        assertEquals("Simple body without placeholders", sentMessage.getText());
    }

    @Test
    void send_ShouldResolveAllPlaceholders_WhenMultiplePlaceholdersExist() {
        // Arrange
        EmailTemplate template = new EmailTemplate();
        template.setCode("ORDER_EMAIL");
        template.setSubject("Order [[${orderId}]] confirmed");
        template.setBody("Dear [[${customerName}]], your order [[${orderId}]] for [[${amount}]] has been confirmed.");

        Map<String, Object> parameters = Map.of(
                "orderId", "12345",
                "customerName", "John Doe",
                "amount", "$99.99"
        );

        EmailEventDto emailEventDto = createTemplateEmailEventDto("ORDER_EMAIL", parameters);

        when(emailTemplateService.findActiveByCode("ORDER_EMAIL")).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(eq(template.getSubject()), anyMap())).thenReturn("Order 12345 confirmed");
        when(thymeleafTemplateService.processTemplate(eq(template.getBody()), anyMap())).thenReturn("Dear John Doe, your order 12345 for $99.99 has been confirmed.");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("Prefix: Order 12345 confirmed", sentMessage.getSubject());
        assertEquals("Dear John Doe, your order 12345 for $99.99 has been confirmed.", sentMessage.getText());
    }

    @Test
    void send_ShouldSendEmail_WhenTemplateAndEmptyParametersProvided() {
        // Arrange
        EmailTemplate template = new EmailTemplate();
        template.setCode("NOTIFICATION");
        template.setSubject("Notification");
        template.setBody("You have a new notification.");

        EmailEventDto emailEventDto = createTemplateEmailEventDto("NOTIFICATION", Map.of());

        when(emailTemplateService.findActiveByCode("NOTIFICATION")).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(anyString(), anyMap())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplateService).findActiveByCode("NOTIFICATION");
    }

    @Test
    void send_ShouldBeValid_WhenUsingTemplateWithoutSubjectAndBody() {
        // Arrange
        EmailEventDto emailEventDto = new EmailEventDto();
        emailEventDto.setTo(Set.of("to@test.com"));
        emailEventDto.setTemplateCode("WELCOME_EMAIL");

        EmailTemplate template = new EmailTemplate();
        template.setSubject("Subject");
        template.setBody("Body");

        when(emailTemplateService.findActiveByCode("WELCOME_EMAIL")).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(anyString(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    private EmailEventDto createValidEmailEventDto() {
        return Instancio.of(EmailEventDto.class)
                .set(Select.field(EmailEventDto::getId), Instancio.create(UUID.class))
                .set(Select.field(EmailEventDto::getTo), Set.of("to@test.com"))
                .set(Select.field(EmailEventDto::getCc), Set.of("cc@test.com"))
                .set(Select.field(EmailEventDto::getBcc), Set.of("bcc@test.com"))
                .set(Select.field(EmailEventDto::getSubject), "Test Subject")
                .set(Select.field(EmailEventDto::getBody), "Test Body")
                .set(Select.field(EmailEventDto::getTemplateCode), null)
                .set(Select.field(EmailEventDto::getTemplateParameters), null)
                .create();
    }

    private EmailEventDto createTemplateEmailEventDto(String templateCode, Map<String, Object> parameters) {
        EmailEventDto dto = new EmailEventDto();
        dto.setTo(Set.of("to@test.com"));
        dto.setCc(Set.of("cc@test.com"));
        dto.setBcc(Set.of("bcc@test.com"));
        dto.setTemplateCode(templateCode);
        dto.setTemplateParameters(parameters);
        return dto;
    }

    @Test
    void send_ShouldResolveThymeleafLoopTemplate_WhenListOfOrdersProvided() {
        // Arrange
        EmailTemplate template = new EmailTemplate();
        template.setCode("ORDER_APPROVAL");
        template.setSubject("[[${toplamSiparisSayisi}]] adet sipariş onayınızı bekliyor");
        template.setBody("""
                <table>
                    <tr th:each="siparis : ${siparisler}">
                        <td th:text="${siparis.siparisTipi}">Tip</td>
                        <td th:text="${siparis.depoAdi}">Depo</td>
                        <td th:text="${siparis.siparisTutari}">Tutar</td>
                    </tr>
                </table>
                <p>Toplam: [[${toplamTutar}]]</p>
                """);

        List<Map<String, String>> siparisler = List.of(
                Map.of("siparisTipi", "Normal Sipariş", "depoAdi", "Merkez", "siparisTutari", "₺15.000"),
                Map.of("siparisTipi", "Acil Sipariş", "depoAdi", "Şube", "siparisTutari", "₺30.000")
        );

        Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("toplamSiparisSayisi", "2");
        parameters.put("siparisler", siparisler);
        parameters.put("toplamTutar", "₺45.000");

        String expectedBody = """
                <table>
                    <tr>
                        <td>Normal Sipariş</td>
                        <td>Merkez</td>
                        <td>₺15.000</td>
                    </tr>
                    <tr>
                        <td>Acil Sipariş</td>
                        <td>Şube</td>
                        <td>₺30.000</td>
                    </tr>
                </table>
                <p>Toplam: ₺45.000</p>
                """;

        EmailEventDto emailEventDto = createTemplateEmailEventDto("ORDER_APPROVAL", parameters);

        when(emailTemplateService.findActiveByCode("ORDER_APPROVAL")).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(eq(template.getSubject()), anyMap())).thenReturn("2 adet sipariş onayınızı bekliyor");
        when(thymeleafTemplateService.processTemplate(eq(template.getBody()), anyMap())).thenReturn(expectedBody);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        smtpEmailSender.send(emailEventDto);

        // Assertions
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(thymeleafTemplateService, times(2)).processTemplate(anyString(), anyMap());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("Prefix: 2 adet sipariş onayınızı bekliyor", sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("Normal Sipariş"));
        assertTrue(sentMessage.getText().contains("Acil Sipariş"));
        assertTrue(sentMessage.getText().contains("₺45.000"));
    }
}
