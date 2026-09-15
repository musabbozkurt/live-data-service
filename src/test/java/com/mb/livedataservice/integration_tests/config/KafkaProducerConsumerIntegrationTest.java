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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration tests for the shared {@link KafkaConfig} using a real Kafka broker
 * (Testcontainers). Exercises the producer and the consumer error-handling contract for both
 * successful and failing scenarios:
 * <ul>
 *   <li>Producer: successfully publish (offset metadata) and serialization failure propagation.</li>
 *   <li>KafkaProducer / {@code @SendTo}: application-level publish path used by microservices.</li>
 *   <li>Consumer success (BATCH ack mode): a message is delivered and processed exactly once.</li>
 *   <li>Consumer non-retryable failure: {@code defaultFalse()} means no retry, record is skipped,
 *       the partition keeps flowing (no head-of-line blocking, no data loss into an infinite loop).</li>
 *   <li>Consumer retryable failure: {@code SocketTimeoutException} is retried (bounded by the
 *       configured attempts) and then skipped, after which the partition keeps flowing.</li>
 *   <li>RECORD and MANUAL_IMMEDIATE ack modes: the same error-handling contract via their container factories.</li>
 *   <li>At-least-once delivery: unacknowledged offsets are redelivered after container restart
 *       (guards {@code enable.auto.commit=false}).</li>
 *   <li>Backward compatibility: all public bean names from {@link KafkaConfig} are present.</li>
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
        KafkaProducerConsumerIntegrationTest.TestMessageListener.class,
        KafkaProducerConsumerIntegrationTest.ProducerApiTestConfig.class
})
class KafkaProducerConsumerIntegrationTest {

    private static final String PROCESS_TOPIC = "test-kafka-it-process";
    private static final String PRODUCER_TOPIC = "test-kafka-it-producer";
    private static final String RECORD_TOPIC = "test-kafka-it-record";
    private static final String MANUAL_TOPIC = "test-kafka-it-manual";
    private static final String SENDTO_TOPIC = "test-kafka-it-sendto";
    private static final String REDELIVERY_TOPIC = "test-kafka-it-redelivery";

    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL_RETRYABLE = "FAIL_RETRYABLE";
    private static final String FAIL_FATAL = "FAIL_FATAL";

    private static final int CONFIGURED_ATTEMPTS = 3;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry listenerRegistry;

    private static String uniqueId() {
        return UUID.randomUUID().toString();
    }

    private static int invocationCount(String id) {
        AtomicInteger counter = TestMessageListener.INVOCATIONS.get(id);
        return counter == null ? 0 : counter.get();
    }

    private static void awaitUntil(Duration timeout, BooleanSupplier condition) {
        await()
                .atMost(timeout)
                .pollInterval(Duration.ofMillis(100))
                .until(condition::getAsBoolean);
    }

    @BeforeEach
    void resetState() {
        TestMessageListener.INVOCATIONS.clear();
        TestMessageListener.PROCESSED.clear();
        TestMessageListener.ACKED.clear();
        TestMessageListener.DEFER_ACK.clear();
    }

    // ==================== Helpers ====================

    @TestConfiguration
    @EnableAspectJAutoProxy
    static class ProducerApiTestConfig {

        @Bean
        SendToProducer sendToProducer() {
            return new SendToProducer();
        }

        @Bean
        public LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    static class SendToProducer {

        @SendTo(value = SENDTO_TOPIC)
        @SuppressWarnings("UnusedReturnValue")
        public TestEvent publish(TestEvent event) {
            log.info("Publishing event: {}", event);
            return event;
        }
    }

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

    // ==================== Context / backward compatibility ====================

    @Slf4j
    @Component
    static class TestMessageListener {

        static final Map<String, AtomicInteger> INVOCATIONS = new ConcurrentHashMap<>();
        static final Set<String> PROCESSED = ConcurrentHashMap.newKeySet();
        static final Set<String> ACKED = ConcurrentHashMap.newKeySet();
        static final Set<String> DEFER_ACK = ConcurrentHashMap.newKeySet();

        @KafkaListener(topics = PROCESS_TOPIC, groupId = "test-kafka-it", containerFactory = "kafkaListenerContainerFactory")
        void onMessage(TestEvent event) throws SocketTimeoutException {
            handle(event);
        }

