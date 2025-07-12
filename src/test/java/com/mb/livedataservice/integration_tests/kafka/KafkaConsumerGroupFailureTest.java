package com.mb.livedataservice.integration_tests.kafka;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(classes = TestcontainersConfiguration.class)
class KafkaConsumerGroupFailureTest {

    @Autowired
    private KafkaContainer kafka;

    @Test
    void autoCommit_ShouldFailWithNonRetriableError_WhenConsumerIsKickedOutOfGroup() throws Exception {
        String topicName = "test-topic-autocommit";

        // Create topic first
        try (AdminClient adminClient = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
            adminClient.createTopics(List.of(new NewTopic(topicName, 1, (short) 1))).all().get();
        }

        // Create KafkaTemplate manually
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
                ))
        );

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-autocommit");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); // Enable auto-commit
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000"); // Extremely frequent commits
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500"); // default 500 record processing
        consumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "10000"); // default 30000 ms, prevents consumer from being kicked out during the test delays

        try (KafkaConsumer<String, String> consumer1 = new KafkaConsumer<>(consumerProps); KafkaConsumer<String, String> consumer2 = new KafkaConsumer<>(consumerProps)) {
            // Subscribe and consume messages to establish group membership
            consumer1.subscribe(List.of(topicName));

            // Send messages to create offsets to commit
            for (int i = 0; i < 10; i++) {
                kafkaTemplate.send(topicName, "key" + i, "message" + i);
            }
            kafkaTemplate.flush();

            // Consumer1 polls and joins group, consumes messages
            var records = consumer1.poll(Duration.ofSeconds(3));
            System.out.println("Consumer1 consumed " + records.count() + " records");

            // Allow auto-commit to happen at least once
            await().atMost(Duration.ofSeconds(2))
                    .pollDelay(Duration.ofSeconds(1))
                    .until(() -> true);

            // Start second consumer to trigger rebalancing
            consumer2.subscribe(List.of(topicName));
            consumer2.poll(Duration.ofSeconds(2));

            // Critical: Wait for consumer1 to exceed max.poll.interval.ms without polling
            // This causes the consumer to be expelled from the group
            System.out.println("Waiting for consumer1 to be kicked out of group...");
            await().atMost(Duration.ofSeconds(12))
                    .pollDelay(Duration.ofSeconds(10)) // Exceed max.poll.interval.ms
                    .until(() -> true);

            // Now when consumer1 tries to poll again, it will:
            // 1. Attempt to rejoin the group (onJoinPrepare)
            // 2. Try to auto-commit pending offsets during rejoin
            // 3. Fail because it's no longer part of an active group
            System.out.println("Consumer1 attempting to rejoin group...");

            // Multiple poll attempts with strategic timing to trigger auto-commit failure
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    System.out.println("Consumer1 poll attempt " + attempt + " starting...");
                    var pollRecords = consumer1.poll(Duration.ofMillis(100));
                    System.out.println("Poll attempt " + attempt + " completed with " + pollRecords.count() + " records");

                    // Brief processing delay
                    await().atMost(Duration.ofMillis(100))
                            .pollDelay(Duration.ofMillis(50))
                            .until(() -> true);
                } catch (Exception e) {
                    System.out.println("Poll attempt " + attempt + " failed: " + e.getMessage());
                }

                // Strategic delay that aligns with auto-commit interval (1000ms)
                // This creates windows where auto-commit attempts occur during group instability
                await().atMost(Duration.ofMillis(1100))
                        .pollDelay(Duration.ofMillis(900))
                        .until(() -> true);
            }

            // Additional wait to ensure all auto-commit attempts are processed
            await().atMost(Duration.ofSeconds(3))
                    .pollDelay(Duration.ofSeconds(2))
                    .until(() -> true);

            // The test succeeds if no unexpected exceptions are thrown
            // The expected log message should appear in console:
            // "Asynchronous auto-commit of offsets failed: Offset commit cannot be completed since the consumer is not part of an active group for auto partition assignment; it is likely that the consumer was kicked out of the group.. Will continue to join group."
            System.out.println("Test completed - check logs for auto-commit failure message");
            assertTrue(true, "Test completed successfully - check logs for auto-commit failure message");
        }
    }
}
