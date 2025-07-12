package com.mb.livedataservice.integration_tests.kafka;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.util.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@Slf4j
@Testcontainers
@SpringBootTest(classes = TestcontainersConfiguration.class)
class KafkaDeadLetterPublishingTest {

    private static final String ORDERS_DLT = KafkaTopics.ORDERS + "-dlt";
    private static KafkaConsumer<String, String> kafkaConsumer;

    @Autowired
    private KafkaOperations<String, Order> operations;

    @BeforeAll
    static void setup(@Autowired KafkaContainer kafka) {
        // Create a test consumer that handles <String, String> records, listening to orders.DLT
        // https://docs.spring.io/spring-kafka/docs/3.0.x/reference/html/#testing
        var consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-consumer", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConsumer = new KafkaConsumer<>(consumerProps);
        kafkaConsumer.subscribe(List.of(ORDERS_DLT));
    }

    @AfterAll
    static void close() {
        // Close the consumer before shutting down Testcontainers Kafka instance
        kafkaConsumer.close();
    }

    @Test
    void orderProcessing_ShouldNotProduceOntoDlt_WhenMessageIsValid() {
        // Send in valid order
        Order order = new Order(randomUUID(), randomUUID(), 1);
        operations.send("orders", order.orderId().toString(), order)
                .whenCompleteAsync((result, ex) -> {
                    if (ex == null) {
                        log.info("Success: {}", result);
                    } else {
                        log.error("Failure ", ex);
                    }
                });

        // Verify no message was produced onto Dead Letter Topic
        assertThrowsExactly(
                IllegalStateException.class,
                () -> KafkaTestUtils.getSingleRecord(kafkaConsumer, ORDERS_DLT, Duration.ofSeconds(5)),
                "No records found for topic");
    }

    @Test
    void orderProcessing_ShouldProduceOntoDlt_WhenMessageIsInvalid() {
        // Amount can not be negative, validation will fail
        Order order = new Order(randomUUID(), randomUUID(), -2);
        operations.send("orders", order.orderId().toString(), order)
                .whenCompleteAsync((result, ex) -> {
                    if (ex == null) {
                        log.info("Success: {}", result);
                    } else {
                        log.error("Failure ", ex);
                    }
                });

        // Verify message produced onto Dead Letter Topic
        ConsumerRecord<String, String> consumerRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, ORDERS_DLT, Duration.ofSeconds(2));

        // Verify headers present, and single header value
        Headers headers = consumerRecord.headers();
        assertThat(headers).map(Header::key).containsAll(List.of(
                "kafka_dlt-exception-fqcn",
                "kafka_dlt-exception-cause-fqcn",
                "kafka_dlt-exception-message",
                "kafka_dlt-exception-stacktrace",
                "kafka_dlt-original-topic",
                "kafka_dlt-original-partition",
                "kafka_dlt-original-offset",
                "kafka_dlt-original-timestamp",
                "kafka_dlt-original-timestamp-type",
                "kafka_dlt-original-consumer-group"));
        assertThat(new String(headers.lastHeader("kafka_dlt-exception-fqcn").value()))
                .isEqualTo("org.springframework.kafka.listener.ListenerExecutionFailedException");
        assertThat(new String(headers.lastHeader("kafka_dlt-exception-cause-fqcn").value()))
                .isEqualTo("org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException");
        assertThat(new String(headers.lastHeader("kafka_dlt-exception-message").value()))
                .contains("Listener method could not be invoked with the incoming message");

        // Verify payload value matches sent in order
        assertThat(consumerRecord.value()).isEqualToIgnoringWhitespace("""
                { "orderId": "%s", "articleId": "%s", "amount":-2 }""".formatted(order.orderId(), order.articleId()));
    }
}
