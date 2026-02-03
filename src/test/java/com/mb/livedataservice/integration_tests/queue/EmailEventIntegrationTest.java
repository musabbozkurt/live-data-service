package com.mb.livedataservice.integration_tests.queue;

import com.mb.livedataservice.data.model.EmailEvent;
import com.mb.livedataservice.data.repository.EmailEventRepository;
import com.mb.livedataservice.enums.EmailStatus;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.queue.dto.EmailEventDto;
import com.mb.livedataservice.queue.producer.impl.EmailEventProducer;
import jakarta.mail.internet.MimeMessage;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {TestcontainersConfiguration.class},
        properties = {
                "namespace=integration_test",
                "email.retry.backoff-delay=100"
        }
)
class EmailEventIntegrationTest {

    @Autowired
    private EmailEventProducer emailEventProducer;

    @Autowired
    private EmailEventRepository emailEventRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void produce_ShouldSendEmailAndSaveEvent_WhenValidEventProvided() {
        // Arrange
        EmailEventDto eventDto = createValidEventDto();
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        // Act
        emailEventProducer.produce(eventDto);

        // Assertions
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    verify(javaMailSender, times(1)).send(any(MimeMessage.class));

                    Optional<EmailEvent> savedEvent = emailEventRepository.findById(eventDto.getId());
                    assertThat(savedEvent).isPresent();
                    savedEvent.ifPresent(event -> {
                        assertThat(event.getStatus()).isEqualTo(EmailStatus.SENT);
                        assertThat(event.getSubject()).isEqualTo("Test Subject");
                    });
                });
    }

    @Test
    void produce_ShouldRetryAndProcessDLT_WhenEmailSenderFails() {
        // Arrange
        EmailEventDto eventDto = createValidEventDto();
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doThrow(new MailSendException("Email service unavailable")).when(javaMailSender).send(any(MimeMessage.class));

        // Act
        emailEventProducer.produce(eventDto);

        // Assertions
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    verify(javaMailSender, Mockito.atLeast(5)).send(any(MimeMessage.class));

                    Optional<EmailEvent> savedEvent = emailEventRepository.findById(eventDto.getId());
                    assertThat(savedEvent).isPresent();
                    savedEvent.ifPresent(event -> {
                        assertThat(event.getStatus()).isEqualTo(EmailStatus.FAILED);
                        assertThat(event.getRetryCount()).isEqualTo(1);
                    });
                });
    }

    private EmailEventDto createValidEventDto() {
        EmailEventDto eventDto = new EmailEventDto();
        eventDto.setId(UUID.randomUUID());
        eventDto.setTo(Set.of("test@example.com"));
        eventDto.setSubject("Test Subject");
        eventDto.setBody("Test Body");
        return eventDto;
    }
}
