package com.mb.livedataservice.integration_tests.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /**
     * Configures an Elasticsearch testcontainers with the analysis-icu plugin installed.
     * <p>
     * The analysis-icu plugin provides support for ICU (International Components for Unicode) analysis,
     * including the {@code icu_collation_keyword} field type for language-specific sorting.
     * <p>
     * <b>Collations</b> are used for sorting documents in a language-specific word order.
     * The {@code icu_collation_keyword} field type encodes terms directly as bytes in a doc values
     * field and a single indexed token, similar to a standard Keyword field.
     * <p>
     * <b>Example mapping usage:</b>
     * <pre>{@code
     * {
     *   "mappings": {
     *     "properties": {
     *       "name": {
     *         "type": "text",
     *         "fields": {
     *           "sort": {
     *             "type": "icu_collation_keyword",
     *             "index": false,
     *             "language": "de",
     *             "country": "DE",
     *             "variant": "@collation=phonebook"
     *           }
     *         }
     *       }
     *     }
     *   }
     * }
     * }</pre>
     *
     * @return the configured Elasticsearch container with analysis-icu plugin
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-icu.html">Elasticsearch ICU Analysis Plugin</a>
     */
    @Bean
    @ServiceConnection
    public ElasticsearchContainer elasticsearch() {
        try (var elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:9.2.2"))) {
            elasticsearchContainer.withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("xpack.security.http.ssl.enabled", "false")
                    .withEnv("cluster.name", "elasticsearch")
                    .withCommand("sh", "-c", "bin/elasticsearch-plugin install analysis-icu && exec /usr/local/bin/docker-entrypoint.sh elasticsearch")
                    .withReuse(true);
            elasticsearchContainer.start();

            assertThat(elasticsearchContainer.isCreated()).isTrue();
            assertThat(elasticsearchContainer.isRunning()).isTrue();

            return elasticsearchContainer;
        }
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgres() {
        try (var postgreSQLContainer = new PostgreSQLContainer("postgres:17.2")) {
            postgreSQLContainer.withReuse(true);
            postgreSQLContainer.start();

            // Set default schema for JDBC connections
            System.setProperty("spring.datasource.hikari.schema", "mb_test");

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
        System.setProperty("redisson.url", "redis://%s:%d".formatted(redisContainer.getHost(), redisContainer.getMappedPort(6379)));

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

    @Bean
    public GenericContainer<?> artemis() {
        GenericContainer<?> artemis = new GenericContainer<>(DockerImageName.parse("apache/activemq-artemis:latest-alpine"))
                .withEnv("ANONYMOUS_LOGIN", "true")
                .withExposedPorts(61616)
                .withReuse(true);

        artemis.start();

        System.setProperty("spring.artemis.broker-url", "tcp://%s:%d".formatted(artemis.getHost(), artemis.getMappedPort(61616)));

        return artemis;
    }
}
