package com.mb.livedataservice.integration_tests.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers(disabledWithoutDocker = true)
public class TestcontainersConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    // ── Static Redis container — started once when the class is loaded ────────────────
    // Must be static so it starts BEFORE the Spring context is created.
    // Properties are injected via ApplicationContextInitializer.initialize(),
    // which runs before any bean instantiation or @Value resolution.
    //
    // Why not @DynamicPropertySource?  → Not processed in @TestConfiguration classes.
    // Why not System.setProperty?      → Spring Environment may already be populated.
    // Why not @ServiceConnection?      → Provides ConnectionDetails but does NOT set
    //                                    spring.data.redis.* properties, so CacheConfig's
    //                                    @Value("${spring.data.redis.host:localhost}")
    //                                    would still resolve to the default.

    private static final GenericContainer<?> REDIS;

    static {
        REDIS = new GenericContainer<>(DockerImageName.parse("redis:8.6.1"))
                .withExposedPorts(6379)
                .withReuse(true);

        REDIS.start();

        assertThat(REDIS.isRunning()).isTrue();
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        String host = REDIS.getHost();
        String port = REDIS.getMappedPort(6379).toString();

        // Inject as highest-priority property source — all @Value, placeholder, and
        // autoconfiguration resolutions will see these before application.yml defaults.
        ctx.getEnvironment()
                .getPropertySources()
                .addFirst(
                        new MapPropertySource(
                                "testcontainers-redis",
                                Map.of(
                                        "spring.data.redis.host", host,
                                        "spring.data.redis.port", port,
                                        "redisson.url", "redis://%s:%s".formatted(host, port)
                                )
                        )
                );
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }

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
    @ServiceConnection
    @Bean(destroyMethod = "stop") // Stop the container when all tests are done
    public ElasticsearchContainer elasticsearch() {
        var elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:9.3.1"));
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

    @ServiceConnection
    @Bean(destroyMethod = "stop")
    public PostgreSQLContainer postgres() {
        var postgreSQLContainer = new PostgreSQLContainer("postgres:18.3");
        postgreSQLContainer.withReuse(true);
        postgreSQLContainer.start();

        // Set default schema for JDBC connections
        System.setProperty("spring.datasource.hikari.schema", "mb_test");

        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();

        return postgreSQLContainer;
    }

    @ServiceConnection
    @Bean(destroyMethod = "stop")
    public KafkaContainer kafka() {
        KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"))
                .withReuse(true);

        kafkaContainer.start();

        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.consumer.bootstrap-servers", kafkaContainer.getBootstrapServers());
        System.setProperty("spring.kafka.producer.bootstrap-servers", kafkaContainer.getBootstrapServers());

        return kafkaContainer;
    }

    @Bean(destroyMethod = "stop")
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
