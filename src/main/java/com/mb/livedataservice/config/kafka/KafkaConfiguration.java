package com.mb.livedataservice.config.kafka;

import com.mb.livedataservice.util.KafkaTopics;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.ByteArrayJacksonJsonMessageConverter;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
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
    @Primary
    public ConcurrentKafkaListenerContainerFactory<@NonNull Object, @NonNull Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<@NonNull Object, @NonNull Object> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        concurrentKafkaListenerContainerFactory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps()));
        concurrentKafkaListenerContainerFactory.setCommonErrorHandler(getDefaultErrorHandler());
        concurrentKafkaListenerContainerFactory.setRecordMessageConverter(new ByteArrayJacksonJsonMessageConverter());
        concurrentKafkaListenerContainerFactory.setBatchMessageConverter(new BatchMessagingMessageConverter());
        return concurrentKafkaListenerContainerFactory;
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        return props;
    }

    private DefaultErrorHandler getDefaultErrorHandler() {
        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler((consumerRecord, exception) -> log.error("Error occurred while processing event. getDefaultErrorHandler - consumerRecord: {}, exception: {}", consumerRecord, ExceptionUtils.getStackTrace(exception)), new FixedBackOff(5000L, 3L));

        defaultErrorHandler.addRetryableExceptions(SocketTimeoutException.class);
        defaultErrorHandler.addNotRetryableExceptions(NullPointerException.class, ValidationException.class, MethodArgumentNotValidException.class);

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
    public NewTopic testTopic() {
        return new NewTopic(KafkaTopics.TEST_TOPIC, 3, (short) 1);
    }
}
