package com.mb.livedataservice.integration_tests.jms;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Enable this test when the following issue is fixed, Free storage space is at 10.7GB of 177.2GB total. Usage rate is 94.0% which is beyond the configured <max-disk-usage>. System will start blocking producers")
@SpringBootTest(classes = TestcontainersConfiguration.class)
class JmsTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    void sendMessage() throws JMSException {
        jmsTemplate.convertAndSend("testQueue", "Hello, JMS!");

        Message message = jmsTemplate.receive("testQueue");

        assertThat(message).isInstanceOf(TextMessage.class);
        TextMessage textMessage = (TextMessage) message;
        assertThat(textMessage).isNotNull();
        assertThat(textMessage.getText()).isEqualTo("Hello, JMS!");
    }
}
