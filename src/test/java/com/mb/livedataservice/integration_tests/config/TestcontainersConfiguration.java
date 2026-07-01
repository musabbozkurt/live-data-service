package com.mb.livedataservice.integration_tests.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers(CustomContainers.class)
public class TestcontainersConfiguration {

    static {
        // Force Testcontainers reuse configuration early
        org.testcontainers.utility.TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", "true");

        // 1. Explicitly start all core container infrastructure objects
        CustomContainers.artemisContainer.start();
        CustomContainers.kafkaContainer.start();
        CustomContainers.postgresContainer.start();
        CustomContainers.redisContainer.start();

        // 2. Start secondary Redis servers for dynamic clusters y/z.
        try {
            CustomContainers.redisContainer.execInContainer("redis-server", "--port", "6380", "--daemonize", "yes");
            CustomContainers.redisContainer.execInContainer("redis-server", "--port", "6381", "--daemonize", "yes");
        } catch (Exception e) {
            throw new RuntimeException("Failed to start secondary Redis servers on ports 6380/6381", e);
        }

        // 3. Inject explicit core environment fallback parameters
        System.setProperty("spring.artemis.broker-url", "tcp://%s:%d".formatted(CustomContainers.artemisContainer.getHost(), CustomContainers.artemisContainer.getMappedPort(61616)));

        System.setProperty("spring.kafka.bootstrap-servers", CustomContainers.kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.consumer.bootstrap-servers", CustomContainers.kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.producer.bootstrap-servers", CustomContainers.kafkaContainer.getBootstrapServers());

        String redisHost = CustomContainers.redisContainer.getHost();
        int redisContainerMappedPort = CustomContainers.redisContainer.getMappedPort(6379);
        System.setProperty("spring.data.redis.host", redisHost);
        System.setProperty("spring.data.redis.port", String.valueOf(redisContainerMappedPort));
        System.setProperty("redisson.url", "redis://%s:%d".formatted(redisHost, redisContainerMappedPort));
    }

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

    // This initializer block now securely pulls ready-mapped ports
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            CustomContainers.redisContainer.start();
            String redisHost = CustomContainers.redisContainer.getHost();
            int portX = CustomContainers.redisContainer.getMappedPort(6379);
            int portY = CustomContainers.redisContainer.getMappedPort(6380);
            int portZ = CustomContainers.redisContainer.getMappedPort(6381);

            TestPropertyValues.of(
                    "redis.clusters.x.host=%s".formatted(redisHost),
                    "redis.clusters.x.port=%d".formatted(portX),
                    "redis.clusters.x.primary=%s".formatted(false),
                    "redis.clusters.y.host=%s".formatted(redisHost),
                    "redis.clusters.y.port=%d".formatted(portY),
                    "redis.clusters.z.host=%s".formatted(redisHost),
                    "redis.clusters.z.port=%d".formatted(portZ)
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
