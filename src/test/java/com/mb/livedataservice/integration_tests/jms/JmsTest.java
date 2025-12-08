package com.mb.livedataservice.integration_tests.jms;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.queue.consumer.JmsOrderListener;
import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.queue.producer.impl.ProducerServiceImpl;
import com.mb.livedataservice.util.Topics;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest(classes = TestcontainersConfiguration.class)
class JmsTest {

    @Autowired
    private JmsClient jmsClient;

    @Autowired
    private ProducerServiceImpl producerService;

    @MockitoSpyBean
    private JmsOrderListener jmsOrderListener;

    @Test
    void sendMessage_ShouldReceiveMessage_WhenMessageIsSent() {
        // Arrange
        String destination = Topics.JMS_CUSTOM_ORDERS.concat("test");
        String message = "Hello, JMS!";

        // Act
        jmsClient.destination(destination).send(message);

        // Assertions
        jmsClient.destination(destination)
                .receive()
                .ifPresent(received -> {
                    log.info("Received message: {}", received);
                    assertThat(received).isNotNull();
                    assertThat(received.getPayload()).isEqualTo(message);
                });
    }

    @Test
    void publishJmsMessage_ShouldTriggerListener_WhenProducerServiceIsCalled() {
        // Arrange
        // Act
        producerService.publishJmsMessage();

        // Assertions
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(jmsOrderListener, atLeastOnce()).receiveOrder(any(Order.class)));
    }

    @Test
    void receiveOrder_ShouldProcessOrder_WhenOrderIsSentToDestination() {
        // Arrange
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 500);

        // Act
        jmsClient.destination(Topics.JMS_CUSTOM_ORDERS).send(order);

        // Assertions
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(jmsOrderListener).receiveOrder(any(Order.class)));
    }

    @Test
    void receiveOrder_ShouldProcessOrder_WhenMessageIsSentWithOptions() {
        // Arrange
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 600);

        // Act
        jmsClient.destination(Topics.JMS_CUSTOM_ORDERS)
                .withTimeToLive(300000)
                .withPriority(9)
                .send(order);

        // Assertions
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(jmsOrderListener, atLeastOnce()).receiveOrder(any(Order.class)));
    }
}
