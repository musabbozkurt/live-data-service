package com.mb.livedataservice.config.redis;

import com.mb.livedataservice.service.CacheService;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public record RedisClientBundle(RedisConnectionFactory redisConnectionFactory,
                                RedisTemplate<String, Object> redisTemplate,
                                RedisCacheManager redisCacheManager,
                                TypeAwareRedisCache.Resolver cacheResolver,
                                CacheService cacheService) {
}
