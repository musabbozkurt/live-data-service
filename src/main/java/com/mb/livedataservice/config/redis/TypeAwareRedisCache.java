package com.mb.livedataservice.config.redis;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * All-in-one typed cache solution in a single file.
 * <p>
 * TypeAwareRedisCache         - overrides lookup() to apply convertValue() after deserialization
 * TypeAwareRedisCache.Manager - creates TypeAwareRedisCache instances
 * TypeAwareRedisCache.Resolver - injects the @Cacheable method return type before each lookup
 * <p>
 * Flow:
 * Resolver.resolveCaches() reads the method generic return type (e.g. Map<String, TemplateDto>)
 * -> sets it on a static ThreadLocal
 * -> lookup() reads it and calls objectMapper.convertValue(rawLinkedHashMap, targetType)
 * -> @Cacheable receives the correctly typed object
 */
public class TypeAwareRedisCache extends RedisCache {

    // Static ThreadLocal — survives across different cache instances on the same thread
    private static final ThreadLocal<@Nullable JavaType> TARGET_TYPE = new ThreadLocal<>();

    private final ObjectMapper objectMapper;

    TypeAwareRedisCache(String name, RedisCacheWriter writer, RedisCacheConfiguration config, ObjectMapper objectMapper) {
        super(name, writer, config);
        this.objectMapper = objectMapper;
    }

    static void setTargetType(@Nullable JavaType type) {
        if (type == null) TARGET_TYPE.remove();
        else TARGET_TYPE.set(type);
    }

    @Override
    protected @Nullable Object lookup(Object key) {
        JavaType type = TARGET_TYPE.get();
        try {
            Object raw = super.lookup(key);
            if (type == null) return raw;
            return objectMapper.convertValue(raw, type);
        } finally {
            TARGET_TYPE.remove(); // always clear — prevents thread-pool leaks
        }
    }

    // ── CacheManager ──────────────────────────────────────────────────────────────────

    public static class Manager extends RedisCacheManager {
        private final RedisCacheWriter writer;
        private final ObjectMapper objectMapper;

        public Manager(RedisCacheWriter writer, RedisCacheConfiguration config, ObjectMapper objectMapper) {
            super(writer, config);
            this.writer = writer;
            this.objectMapper = objectMapper;
        }

        @Override
        protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration config) {
            return new TypeAwareRedisCache(
                    name,
                    writer,
                    config != null ? config : getDefaultCacheConfiguration(),
                    objectMapper
            );
        }
    }

    // ── CacheResolver ─────────────────────────────────────────────────────────────────

    public static class Resolver extends SimpleCacheResolver {
        private final ObjectMapper objectMapper;

        public Resolver(CacheManager cacheManager, ObjectMapper objectMapper) {
            super(cacheManager);
            this.objectMapper = objectMapper;
        }

        @Override
        public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
            Collection<? extends Cache> caches = super.resolveCaches(context);
            TypeAwareRedisCache.setTargetType(resolveReturnType(context.getMethod()));
            return caches;
        }

        @Nullable
        private JavaType resolveReturnType(Method method) {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            Type generic = method.getGenericReturnType();
            if (generic instanceof ParameterizedType pt) {
                Class<?> raw = (Class<?>) pt.getRawType();
                Type[] args = pt.getActualTypeArguments();
                if (Map.class.isAssignableFrom(raw) && args.length == 2)
                    return typeFactory.constructMapType(HashMap.class, toRaw(args[0]), toRaw(args[1]));
                if (Collection.class.isAssignableFrom(raw) && args.length == 1)
                    return typeFactory.constructCollectionType(Set.class.isAssignableFrom(raw) ? Set.class : ArrayList.class, toRaw(args[0]));
            }
            if (generic instanceof Class<?> c) return typeFactory.constructType(c);
            return null;
        }

        private Class<?> toRaw(Type type) {
            if (type instanceof Class<?> c) return c;
            if (type instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
            return Object.class;
        }
    }
}
