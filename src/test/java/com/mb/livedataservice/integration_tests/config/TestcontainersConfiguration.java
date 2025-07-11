package com.mb.livedataservice.integration_tests.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public ElasticsearchContainer elasticsearch() {
        ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("elasticsearch:8.17.1"))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false")
                .withEnv("cluster.name", "elasticsearch")
                .withReuse(true);

        elasticsearchContainer.start();

        System.setProperty("spring.elasticsearch.uris", elasticsearchContainer.getHttpHostAddress());

        return elasticsearchContainer;
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgres() {
        try (var postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.2")) {
            postgreSQLContainer.withReuse(true);
            postgreSQLContainer.start();

            assertThat(postgreSQLContainer.isCreated()).isTrue();
            assertThat(postgreSQLContainer.isRunning()).isTrue();

            return postgreSQLContainer;
        }
    }

    @Bean
    public GenericContainer<?> redis() {
        GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
                .withExposedPorts(6379)
                .withReuse(true);

        redisContainer.start();

        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString());

        return redisContainer;
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafka() {
        KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.0.0"))
                .withReuse(true);

        kafkaContainer.start();

        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.consumer.bootstrap-servers", kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.producer.bootstrap-servers", kafkaContainer.getBootstrapServers());

        return kafkaContainer;
    }
}
