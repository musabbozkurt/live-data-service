package com.mb.livedataservice.service;

import com.mb.livedataservice.config.redis.RedisClientRegistry;
import com.mb.livedataservice.config.redis.RedisRoutingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisRoutingProviderService {

    private final RedisRoutingProvider redisProvider;
    private final RedisClientRegistry redisClientRegistry;
    private final CacheService cacheService;
    private final CacheService xCacheService;

    //@Qualifier("yCacheService")
    private final CacheService yCacheService;

    //@Qualifier("zCacheService")
    private final CacheService zCacheService;

    public void cacheData(String targetCluster, String key, Object value) {
        // targetCluster = "x", "y", or "z"
        redisProvider.getTemplateForCluster(targetCluster).opsForValue().set(key, value);
        redisClientRegistry.bundle(targetCluster).cacheService().put(key, value);
    }

    public Set<String> getAllKeys(String targetCluster) {
        var template = redisProvider.getTemplateForCluster(targetCluster);
        return template.keys("*");
    }
}