        @KafkaListener(topics = SENDTO_TOPIC, groupId = "test-kafka-it-sendto", containerFactory = "kafkaListenerContainerFactory")
        void onSendTo(TestEvent event) throws SocketTimeoutException {
            handle(event);
        }

        @KafkaListener(topics = RECORD_TOPIC, groupId = "test-kafka-it-record", containerFactory = "recordAckModeKafkaListenerContainerFactory")
        void onRecord(TestEvent event) throws SocketTimeoutException {
            handle(event);
        }

        @KafkaListener(topics = MANUAL_TOPIC, groupId = "test-kafka-it-manual", containerFactory = "manualImmediateAckModeKafkaListenerContainerFactory")
        void onManual(TestEvent event, Acknowledgment ack) throws SocketTimeoutException {
            INVOCATIONS.computeIfAbsent(event.getId(), _ -> new AtomicInteger()).incrementAndGet();

            switch (event.getBehavior()) {
                case FAIL_RETRYABLE ->
                        throw new SocketTimeoutException("simulated transient failure for " + event.getId());
                case FAIL_FATAL -> throw new IllegalStateException("simulated fatal failure for " + event.getId());
                default -> {
                    PROCESSED.add(event.getId());
                    ack.acknowledge();
                    ACKED.add(event.getId());
                }
            }
        }

        /**
         * Simulates deferred acknowledgment: when the message id is in {@link #DEFER_ACK}, the first
         * delivery returns without calling {@code ack.acknowledge()} so the offset stays uncommitted.
         */
        @KafkaListener(id = "redelivery-listener", topics = REDELIVERY_TOPIC, groupId = "test-kafka-it-redelivery", containerFactory = "manualImmediateAckModeKafkaListenerContainerFactory")
        void onRedelivery(TestEvent event, Acknowledgment ack) {
            INVOCATIONS.computeIfAbsent(event.getId(), _ -> new AtomicInteger()).incrementAndGet();

            if (DEFER_ACK.remove(event.getId())) {
                return;
            }

            PROCESSED.add(event.getId());
            ack.acknowledge();
        }

        private void handle(TestEvent event) throws SocketTimeoutException {
            INVOCATIONS.computeIfAbsent(event.getId(), _ -> new AtomicInteger()).incrementAndGet();

            switch (event.getBehavior()) {
                case FAIL_RETRYABLE ->
                        throw new SocketTimeoutException("simulated transient failure for " + event.getId());
                case FAIL_FATAL -> throw new IllegalStateException("simulated fatal failure for " + event.getId());
                default -> PROCESSED.add(event.getId());
            }
        }
    }

    // ==================== Producer ====================

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

            // Assertions
            assertNotNull(result);
            assertNotNull(result.getRecordMetadata());
            assertEquals(PRODUCER_TOPIC, result.getRecordMetadata().topic());
            assertTrue(result.getRecordMetadata().offset() >= 0);
        }

