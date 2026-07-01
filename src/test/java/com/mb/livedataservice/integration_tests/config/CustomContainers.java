package com.mb.livedataservice.integration_tests.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

interface CustomContainers {

    String REDIS_USERNAME = "testUser";
    String REDIS_PASSWORD = "testPassword";

    @Container
    @ServiceConnection
    ArtemisContainer artemisContainer = new ArtemisContainer(DockerImageName.parse("apache/activemq-artemis:latest-alpine"))
            .withEnv("ANONYMOUS_LOGIN", "true")
            .withEnv("EXTRA_ARGS", "--global-max-size 256M")
            .withExposedPorts(61616)
            .withReuse(true);

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
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-icu.html">Elasticsearch ICU Analysis Plugin</a>
     */
    @Container
    @ServiceConnection
    ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:9.3.1"))
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withEnv("cluster.name", "elasticsearch")
            .withCommand("sh", "-c", "bin/elasticsearch-plugin install analysis-icu && exec /usr/local/bin/docker-entrypoint.sh elasticsearch")
            .withReuse(true);

    @Container
    @ServiceConnection
    KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"))
            .withReuse(true);

    @Container
    @ServiceConnection
    PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:18.3")
            .withReuse(true);

    @Container
    @ServiceConnection
    RedisContainer redisContainer = new RedisContainer("redis:8.6.1")
            .withExposedPorts(6379, 6380, 6381)
            //.withCommand("redis-server", "--requirepass", REDIS_PASSWORD, "--user", REDIS_USERNAME, "on", ">" + REDIS_PASSWORD, "~*", "+@all")
            .withCommand("sh", "-c", "redis-server --port 6379 & redis-server --port 6380 --daemonize yes && redis-server --port 6381 --daemonize yes && wait")
            .withReuse(true);
}
