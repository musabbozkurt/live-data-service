package com.mb.livedataservice.config;

import com.mb.livedataservice.config.serializer.CustomJackson2JsonRedisSerializer;
import com.mb.livedataservice.service.CacheService;
import com.mb.livedataservice.util.JsonUtils;
import com.mb.livedataservice.util.RedisConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@AutoConfigureAfter(DataRedisAutoConfiguration.class)
@ConditionalOnClass({RedisOperations.class, RedisConnectionFactory.class, RedisCacheConfiguration.class})
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class CacheConfig {

    /*
     * If the Redis client is protected, add this config bean. Otherwise, this bean can be removed.
     *
     * However, if you can run any commands in redis, please run the following command to enable org.springframework.data.redis.core.RedisKeyExpiredEvent
     * command -> redis-cli config set notify-keyspace-events xE
     *
     * notify-keyspace-events should be xE.
     * To get the value of notify-keyspace-events run this -> config get "notify-keyspace-events"
     *
     * This means that Spring Session cannot configure Redis Keyspace events for you.
     * To disable the automatic configuration add ConfigureRedisAction.NO_OP as a bean.
     * */
    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    @Bean(name = "cacheManager")
    @ConditionalOnMissingBean(name = "cacheManager")
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration expireIn1Day = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put(RedisConstants.CACHE_KEY, expireIn1Day);

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonUtils.createMapper();
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.data.redis.host:localhost}") String host,
                                                         @Value("${spring.data.redis.port:6379}") int port) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        CustomJackson2JsonRedisSerializer customSerializer = new CustomJackson2JsonRedisSerializer();

        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(customSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(customSerializer);
        return redisTemplate;
    }

    @Bean
    @Primary
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, @Value("${spring.application.name}") String applicationName) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .prefixCacheNameWith(applicationName + ":")
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new CustomJackson2JsonRedisSerializer())))
                .build();
    }

    @Bean
    public CacheService cacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        return new CacheService(objectMapper, redisTemplate);
    }
}
