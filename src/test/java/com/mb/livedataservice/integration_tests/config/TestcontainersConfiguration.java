package com.mb.livedataservice.integration_tests.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers(CustomContainers.class)
public class TestcontainersConfiguration {

    /**
     * Ensures Flyway clean+migrate runs only once per test suite execution,
     * regardless of how many Spring contexts are created. This prevents one
     * context's clean() from dropping tables needed by another cached context.
     */
    private static final AtomicBoolean flywayInitialized = new AtomicBoolean(false);

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
     * Flyway migration strategy that cleans and migrates only on the first context initialization.
     * Subsequent contexts (e.g., @DataJdbcTest, @DataJpaTest slices) just run migrate()
     * which is a no-op since all migrations are already applied. This prevents one context's
     * clean() from destroying tables needed by other cached contexts sharing the same DB.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            if (flywayInitialized.compareAndSet(false, true)) {
                flyway.clean();
                flyway.migrate();
            } else {
                flyway.migrate();
            }
        };
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
