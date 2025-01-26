package com.mb.livedataservice.integration_tests.config;

import com.mb.livedataservice.integration_tests.containers.DefaultElasticsearchContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.0"));
    }

    @Bean
    public PostgreSQLContainer<?> postgres() {
        PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.1");
        postgreSQLContainer.start();

        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();

        return postgreSQLContainer;
    }

    @Bean
    public GenericContainer<?> redis() {
        GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.4")).withExposedPorts(6379);

        redisContainer.start();

        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString());

        return redisContainer;
    }

    @Bean
    public KafkaContainer kafka() {
        KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

        kafkaContainer.start();

        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());

        return kafkaContainer;
    }

    @Bean
    public ElasticsearchContainer defaultElasticsearchContainer() {
        return new DefaultElasticsearchContainer();
    }

    @Bean
    public ElasticsearchContainer elasticsearchContainer() {
        ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.12.1"))
                .withEnv("xpack.security.enabled", "false");

        elasticsearchContainer.start();

        System.setProperty("spring.data.elasticsearch.cluster-nodes", elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));
        System.setProperty("spring.data.elasticsearch.repositories.enabled", "true");

        return elasticsearchContainer;
    }
}
