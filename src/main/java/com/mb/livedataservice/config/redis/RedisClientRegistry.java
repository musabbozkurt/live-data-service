package com.mb.livedataservice.config.redis;

import com.mb.livedataservice.service.CacheService;
import com.mb.livedataservice.util.CustomStringUtils;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

public record RedisClientRegistry(Map<String, RedisClientBundle> bundles) {

    public RedisConnectionFactory connectionFactory(String clientKey) {
        return bundle(clientKey).redisConnectionFactory();
    }

    public RedisTemplate<String, Object> redisTemplate(String clientKey) {
        return bundle(clientKey).redisTemplate();
    }

    public RedisCacheManager redisCacheManager(String clientKey) {
        return bundle(clientKey).redisCacheManager();
    }

    public TypeAwareRedisCache.Resolver cacheResolver(String clientKey) {
        return bundle(clientKey).cacheResolver();
    }

    public CacheService cacheService(String clientKey) {
        return bundle(clientKey).cacheService();
    }

    public RedisClientBundle bundle(String clientKey) {
        RedisClientBundle bundle = bundles.get(CustomStringUtils.toCamelCase(clientKey));
        if (bundle == null) {
            throw new IllegalArgumentException("Unknown Redis client: " + clientKey);
        }
        return bundle;
    }
}

