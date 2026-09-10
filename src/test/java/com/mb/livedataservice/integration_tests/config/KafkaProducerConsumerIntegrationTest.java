package com.mb.livedataservice.integration_tests.config;

import com.mb.livedataservice.config.KafkaConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration tests for the shared {@link KafkaConfig} using a real Kafka broker
 * (Testcontainers). Exercises the producer and the consumer error-handling contract for both
 * successful and failing scenarios:
 * <ul>
 *   <li>Producer: successful publish (offset metadata) and serialization failure propagation.</li>
 *   <li>Consumer success: message is delivered and processed exactly once.</li>
 *   <li>Consumer non-retryable failure: {@code defaultFalse()} means no retry, record is skipped,
 *       the partition keeps flowing (no head-of-line blocking, no data loss into an infinite loop).</li>
 *   <li>Consumer retryable failure: {@code SocketTimeoutException} is retried (bounded by the
 *       configured attempts) and then skipped, after which the partition keeps flowing.</li>
 * </ul>
 */
@Slf4j
@SpringBootTest(properties = {
        "spring.kafka.consumer.concurrency=1",
        "spring.kafka.consumer.retry.attempts=3",
        "spring.kafka.consumer.retry.backoff-ms=200",
}, classes = TestcontainersConfiguration.class)
@DisplayName("Kafka Producer/Consumer Integration Tests")
@ContextConfiguration(classes = {
        KafkaConfig.class,
        ValidationAutoConfiguration.class,
        KafkaProducerConsumerIntegrationTest.TestMessageListener.class
})
class KafkaProducerConsumerIntegrationTest {

    private static final String PROCESS_TOPIC = "test-kafka-it-process";
    private static final String PRODUCER_TOPIC = "test-kafka-it-producer";

    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL_RETRYABLE = "FAIL_RETRYABLE";
    private static final String FAIL_FATAL = "FAIL_FATAL";

    private static final int CONFIGURED_ATTEMPTS = 3;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static String uniqueId() {
        return UUID.randomUUID().toString();
    }

    // ==================== Producer ====================

    private static int invocationCount(String id) {
        AtomicInteger counter = TestMessageListener.INVOCATIONS.get(id);
        return counter == null ? 0 : counter.get();
    }

    // ==================== Consumer success ====================

    private static void awaitUntil(Duration timeout, BooleanSupplier condition) {
        await()
                .atMost(timeout)
                .pollInterval(Duration.ofMillis(100))
                .until(condition::getAsBoolean);
    }

    // ==================== Consumer failure ====================

    @BeforeEach
    void resetState() {
        TestMessageListener.INVOCATIONS.clear();
        TestMessageListener.PROCESSED.clear();
    }

