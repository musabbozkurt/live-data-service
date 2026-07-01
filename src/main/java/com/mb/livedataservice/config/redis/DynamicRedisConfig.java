package com.mb.livedataservice.config.redis;

import com.mb.livedataservice.config.redis.serializer.CustomJackson2JsonRedisSerializer;
import com.mb.livedataservice.service.CacheService;
import com.mb.livedataservice.util.CustomStringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers Redis clients from configuration and exposes them as Spring beans.
 */
@EnableCaching
@Configuration
@EnableConfigurationProperties(RedisClusterProperties.class)
public class DynamicRedisConfig {

    @Bean
    public RedisClientRegistry redisClientRegistry(RedisClusterProperties redisClusterProperties, ObjectMapper objectMapper, ConfigurableListableBeanFactory beanFactory) {
        Map<String, RedisClientBundle> bundles = new LinkedHashMap<>();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        redisClusterProperties.getClusters().forEach((clientKey, clientProperties) -> {
            String prefix = CustomStringUtils.toCamelCase(clientKey);
            bundles.put(prefix, createBundle(clientProperties, objectMapper));

            boolean primary = clientProperties.isPrimary();

            registerFactoryBean(registry, prefix + "RedisConnectionFactory", RedisConnectionFactory.class, "redisConnectionFactory", prefix, primary);
            registerFactoryBean(registry, prefix + "RedisTemplate", RedisTemplate.class, "redisTemplate", prefix, primary);
            registerFactoryBean(registry, prefix + "RedisCacheManager", RedisCacheManager.class, "redisCacheManager", prefix, primary);
            registerFactoryBean(registry, prefix + "CacheResolver", TypeAwareRedisCache.Resolver.class, "cacheResolver", prefix, primary);
            registerFactoryBean(registry, prefix + "CacheService", CacheService.class, "cacheService", prefix, false);
        });

        return new RedisClientRegistry(Map.copyOf(bundles));
    }

    private RedisClientBundle createBundle(RedisClusterProperties.ClusterConfig clusterConfig, ObjectMapper objectMapper) {
        RedisConnectionFactory connectionFactory = createConnectionFactory(clusterConfig);
        RedisTemplate<String, Object> template = createRedisTemplate(connectionFactory);
        RedisCacheManager cacheManager = createCacheManager(connectionFactory, objectMapper);
        TypeAwareRedisCache.Resolver cacheResolver = new TypeAwareRedisCache.Resolver(cacheManager, objectMapper);
        CacheService cacheService = new CacheService(objectMapper, template);
        return new RedisClientBundle(connectionFactory, template, cacheManager, cacheResolver, cacheService);
    }

    private RedisConnectionFactory createConnectionFactory(RedisClusterProperties.ClusterConfig clusterConfig) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(clusterConfig.getHost());
        config.setPort(clusterConfig.getPort());
        config.setPassword(clusterConfig.getPassword());
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        factory.start();
        return factory;
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        CustomJackson2JsonRedisSerializer customSerializer = new CustomJackson2JsonRedisSerializer();

        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(customSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(customSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private RedisCacheManager createCacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> cacheName + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new CustomJackson2JsonRedisSerializer()));
        return new TypeAwareRedisCache.Manager(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), config, objectMapper);
    }

    private void registerFactoryBean(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass, String factoryMethod, String prefix, boolean primary) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(beanClass);
        definition.setFactoryBeanName("redisClientRegistry");
        definition.setFactoryMethodName(factoryMethod);
        definition.getConstructorArgumentValues().addGenericArgumentValue(prefix);
        definition.setPrimary(primary);
        definition.setAutowireCandidate(true);
        registry.registerBeanDefinition(beanName, definition);
    }
}
