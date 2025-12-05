package com.mb.livedataservice.integration_tests.kafka;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.util.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@Slf4j
@SpringBootTest(classes = TestcontainersConfiguration.class)
class KafkaDeadLetterPublishingTest {

    private static final String ORDERS_DLT = KafkaTopics.ORDERS + "-dlt";
    private static KafkaConsumer<String, String> kafkaConsumer;

    @Autowired
    private KafkaOperations<@NonNull String, @NonNull Object> kafkaOperations;

    @BeforeAll
    static void setup(@Autowired KafkaContainer kafka) {
        // Create a test consumer that handles <String, String> records, listening to orders.DLT
        // https://docs.spring.io/spring-kafka/docs/3.0.x/reference/html/#testing
        var consumerProps = KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-consumer", true);
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
    void kafkaProducer_ShouldHaveCorrectConfiguration_WhenConfigured() {
        var configs = kafkaOperations.getProducerFactory().getConfigurationProperties();

        // Verify producer is configured with acks=all for durability
        assertThat(configs).containsEntry(ProducerConfig.ACKS_CONFIG, "all")
                // Verify producer maintains message ordering with max in flight requests = 1
                .containsEntry(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
                // Verify producer has idempotence enabled to prevent duplicate messages
                .containsEntry(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    }

    @Test
    void orderProcessing_ShouldNotProduceOntoDlt_WhenMessageIsValid() {
        // Send in valid order
        Order order = new Order(randomUUID(), randomUUID(), 1);
        kafkaOperations.send("orders", order.orderId().toString(), order)
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
}
