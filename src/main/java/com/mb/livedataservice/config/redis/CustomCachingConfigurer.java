package com.mb.livedataservice.config.redis;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Configuration;

/**
 * Single CachingConfigurer for the entire application.
 * Registers the primary cache resolver globally for all @Cacheable annotations.
 * <p>
 * When multiple Redis configs exist (marketing, order, inventory...),
 * inject the @Primary one here. Each config still produces its own
 * TypeAwareRedisCache.Resolver bean — this class just designates which
 * one is the application-wide default.
 */
@Configuration
public class CustomCachingConfigurer implements CachingConfigurer {

    private final TypeAwareRedisCache.Resolver resolver;

    // @Primary resolver is injected automatically.
    // To use a specific one: @Qualifier("inventoryCacheResolver")
    public CustomCachingConfigurer(TypeAwareRedisCache.Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public CacheResolver cacheResolver() {
        return resolver;
    }
}