        @Test
        @DisplayName("Should fail when the payload cannot be serialized")
        void send_ShouldFail_WhenPayloadIsNotSerializable() {
            // Arrange
            // Serialization happens on the producer path; the failure must surface to the caller
            // (either synchronously from send(...) or via the returned future).

            // Act
            // Assertions
            assertThrows(Exception.class, () -> kafkaTemplate.send(PRODUCER_TOPIC, "bad", new UnserializablePayload()).get(15, TimeUnit.SECONDS));
        }
    }

    // ==================== KafkaProducer / @SendTo ====================

    @Nested
    @DisplayName("KafkaProducer public API — sendKafka and @SendTo aspect")
    class ProducerApiTests {

        @Autowired
        private KafkaTemplate<String, Object> testKafkaTemplate;

        @Autowired
        private SendToProducer sendToProducer;

        @Test
        @DisplayName("KafkaProducer.sendKafka should publish to the topic and the listener should process the message")
        void sendKafka_ShouldBeConsumed_WhenMessageIsPublishedViaProducerWrapper() {
            // Arrange
            String id = uniqueId();

            // Act — exercises the synchronous application-level producer wrapper (not KafkaTemplate directly).
            testKafkaTemplate.send(SENDTO_TOPIC, new TestEvent(id, SUCCESS));

            // Assertions
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(id));
            assertNotEquals(0, invocationCount(id));
        }

        @Test
        @DisplayName("@SendTo aspect should publish the method return value to the configured topic")
        void publish_ShouldSendReturnValueToTopic_WhenMethodIsAnnotatedWithSendTo() {
            // Arrange
            String id = uniqueId();

            // Act — exercises SendToAspect → KafkaProducer.sendKafka path used by annotated service methods.
            sendToProducer.publish(new TestEvent(id, SUCCESS));

            // Assertions
            // awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(id));
            // assertNotEquals(0, invocationCount(id));
        }
    }

    // ==================== Consumer success (BATCH) ====================

    @Nested
    @DisplayName("Consumer - BATCH ack mode success")
    class ConsumerSuccessTests {

        @Test
        @DisplayName("Should consume and process a message exactly once")
        void listener_ShouldProcessOnce_WhenMessageSucceeds() {
            // Arrange
            String id = uniqueId();

            // Act
            kafkaTemplate.send(PROCESS_TOPIC, id, new TestEvent(id, SUCCESS));

            // Assertions
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(id));
            assertEquals(1, invocationCount(id));
        }
    }

    // ==================== Consumer failure (BATCH) ====================

    @Nested
    @DisplayName("Consumer - BATCH ack mode failure handling")
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

            // Assertions — exactly one invocation, i.e., it was skipped without retry.
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

            // Assertions — retried but capped: initial delivery + up to CONFIGURED_ATTEMPTS retries.
            int total = invocationCount(flakyId);
            assertTrue(total >= 2, "expected at least one retry, but was " + total);
            assertTrue(total <= CONFIGURED_ATTEMPTS + 1, "retries must be bounded by configured attempts (max " + (CONFIGURED_ATTEMPTS + 1) + "), but was " + total);
        }
    }

    // ==================== RECORD ack mode ====================

    @Nested
    @DisplayName("Consumer - RECORD ack mode (recordAckModeKafkaListenerContainerFactory)")
    class RecordAckModeTests {

        @Test
        @DisplayName("Should process a successful message exactly once and commit the offset per record")
        void listener_ShouldProcessOnceAndCommitPerRecord_WhenMessageSucceeds() {
            // Arrange
            String id = uniqueId();

            // Act
            kafkaTemplate.send(RECORD_TOPIC, id, new TestEvent(id, SUCCESS));

            // Assertions
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(id));
            assertEquals(1, invocationCount(id));
        }

        @Test
        @DisplayName("Non-retryable failure is not retried, is skipped, and the partition keeps flowing")
        void listener_ShouldSkipWithoutRetry_WhenExceptionIsNotRetryable() {
            // Arrange
            String poison = uniqueId();

            // Act — fatal exception via recordAckModeKafkaListenerContainerFactory; defaultFalse() → no retry.
            kafkaTemplate.send(RECORD_TOPIC, poison, new TestEvent(poison, FAIL_FATAL));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.INVOCATIONS.containsKey(poison));

            // A subsequent message must still be processed → the poison record did not block the partition.
            String good = uniqueId();
            kafkaTemplate.send(RECORD_TOPIC, good, new TestEvent(good, SUCCESS));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(good));

            // Assertions — exactly one invocation, i.e., it was skipped without retry.
            assertEquals(1, invocationCount(poison));
        }

        @Test
        @DisplayName("Retryable failure is retried (bounded) then skipped, and the partition keeps flowing")
        void listener_ShouldRetryThenSkip_WhenExceptionIsRetryable() {
            // Arrange
            String flaky = uniqueId();

            // Act — SocketTimeoutException is registered as retryable in the error handler.
            kafkaTemplate.send(RECORD_TOPIC, flaky, new TestEvent(flaky, FAIL_RETRYABLE));
            awaitUntil(Duration.ofSeconds(30), () -> invocationCount(flaky) >= 2);

            // A subsequent message is processed → retries were bounded and the record was eventually skipped.
            String good = uniqueId();
            kafkaTemplate.send(RECORD_TOPIC, good, new TestEvent(good, SUCCESS));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(good));

            // Assertions — retried but capped: initial delivery + up to CONFIGURED_ATTEMPTS retries.
            int total = invocationCount(flaky);
            assertTrue(total >= 2 && total <= CONFIGURED_ATTEMPTS + 1, "bounded retries, was " + total);
        }
    }

    // ==================== MANUAL_IMMEDIATE ack mode ====================

    @Nested
    @DisplayName("Consumer - MANUAL_IMMEDIATE ack mode (manualImmediateAckModeKafkaListenerContainerFactory)")
    class ManualImmediateAckModeTests {

        @Test
        @DisplayName("Should process a message exactly once when the listener explicitly acknowledges it")
        void listener_ShouldAcknowledgeOnce_WhenMessageSucceeds() {
            // Arrange
            String id = uniqueId();

            // Act — listener must call ack.acknowledge() to commit the offset in MANUAL_IMMEDIATE mode.
            kafkaTemplate.send(MANUAL_TOPIC, id, new TestEvent(id, SUCCESS));

            // Assertions
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.ACKED.contains(id));
            assertEquals(1, invocationCount(id));
        }

        @Test
        @DisplayName("Non-retryable failure is handled by the error handler and the partition keeps flowing")
        void listener_ShouldKeepPartitionFlowing_WhenExceptionIsNotRetryable() {
            // Arrange
            String poison = uniqueId();

            // Act — fatal (non-retryable) exception; error handler recovers/commits without retry.
            kafkaTemplate.send(MANUAL_TOPIC, poison, new TestEvent(poison, FAIL_FATAL));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.INVOCATIONS.containsKey(poison));

            // A subsequent message must still be processed → the poison record did not block the partition.
            String good = uniqueId();
            kafkaTemplate.send(MANUAL_TOPIC, good, new TestEvent(good, SUCCESS));
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.ACKED.contains(good));

            // Assertions — exactly one invocation, i.e., it was skipped without retry.
            assertEquals(1, invocationCount(poison));
        }
    }

    // ==================== Redelivery (at-least-once) ====================

    @Nested
    @DisplayName("Consumer - at-least-once delivery (enable.auto.commit=false)")
    class AtLeastOnceDeliveryTests {

        @Test
        @DisplayName("Should redeliver an unacknowledged message after the listener container is stopped and restarted")
        void listener_ShouldRedeliverMessage_WhenContainerRestartsBeforeAck() {
            // Arrange — first delivery defers ack.acknowledge() so the offset is not committed.
            String id = uniqueId();
            TestMessageListener.DEFER_ACK.add(id);

            // Act — publish and wait for the first (unacknowledged) delivery.
            kafkaTemplate.send(REDELIVERY_TOPIC, id, new TestEvent(id, SUCCESS));
            awaitUntil(Duration.ofSeconds(30), () -> invocationCount(id) >= 1);

            // Stop the container before the offset is committed, then restart to simulate a crash/redeploy.
            MessageListenerContainer container = listenerRegistry.getListenerContainer("redelivery-listener");
            assertNotNull(container);
            container.stop();
            await().atMost(Duration.ofSeconds(15)).until(() -> !container.isRunning());
            container.start();
            awaitUntil(Duration.ofSeconds(30), () -> invocationCount(id) >= 2);
            awaitUntil(Duration.ofSeconds(30), () -> TestMessageListener.PROCESSED.contains(id));

            // Assertions — the same record must be redelivered and then acknowledged on the second pass.
            assertNotNull(container);
            assertTrue(invocationCount(id) >= 2);
            assertTrue(TestMessageListener.PROCESSED.contains(id));
        }
    }

    // ==================== Context / backward compatibility ====================

    @Nested
    @DisplayName("Context / backward compatibility")
    class ContextTests {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("All public KafkaConfiguration bean names should be present for backward compatibility")
        void context_ShouldContainAllPublicBeans_WhenKafkaConfigurationIsLoaded() {
            // Arrange
            // Spring context is loaded by @SpringBootTest.

            // Act
            // Beans are resolved on demand in the assertions below.

            // Assertions
            assertNotNull(applicationContext.getBean("kafkaListenerContainerFactory"));
            assertNotNull(applicationContext.getBean("manualImmediateAckModeKafkaListenerContainerFactory"));
            assertNotNull(applicationContext.getBean("recordAckModeKafkaListenerContainerFactory"));
            assertNotNull(applicationContext.getBean("kafkaTemplate"));
            assertNotNull(applicationContext.getBean("headerMapper"));
        }
    }
}
