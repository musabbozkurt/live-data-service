package com.mb.livedataservice.integration_tests.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import java.sql.Connection;
import java.sql.DriverManager;

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

        // Pre-create the schema so Flyway clean() and currentSchema work on fresh containers (CI)
        try (Connection conn = DriverManager.getConnection(
                CustomContainers.postgresContainer.getJdbcUrl(),
                CustomContainers.postgresContainer.getUsername(),
                CustomContainers.postgresContainer.getPassword())) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS mb_test");
        } catch (Exception e) {
            throw new RuntimeException("Failed to pre-create mb_test schema", e);
        }

        System.setProperty("spring.data.redis.host", CustomContainers.redisContainer.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(CustomContainers.redisContainer.getMappedPort(6379)));

        System.setProperty("redisson.url", "redis://%s:%s".formatted(CustomContainers.redisContainer.getHost(), CustomContainers.redisContainer.getMappedPort(6379)));
    }

    /**
     * For reused containers: clean() resets the schema to a known state, then migrate()
     * applies all migrations. The mb_test schema is pre-created in the static block above
     * to ensure clean() works on fresh containers (CI).
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
