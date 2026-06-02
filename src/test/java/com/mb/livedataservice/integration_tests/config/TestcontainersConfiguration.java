package com.mb.livedataservice.integration_tests.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers(CustomContainers.class)
public class TestcontainersConfiguration {

    static {
        org.testcontainers.utility.TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", "true");

        CustomContainers.artemisContainer.start();
        CustomContainers.kafkaContainer.start();
        CustomContainers.postgresContainer.start();
        CustomContainers.redisContainer.start();

        System.setProperty("spring.artemis.broker-url", "tcp://%s:%d".formatted(CustomContainers.artemisContainer.getHost(), CustomContainers.artemisContainer.getMappedPort(61616)));

        System.setProperty("spring.kafka.bootstrap-servers", CustomContainers.kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.consumer.bootstrap-servers", CustomContainers.kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.producer.bootstrap-servers", CustomContainers.kafkaContainer.getBootstrapServers());

        System.setProperty("spring.datasource.url", CustomContainers.postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", CustomContainers.postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", CustomContainers.postgresContainer.getPassword());
        System.setProperty("spring.datasource.hikari.connection-init-sql", "SET search_path TO mb_test,public");

        System.setProperty("spring.data.redis.host", CustomContainers.redisContainer.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(CustomContainers.redisContainer.getMappedPort(6379)));

        System.setProperty("redisson.url", "redis://%s:%s".formatted(CustomContainers.redisContainer.getHost(), CustomContainers.redisContainer.getMappedPort(6379)));
    }

    /**
     * For reused containers: repair() fixes checksum mismatches in the Flyway
     * schema history without dropping tables or the schema. Then migrate()
     * applies any new migrations. Because all your migration scripts use
     * "CREATE ... IF NOT EXISTS" and "INSERT ... VALUES" (no idempotency on
     * data), we make data inserts idempotent below.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
