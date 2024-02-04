package com.mb.livedataservice.config;

import com.mb.livedataservice.utils.RedisConstants;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@AutoConfigureAfter(RedisAutoConfiguration.class)
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
}