    // ==================== Helpers ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestEvent implements Serializable {
        private String id;
        private String behavior;
    }

    /**
     * Payload whose serialization always fails, used to verify producer error propagation.
     */
    static class UnserializablePayload {
        @SuppressWarnings("unused")
        public String getBoom() {
            throw new IllegalStateException("intentional serialization failure");
        }
    }

    @Slf4j
    @Component
    static class TestMessageListener {

        static final Map<String, AtomicInteger> INVOCATIONS = new ConcurrentHashMap<>();
        static final Set<String> PROCESSED = ConcurrentHashMap.newKeySet();

        @KafkaListener(topics = PROCESS_TOPIC, groupId = "test-kafka-it", containerFactory = "kafkaListenerContainerFactory")
        void onMessage(TestEvent event) throws SocketTimeoutException {
            INVOCATIONS.computeIfAbsent(event.getId(), _ -> new AtomicInteger()).incrementAndGet();

            switch (event.getBehavior()) {
                case FAIL_RETRYABLE ->
                        throw new SocketTimeoutException("simulated transient failure for " + event.getId());
                case FAIL_FATAL -> throw new IllegalStateException("simulated fatal failure for " + event.getId());
                default -> PROCESSED.add(event.getId());
            }
        }
    }

    // ==================== Test fixtures ====================

    @Nested
    @DisplayName("Producer")
    class ProducerTests {

        @Test
        @DisplayName("Should publish successfully and return record metadata with a valid offset")
        void send_ShouldReturnMetadata_WhenPayloadIsValid() throws Exception {
            // Arrange
            TestEvent event = new TestEvent(uniqueId(), SUCCESS);

            // Act
            SendResult<String, Object> result = kafkaTemplate.send(PRODUCER_TOPIC, event.getId(), event).get(15, TimeUnit.SECONDS);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getRecordMetadata());
            assertEquals(PRODUCER_TOPIC, result.getRecordMetadata().topic());
            assertTrue(result.getRecordMetadata().offset() >= 0);
        }

        @Test
        @DisplayName("Should fail when the payload cannot be serialized")
        void send_ShouldFail_WhenPayloadIsNotSerializable() {
            // Serialization happens on the producer path; the failure must surface to the caller
            // (either synchronously from send(...) or via the returned future).
            assertThrows(Exception.class, () -> kafkaTemplate.send(PRODUCER_TOPIC, "bad", new UnserializablePayload()).get(15, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("Consumer - success")
    class ConsumerSuccessTests {

        @Test
        @DisplayName("Should consume and process a message exactly once")
        void listener_ShouldProcessOnce_WhenMessageSucceeds() {
            // Arrange
            String id = uniqueId();

            // Act
            kafkaTemplate.send(PROCESS_TOPIC, id, new TestEvent(id, SUCCESS));

            // Assert
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(id));
            assertEquals(1, invocationCount(id));
        }
    }

    @Nested
    @DisplayName("Consumer - failure handling")
    class ConsumerFailureTests {

        @Test
        @DisplayName("Non-retryable failure is not retried, is skipped, and the partition keeps flowing")
        void listener_ShouldSkipWithoutRetry_WhenExceptionIsNotRetryable() {
            // Arrange
            String poisonId = uniqueId();

            // Act — a fatal (non-retryable) exception; defaultFalse() means zero retries.
            kafkaTemplate.send(PROCESS_TOPIC, poisonId, new TestEvent(poisonId, FAIL_FATAL));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.INVOCATIONS.containsKey(poisonId));

            // A subsequent message must still be processed → the poison record did not block the partition.
            String goodId = uniqueId();
            kafkaTemplate.send(PROCESS_TOPIC, goodId, new TestEvent(goodId, SUCCESS));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(goodId));

            // Assert — exactly one invocation, i.e. it was skipped without retry.
            assertEquals(1, invocationCount(poisonId));
        }

        @Test
        @DisplayName("Retryable failure is retried (bounded) then skipped, and the partition keeps flowing")
        void listener_ShouldRetryThenSkip_WhenExceptionIsRetryable() {
            // Arrange
            String flakyId = uniqueId();

            // Act — SocketTimeoutException is registered as retryable in the error handler.
            kafkaTemplate.send(PROCESS_TOPIC, flakyId, new TestEvent(flakyId, FAIL_RETRYABLE));

            // At least one retry must occur (more than the initial delivery).
            awaitUntil(Duration.ofSeconds(30), () -> invocationCount(flakyId) >= 2);

            // A subsequent message is processed → retries were bounded and the record was eventually skipped.
            String goodId = uniqueId();
            kafkaTemplate.send(PROCESS_TOPIC, goodId, new TestEvent(goodId, SUCCESS));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(goodId));

            // Assert — retried but capped: initial delivery + up to CONFIGURED_ATTEMPTS retries.
            int total = invocationCount(flakyId);
            assertTrue(total >= 2, "expected at least one retry, but was " + total);
            assertTrue(total <= CONFIGURED_ATTEMPTS + 1, "retries must be bounded by configured attempts (max " + (CONFIGURED_ATTEMPTS + 1) + "), but was " + total);
        }
    }
}
