package com.mb.livedataservice.config.kafka;

import com.mb.livedataservice.util.KafkaTopics;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.retrytopic.DestinationTopic;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory;
import org.springframework.kafka.retrytopic.SuffixingRetryTopicNamesProviderFactory;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration implements KafkaListenerConfigurer {

    private final LocalValidatorFactoryBean validator;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        // https://docs.spring.io/spring-kafka/docs/2.8.1/reference/html/#kafka-validation
        registrar.setValidator(this.validator);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaOperations<String, Object> kafkaOperations(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, Object> defaultRetryTopicKafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        factory.setCommonErrorHandler(kafkaErrorHandler());
        factory.setRecordMessageConverter(new ByteArrayJsonMessageConverter());

        return factory;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(
                (_, _) -> {
                }, new FixedBackOff(1000, 5)
        );
        defaultErrorHandler.addRetryableExceptions(SocketTimeoutException.class);
        defaultErrorHandler.addNotRetryableExceptions(NullPointerException.class);
        return defaultErrorHandler;
    }

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(KafkaTopics.ORDERS)
                // Use more than one partition for frequently used input topic
                .partitions(6)
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic(KafkaDeadLetterTopicProperties properties) {
        // https://docs.spring.io/spring-kafka/docs/2.8.1/reference/html/#configuring-topics
        return TopicBuilder.name(KafkaTopics.ORDERS + properties.getDeadletter().suffix())
                // Use only one partition for infrequently used Dead Letter Topic
                .partitions(1)
                // Use longer retention for Dead Letter Topic, allowing for more time to troubleshoot
                .config(TopicConfig.RETENTION_MS_CONFIG, "" + properties.getDeadletter().retention().toMillis())
                .build();
    }

    /// When customRetryTopic is used, the default error handler is not applied.
    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaOperations<String, Object> kafkaOperations,
                                                   KafkaDeadLetterTopicProperties properties) {
        // Publish to dead letter topic any messages dropped after retries with back off
        var recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
                // Always send to first/only partition of DLT suffixed topic
                (cr, _) -> new TopicPartition(cr.topic() + properties.getDeadletter().suffix(), 0));

        // Spread out attempts over time, taking a little longer between each attempt
        // Set a max for retries below max.poll.interval.ms; default: 5m, as otherwise we trigger a consumer rebalance
        Backoff backoff = properties.getBackoff();
        var exponentialBackOff = new ExponentialBackOffWithMaxRetries(backoff.maxRetries());
        exponentialBackOff.setInitialInterval(backoff.initialInterval().toMillis());
        exponentialBackOff.setMultiplier(backoff.multiplier());
        exponentialBackOff.setMaxInterval(backoff.maxInterval().toMillis());

        // Do not try to recover from validation exceptions when validation of orders failed
        var errorHandler = new DefaultErrorHandler(recoverer, exponentialBackOff);
        errorHandler.addNotRetryableExceptions(ValidationException.class);

        return errorHandler;
    }

    // https://github.com/spring-projects/spring-kafka/discussions/2238
    // This is used to customize the DLT topic name but not worked as expected.
    @Bean
    public RetryTopicNamesProviderFactory providerFactory() {
        return new SuffixingRetryTopicNamesProviderFactory() {
            @Override
            public RetryTopicNamesProviderFactory.RetryTopicNamesProvider createRetryTopicNamesProvider(DestinationTopic.Properties properties) {
                if (properties.isDltTopic()) {
                    return new SuffixingRetryTopicNamesProvider(properties) {
                        @Override
                        public String getTopicName(String topic) {
                            return "musab-custom-dlt";
                        }
                    };
                }
                return super.createRetryTopicNamesProvider(properties);
            }
        };
    }

    // https://docs.spring.io/spring-kafka/reference/retrytopic/dlt-strategies.html
    // https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html
    @Bean
    public RetryTopicConfiguration customRetryTopic(KafkaTemplate<?, ?> kafkaTemplate) {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .dltHandlerMethod("customDltProcessor", "processDltMessage")
                .autoStartDltHandler(true)
                .create(kafkaTemplate);
    }

    @Component("customDltProcessor")
    public static class CustomDltProcessor {

        public void processDltMessage(ConsumerRecord<?, ?> consumerRecord) {
            log.error("DLT message consumed from topic: {}, offset: {}, message: {}", consumerRecord.topic(), consumerRecord.offset(), consumerRecord.value());
        }
    }
}
