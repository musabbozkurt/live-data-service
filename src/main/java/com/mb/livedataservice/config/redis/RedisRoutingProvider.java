package com.mb.livedataservice.config.redis;

import com.mb.livedataservice.config.redis.serializer.CustomJackson2JsonRedisSerializer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRoutingProvider {

    private final RedisClusterProperties redisClusterProperties;

    private final Map<String, RedisTemplate<String, Object>> templates = new ConcurrentHashMap<>();
    private final Map<String, LettuceConnectionFactory> connectionFactories = new ConcurrentHashMap<>();

    /**
     * Eagerly build a {@link RedisTemplate} for every configured cluster at application startup.
     * This surfaces misconfiguration early (fail-fast) instead of on first use, while the cache
     * keeps {@link #getTemplateForCluster(String)} O(1) afterward.
     */
    @PostConstruct
    void initializeTemplates() {
        redisClusterProperties.getClusters().keySet().forEach(clusterKey -> {
            templates.computeIfAbsent(clusterKey, this::createTemplate);
            log.info("Initialized Redis template for cluster '{}'", clusterKey);
        });
    }

    @PreDestroy
    public void destroy() {
        connectionFactories.values().forEach(LettuceConnectionFactory::destroy);
        connectionFactories.clear();
        templates.clear();
    }

    public RedisTemplate<String, Object> getTemplateForCluster(String clusterKey) {
        // Cache is warmed at startup; this still lazily creates if an unknown-but-configured key appears.
        return templates.computeIfAbsent(clusterKey, this::createTemplate);
    }

    private RedisTemplate<String, Object> createTemplate(String clusterKey) {
        RedisClusterProperties.ClusterConfig config = redisClusterProperties.getClusters().get(clusterKey);
        if (config == null) {
            throw new IllegalArgumentException("No Redis cluster configured for key: " + clusterKey);
        }

        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(config.getHost(), config.getPort());
        if (StringUtils.isNotBlank(config.getPassword())) {
            standalone.setPassword(config.getPassword());
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone);
        factory.afterPropertiesSet();
        connectionFactories.put(clusterKey, factory);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new CustomJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
